package killrvideo.service;

import java.util.Optional;
import javax.inject.Inject;

import com.datastax.driver.core.PagingState;

import info.archinnov.achilles.generated.manager.TagsByLetter_Manager;
import info.archinnov.achilles.generated.manager.VideoByTag_Manager;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.search.SearchServiceGrpc.AbstractSearchService;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosResponse;

public class SearchService extends AbstractSearchService {

    @Inject
    VideoByTag_Manager videoByTagManager;

    @Inject
    TagsByLetter_Manager tagsByLetterManager;

    @Override
    public void searchVideos(SearchVideosRequest request, StreamObserver<SearchVideosResponse> responseObserver) {
        videoByTagManager
                .dsl()
                .select()
                .allColumns_FromBaseTable()
                .where()
                .tag_Eq(request.getQuery())
                .limit(request.getPageSize())
                .withOptionalPagingState(request.getPagingState())
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
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return tuple2;
                });
    }

    @Override
    public void getQuerySuggestions(GetQuerySuggestionsRequest request, StreamObserver<GetQuerySuggestionsResponse> responseObserver) {
        tagsByLetterManager
                .dsl()
                .select()
                .tag()
                .fromBaseTable()
                .where()
                .firstLetter_Eq(request.getQuery().substring(0, 1))
                .tag_Gte(request.getQuery())
                .limit(request.getPageSize())
                .getListAsync()
                .handle((entities, ex) -> {
                    if (entities != null) {
                        final GetQuerySuggestionsResponse.Builder builder = GetQuerySuggestionsResponse.newBuilder();
                        entities.stream().forEach(entity -> builder.addSuggestions(entity.getTag()));
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entities;
                });
    }

}
