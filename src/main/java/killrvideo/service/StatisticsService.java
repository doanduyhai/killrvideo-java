package killrvideo.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import info.archinnov.achilles.generated.manager.VideoPlaybackStats_Manager;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.VideoPlaybackStats;
import killrvideo.statistics.StatisticsServiceGrpc.AbstractStatisticsService;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse;

public class StatisticsService extends AbstractStatisticsService {

    @Inject
    VideoPlaybackStats_Manager videoPlaybackStatsManager;

    @Override
    public void recordPlaybackStarted(RecordPlaybackStartedRequest request, StreamObserver<RecordPlaybackStartedResponse> responseObserver) {

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        videoPlaybackStatsManager
                .dsl()
                .update()
                .fromBaseTable()
                .views_Incr()
                .where()
                .videoid_Eq(videoId)
                .executeAsync()
                .handle((rs, ex) -> {
                    if (rs != null) {
                        responseObserver.onNext(RecordPlaybackStartedResponse.newBuilder().build());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                });
    }

    @Override
    public void getNumberOfPlays(GetNumberOfPlaysRequest request, StreamObserver<GetNumberOfPlaysResponse> responseObserver) {

        if (request.getVideoIdsCount() > 20) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Cannot do a get more than 20 videos at once").asRuntimeException());
        }

        final List<CompletableFuture<VideoPlaybackStats>> statsFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> videoPlaybackStatsManager.crud().findById(uuid).getAsync())
                .collect(toList());

        final GetNumberOfPlaysResponse.Builder builder = GetNumberOfPlaysResponse.newBuilder();

        CompletableFuture
                .allOf(statsFuture.toArray(new CompletableFuture[statsFuture.size()]))
                .thenApply(v -> statsFuture.stream().map(CompletableFuture::join).collect(toList()))
                .thenAccept(list -> list
                        .stream()
                        .map(entity -> builder.addStats(entity.toPlayStats()))
                        .collect(toList()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
