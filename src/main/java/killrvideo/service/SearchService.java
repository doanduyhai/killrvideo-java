package killrvideo.service;

import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Optional;
import java.util.regex.Matcher;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.mapping.Mapper;
import killrvideo.entity.Schema;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.Video;
import killrvideo.utils.FutureUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceGrpc.AbstractSearchService;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class SearchService extends AbstractSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Inject
    Mapper<TagsByLetter> tagsByLetterMapper;

    @Inject
    Mapper<Video> videosMapper;

    @Inject
    KillrVideoInputValidator validator;

    @Inject
    DseSession dseSession;

    private String tagsByLetterTableName;
    private String videosTableName;
    private PreparedStatement getQuerySuggestions_getTagsPrepared;
    private PreparedStatement searchVideos_getVideosWithSearchPrepared;

    @PostConstruct
    public void init() {
        tagsByLetterTableName = tagsByLetterMapper.getTableMetadata().getName();
        videosTableName = videosMapper.getTableMetadata().getName();

        getQuerySuggestions_getTagsPrepared = dseSession.prepare(
                QueryBuilder
                        .select()
                        .from(Schema.KEYSPACE, tagsByLetterTableName)
                        .where(QueryBuilder.eq("first_letter", QueryBuilder.bindMarker()))
                        .and(QueryBuilder.gte("tag", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        /**
         * Pass a column name of "solr_query" to the QueryBuilder because we are
         * using DSE Search to provide a more comprehensive video search experience.
         * Notice we are using a consistency of LOCAL_ONE instead of LOCAL_QUORUM as for other
         * queries.  LOCAL_QUORUM is not currently supported by DSE Search.
         */
        searchVideos_getVideosWithSearchPrepared = dseSession.prepare(
                QueryBuilder
                        .select()
                        .all()
                        .from(Schema.KEYSPACE, videosTableName)
                        .where(QueryBuilder.eq("solr_query", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
    }

    @Override
    public void searchVideos(SearchVideosRequest request, StreamObserver<SearchVideosResponse> responseObserver) {

        LOGGER.debug("Start searching video by tag");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Optional<String> pagingState = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        final StringBuilder solrQuery = new StringBuilder();
        final String replaceFind = "\"";
        final String replaceWith = "\\\"";

        /**
         * Do a Solr query against DSE search to find videos using Solr's ExtendedDisMax query parser. Query the
         * name, tags, and description fields in the videos table giving a boost to matches in the name and tags
         * fields as opposed to the description field
         * More info on ExtendedDisMax: http://wiki.apache.org/solr/ExtendedDisMax
         *
         * Notice the "paging":"driver" parameter.  This is to ensure we dynamically
         * enable pagination regardless of our nodes dse.yaml setting.
         * https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/search/cursorsDeepPaging.html#cursorsDeepPaging__srchCursorCQL
         */
        String requestQuery = request.getQuery()
                .replaceAll(replaceFind, Matcher.quoteReplacement(replaceWith));

        solrQuery
                .append("{\"q\":\"{!edismax qf=\\\"name^2 tags^1 description\\\"}")
                .append(requestQuery)
                .append("\", \"paging\":\"driver\"}");

        LOGGER.debug("searchVideos() solr_query is : " + solrQuery);
        BoundStatement statement = searchVideos_getVideosWithSearchPrepared.bind()
                .setString("solr_query", solrQuery.toString());

        statement.setFetchSize(request.getPageSize());

        pagingState.ifPresent( x -> statement.setPagingState(PagingState.fromString(x)));

        FutureUtils.buildCompletableFuture(videosMapper.mapAsync(dseSession.executeAsync(statement)))
                .handle((videos, ex) -> {
                    if (videos != null) {
                        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
                        builder.setQuery(request.getQuery());

                        int remaining = videos.getAvailableWithoutFetching();
                        for (Video video : videos) {
                            builder.addVideos(video.toResultVideoPreview());

                            if (--remaining == 0) {
                                break;
                            }
                        }

                        Optional.ofNullable(videos.getExecutionInfo().getPagingState())
                                .map(PagingState::toString)
                                .ifPresent(builder::setPagingState);
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End searching video by tag");

                    } else if (ex != null) {
                        LOGGER.error(this.getClass().getName() + ".searchVideos() Exception when searching video by tag: " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return videos;
                });
    }

    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<GetQuerySuggestionsResponse> responseObserver) {

        LOGGER.debug("Start getting query suggestions by tag");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        BoundStatement statement = getQuerySuggestions_getTagsPrepared.bind()
                .setString("first_letter", request.getQuery().substring(0, 1))
                .setString("tag", request.getQuery());

        statement.setFetchSize(request.getPageSize());

        FutureUtils.buildCompletableFuture(tagsByLetterMapper.mapAsync(dseSession.executeAsync(statement)))
                .handle((tags, ex) -> {
                    if (tags != null) {
                        final GetQuerySuggestionsResponse.Builder builder = GetQuerySuggestionsResponse.newBuilder();

                        int remaining = tags.getAvailableWithoutFetching();
                        for (TagsByLetter tag : tags) {
                            builder.addSuggestions(tag.getTag());

                            if (--remaining == 0) {
                                break;
                            }
                        }

                        builder.setQuery(request.getQuery());
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting query suggestions by tag");

                    } else if (ex != null) {
                        LOGGER.error("Exception getting query suggestions by tag : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return tags;
                });
    }

}