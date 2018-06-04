package killrvideo.service;

import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import killrvideo.configuration.KillrVideoConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.mapping.Mapper;
import com.google.common.reflect.TypeToken;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.Schema;
import killrvideo.entity.Video;
import killrvideo.search.SearchServiceGrpc.SearchServiceImplBase;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;
import killrvideo.utils.FutureUtils;
import killrvideo.validation.KillrVideoInputValidator;

@Service
//public class SearchService extends AbstractSearchService {
public class SearchService extends SearchServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Inject
    Mapper<Video> videosMapper;

    @Inject
    KillrVideoInputValidator validator;

    @Inject
    DseSession dseSession;

    /** Load configuration from Yaml file and environments variables. */
    @Inject
    private KillrVideoConfiguration config;

    private String videosTableName;
    
    private PreparedStatement getQuerySuggestions_getTagsPrepared;
    
    private PreparedStatement searchVideos_getVideosWithSearchPrepared;

    /**
     * Wrap search queries with "paging":"driver" to dynamically enable
     * paging to ensure we pull back all available results in the application.
     * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/cursorsDeepPaging.html#cursorsDeepPaging__using-paging-with-cql-solr-queries-solrquery-Rim2GsbY
     */
    final private String pagingDriverStart = "{\"q\":\"";
    final private String pagingDriverEnd = "\", \"paging\":\"driver\"}";

    @PostConstruct
    public void init() {
        videosTableName = videosMapper.getTableMetadata().getName();

        /**
         * Pass a column name of "solr_query" to the QueryBuilder because we are
         * using DSE Search to provide a more comprehensive video search experience.
         * Notice we are using a consistency of LOCAL_ONE instead of LOCAL_QUORUM
         * as compared to other queries.  LOCAL_QUORUM is not currently supported by DSE Search.
         */

        getQuerySuggestions_getTagsPrepared = dseSession.prepare(
                QueryBuilder
                        .select("name", "tags", "description")
                        .from(Schema.KEYSPACE, videosTableName)
                        .where(QueryBuilder.eq("solr_query", QueryBuilder.bindMarker()))
                 ).setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

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

        LOGGER.debug("Start searching videos by name, tag, and description");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Optional<String> pagingState = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        final StringBuilder solrQuery = new StringBuilder();
        final String replaceFind = " ";
        final String replaceWith = " AND ";

        /**
         * Perform a query using DSE search to find videos. Query the
         * name, tags, and description columns in the videos table giving a boost to matches in the name and tags
         * columns as opposed to the description column.
         */
        final String requestQuery = request.getQuery().trim()
                .replaceAll(replaceFind, Matcher.quoteReplacement(replaceWith));

        /**
         * In this case we are using DSE Search to query across the name, tags, and
         * description columns with a boost on name and tags.  The boost will put
         * more priority on the name column, then tags, and finally description.
         *
         * Note that tags is a
         * collection of tags per each row with no extra steps to include all data
         * in the collection.  This is a more comprehensive search as
         * we are not just looking at values within the tags column, but also looking
         * across the other columns for similar occurrences.  This is especially helpful
         * if there are no tags for a given video as it is more likely to give us results.
         *
         * Refer to the following documentation for a deeper look at term boosting:
         * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/advancedTerms.html
         */
        solrQuery
                .append(pagingDriverStart)
                .append("name:(").append(requestQuery).append(")^4 OR ")
                .append("tags:(").append(requestQuery).append(")^2 OR ")
                .append("description:(").append(requestQuery).append(")")
                .append(pagingDriverEnd);

        final BoundStatement statement = searchVideos_getVideosWithSearchPrepared.bind().setString("solr_query", solrQuery.toString());
        LOGGER.debug("searchVideos() executed query is : " + statement.preparedStatement().getQueryString());
        LOGGER.debug("searchVideos() solr_query ? is : " + solrQuery);

        statement.setFetchSize(request.getPageSize());

        pagingState.ifPresent(x -> statement.setPagingState(PagingState.fromString(x)));

        /**
         * Even though we are using a DSE Search powered query it is still
         * a CQL query so we can use the same execution style and result types
         * we would expect from a pure CQL query.
         */
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

                        LOGGER.debug("End searching videos by name, tag, and description");

                    } else if (ex != null) {
                        LOGGER.error(this.getClass().getName() + ".searchVideos() Exception when searching video by tag: " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return videos;
                });
    }

    @SuppressWarnings("serial")
    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<GetQuerySuggestionsResponse> responseObserver) {

        LOGGER.debug("Start getting query suggestions by name, tag, and description");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        /**
         * Perform a query using DSE Search against the name, tags, and
         * description columns of the videos table.  Notice the wildcard
         * character "*" tacked on to each requestQuery.  We use this to
         * match any words matching the string typed into the search bar.
         *
         * Refer to the following documentation for a deeper look at search filtering:
         * https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/search_index/queryTerms.html
         */
        final String requestQuery = request.getQuery().trim();
        final StringBuilder solrQuery = new StringBuilder();
        solrQuery
                .append(pagingDriverStart)
                .append("name:(").append(requestQuery).append("*) OR ")
                .append("tags:(").append(requestQuery).append("*) OR ")
                .append("description:(").append(requestQuery).append("*)")
                .append(pagingDriverEnd);

        LOGGER.debug("getQuerySuggestions() solr_query is : " + solrQuery);
        final BoundStatement statement = getQuerySuggestions_getTagsPrepared.bind()
                .setString("solr_query", solrQuery.toString());

        statement.setFetchSize(request.getPageSize());

        /**
         * In this case since I am only returning the name, tags, and description columns and
         * not a complete entity I am not using a mapper, just a normal query
         * with a "Row" result set.
         *
         * Also notice the use of regex pattern matching below.  This is used to
         * parse out suggestion words from the names and tags we pulled using DSE Search.
         */
        FutureUtils.buildCompletableFuture(dseSession.executeAsync(statement))
                .handle((rows, ex) -> {
                    if (rows != null) {
                        final GetQuerySuggestionsResponse.Builder builder = GetQuerySuggestionsResponse.newBuilder();

                        // Use a TreeSet to ensure 1) no duplicates, and 2) words are ordered naturally alphabetically
                        final Set<String> suggestionSet = new TreeSet<>();

                        /**
                         * Here, we are inserting the request from the search bar, maybe something
                         * like "c", "ca", or "cas" as someone starts to type the word "cassandra".
                         * For each of these cases we are looking for any words in the search data that
                         * start with the values above.
                         */
                        final String pattern = "\\b" + request.getQuery() + "[a-z]*\\b";
                        final Pattern checkRegex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

                        int remaining = rows.getAvailableWithoutFetching();
                        for (Row row : rows) {
                            String name = row.getString("name");
                            Set<String> tags = row.getSet("tags", new TypeToken<String>() {});
                            String description = row.getString("description");

                            /**
                             * Since I want matches from the name, tags, and description fields
                             * concatenate them together, apply regex, and add any results into
                             * our suggestionSet TreeMap.  The TreeMap will handle any duplicates
                             * and order our results appropriately.
                             */
                            Matcher regexMatcher = checkRegex.matcher(name.concat(tags.toString()).concat(description));
                            while (regexMatcher.find()) {
                                suggestionSet.add(regexMatcher.group().toLowerCase());
                            }

                            if (--remaining == 0) {
                                break;
                            }
                        }

                        /**
                         * Exclude words that aren't really all that helpful for this type
                         * of search like "and", "of", "the", etc...
                         */
                        suggestionSet.removeAll(config.getIgnoredWords());

                        // Send our results back to the client
                        builder.addAllSuggestions(suggestionSet);
                        builder.setQuery(request.getQuery());
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting query suggestions by name, tag, and description");

                    } else if (ex != null) {
                        LOGGER.error("Exception getting query suggestions by name, tag, and description : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rows;
                });
    }

}