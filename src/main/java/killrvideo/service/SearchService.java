package killrvideo.service;

import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Optional;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.PagingState;

//import info.archinnov.achilles.generated.manager.TagsByLetter_Manager;
//import info.archinnov.achilles.generated.manager.VideoByTag_Manager;
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

    //:TODO Fix this
    /*
    @Inject
    VideoByTag_Manager videoByTagManager;

    @Inject
    TagsByLetter_Manager tagsByLetterManager;
    */

    @Inject
    KillrVideoInputValidator validator;

    @Override
    public void searchVideos(SearchVideosRequest request, StreamObserver<SearchVideosResponse> responseObserver) {

        LOGGER.debug("Start searching video by tag");

        //:TODO Fix this
        /*
        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Optional<String> pagingState = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        videoByTagManager
                .dsl()
                .select()
                .allColumns_FromBaseTable()
                .where()
                .tag().Eq(request.getQuery())
                .withFetchSize(request.getPageSize())
                .withOptionalPagingStateString(pagingState)
                .getListAsyncWithStats()
                .handle((tuple2, ex) -> {
                    if (tuple2 != null) {
                        final SearchVideosResponse.Builder builder = SearchVideosResponse.newBuilder();
                        builder.setQuery(request.getQuery());
                        tuple2._1().stream().forEach(entity -> builder.addVideos(entity.toResultVideoPreview()));
                        Optional.ofNullable(tuple2._2().getPagingState())
                                .map(PagingState::toString)
                                .ifPresent(builder::setPagingState);
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End searching video by tag");

                    } else if (ex != null) {

                        LOGGER.error("Exception when searching video by tag : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return tuple2;
                });
        */
    }

    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<GetQuerySuggestionsResponse> responseObserver) {

        LOGGER.debug("Start getting query suggestions by tag");

        //:TODO Fix this
        /*
        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        tagsByLetterManager
                .dsl()
                .select()
                .tag()
                .fromBaseTable()
                .where()
                .firstLetter().Eq(request.getQuery().substring(0, 1))
                .tag().Gte(request.getQuery())
                .withFetchSize(request.getPageSize())
                .getListAsync()
                .handle((entities, ex) -> {
                    if (entities != null) {
                        final GetQuerySuggestionsResponse.Builder builder = GetQuerySuggestionsResponse.newBuilder();
                        entities.stream().forEach(entity -> builder.addSuggestions(entity.getTag()));
                        builder.setQuery(request.getQuery());
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting query suggestions by tag");

                    } else if (ex != null) {

                        LOGGER.error("Exception getting query suggestions by tag : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entities;
                });
        */
    }

}