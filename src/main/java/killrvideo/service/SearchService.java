package killrvideo.service;

import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import killrvideo.entity.Schema;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.VideoByTag;
import killrvideo.utils.FutureUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.PagingState;

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
    Mapper<VideoByTag> videosByTagMapper;

    @Inject
    Mapper<TagsByLetter> tagsByLetterMapper;

    @Inject
    MappingManager manager;

    @Inject
    KillrVideoInputValidator validator;

    private Session session;
    private String tagsByLetterTableName;
    private String videosByTagTableName;

    @PostConstruct
    public void init() {
        this.session = manager.getSession();

        tagsByLetterTableName = tagsByLetterMapper.getTableMetadata().getName();
        videosByTagTableName = videosByTagMapper.getTableMetadata().getName();

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

        BuiltStatement statement = QueryBuilder
                .select()
                .all()
                .from(Schema.KEYSPACE, videosByTagTableName)
                .where(QueryBuilder.eq("tag", request.getQuery()));

        statement
                .setFetchSize(request.getPageSize());

        //:TODO Figure out more streamlined way to do this with Optional and java 8 lambda
        if (pagingState.isPresent()) {
            statement.setPagingState(PagingState.fromString(pagingState.get()));
        }

        FutureUtils.buildCompletableFuture(videosByTagMapper.mapAsync(session.executeAsync(statement)))
                .handle((videos, ex) -> {
                    if (videos != null) {
                        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
                        builder.setQuery(request.getQuery());

                        int remaining = videos.getAvailableWithoutFetching();
                        for (VideoByTag video : videos) {
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

                        LOGGER.error("Exception when searching video by tag : " + mergeStackTrace(ex));

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

        BuiltStatement statement = QueryBuilder
                .select()
                .from(Schema.KEYSPACE, tagsByLetterTableName)
                .where(QueryBuilder.eq("first_letter", request.getQuery().substring(0, 1)))
                .and(QueryBuilder.gte("tag", request.getQuery()));

        statement
                .setFetchSize(request.getPageSize());

        FutureUtils.buildCompletableFuture(tagsByLetterMapper.mapAsync(session.executeAsync(statement)))
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