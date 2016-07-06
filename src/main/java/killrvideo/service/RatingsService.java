package killrvideo.service;


import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import info.archinnov.achilles.generated.manager.VideoRatingByUser_Manager;
import info.archinnov.achilles.generated.manager.VideoRating_Manager;
import info.archinnov.achilles.type.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.VideoRatingByUser;
import killrvideo.ratings.RatingsServiceGrpc.AbstractRatingsService;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.utils.TypeConverter;

@Service
public class RatingsService extends AbstractRatingsService {

    @Inject
    VideoRating_Manager ratingManager;

    @Inject
    VideoRatingByUser_Manager ratingByUserManager;

    @Inject
    EventBus eventBus;

    @Override
    public void rateVideo(RateVideoRequest request, StreamObserver<RateVideoResponse> responseObserver) {
        final Instant time = Instant.now();

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        final CompletableFuture<Empty> counterUpdateAsync = ratingManager
                .dsl()
                .update()
                .fromBaseTable()
                .ratingCounter_Incr()
                .ratingTotal_Incr(new Long(request.getRating()))
                .where()
                .videoid_Eq(videoId)
                .executeAsync();

        final CompletableFuture<Empty> ratingInsertAsync = ratingByUserManager
                .crud()
                .insert(new VideoRatingByUser(videoId, userId, request.getRating()))
                .executeAsync();


        CompletableFuture
                .allOf(counterUpdateAsync, ratingInsertAsync)
                .handle((rs, ex) -> {
                    if (rs != null) {
                        eventBus.post(UserRatedVideo.newBuilder()
                                .setVideoId(request.getVideoId())
                                .setUserId(request.getUserId())
                                .setRating(request.getRating())
                                .setRatingTimestamp(TypeConverter.instantToTimeStamp(time))
                                .build());
                        responseObserver.onNext(RateVideoResponse.newBuilder().build());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                });
    }

    public void getRating(GetRatingRequest request, StreamObserver<GetRatingResponse> responseObserver) {
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        ratingManager
                .crud()
                .findById(videoId)
                .getAsync()
                .handle((entity, ex) -> {
                    if (entity != null) {
                        responseObserver.onNext(entity.toRatingResponse());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entity;
                });

    }

    @Override
    public void getUserRating(GetUserRatingRequest request, StreamObserver<GetUserRatingResponse> responseObserver) {
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        ratingByUserManager
                .crud()
                .findById(videoId, userId)
                .getAsync()
                .handle((entity, ex) -> {
                    if (entity != null) {
                        responseObserver.onNext(entity.toUserRatingResponse());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entity;
                });
    }


}
