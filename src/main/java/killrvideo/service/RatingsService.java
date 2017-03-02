package killrvideo.service;


import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import com.google.common.util.concurrent.ListenableFuture;
//import com.sun.tools.javac.code.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Mapper;

//import info.archinnov.achilles.generated.manager.VideoRatingByUser_Manager;
//import info.archinnov.achilles.generated.manager.VideoRating_Manager;
//import info.archinnov.achilles.type.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.VideoRating;
import killrvideo.entity.VideoRatingByUser;
import killrvideo.events.CassandraMutationError;
import killrvideo.ratings.RatingsServiceGrpc.AbstractRatingsService;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class RatingsService extends AbstractRatingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingsService.class);

    //@Inject
    //VideoRating_Manager ratingManager;

    @Inject
    MappingManager manager;

    //@Inject
    //VideoRating rating;

    //@Inject
    //VideoRatingByUser_Manager ratingByUserManager;

    @Inject
    EventBus eventBus;

    @Inject
    KillrVideoInputValidator validator;

    @Override
    public void rateVideo(RateVideoRequest request, StreamObserver<RateVideoResponse> responseObserver) {

        LOGGER.debug("Start rate video request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Instant time = Instant.now();

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        /**
         * Increment rating_counter by 1
         * Increment rating_total by amount of rating
         */
        /*final CompletableFuture<Empty> counterUpdateAsync = ratingManager
                .dsl()
                .update()
                .fromBaseTable()
                .ratingCounter().Incr()
                .ratingTotal().Incr(new Long(request.getRating()))
                .where()
                .videoid().Eq(videoId)
                .executeAsync();*/

        Mapper<VideoRating> mapper = manager.mapper(VideoRating.class);

        // videoId matches the partition key set in the VideoRating class
        VideoRating videoRating = mapper.get(videoId);
        Long rating = videoRating.getRatingCounter();

        LOGGER.debug("Video rating count for videoId: " + videoId + " IS " + rating);

        // Now increment the counter
        videoRating.setRatingCounter(rating + 1);
        mapper.saveAsync(videoRating);

        //:TODO Replace ratingByUserManager with supporting DSE driver
        /**
         * Insert the rating into video_ratings_by_user
         */
        /*
        final CompletableFuture<Empty> ratingInsertAsync = ratingByUserManager
                .crud()
                .insert(new VideoRatingByUser(videoId, userId, request.getRating()))
                .executeAsync();
         */

        /**
         * Here, instead of using logged batch, we can insert both mutations asynchronously
         * In case of error, we log the request into the mutation error log for replay later
         * by another micro-service
         */
        /* CompletableFuture
                .allOf(counterUpdateAsync, ratingInsertAsync)
                .handle((rs, ex) -> {
                    if (ex == null) {
                        eventBus.post(UserRatedVideo.newBuilder()
                                .setVideoId(request.getVideoId())
                                .setUserId(request.getUserId())
                                .setRating(request.getRating())
                                .setRatingTimestamp(TypeConverter.instantToTimeStamp(time))
                                .build());
                        responseObserver.onNext(RateVideoResponse.newBuilder().build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End rate video request");

                    } else {

                        LOGGER.error("Exception rating video : " + mergeStackTrace(ex));

                        eventBus.post(new CassandraMutationError(request, ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                }); */
    }

    @Override
    public void getRating(GetRatingRequest request, StreamObserver<GetRatingResponse> responseObserver) {

        LOGGER.debug("Start get video rating request");
        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        Mapper<VideoRating> mapper = manager.mapper(VideoRating.class);
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        // videoId matches the partition key set in the VideoRating class
        VideoRating videoRating = mapper.get(videoId);
        Long rating = videoRating.getRatingCounter();
        LOGGER.debug("Video rating count for videoId: " + videoId + " IS " + rating);

        /*ratingManager
                .crud()
                .findById(videoId)
                .getAsync()
                .handle((entity, ex) -> {
                    if (ex != null) {

                        LOGGER.error("Exception when getting video rating : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    } else {
                        if (entity != null) {
                            responseObserver.onNext(entity.toRatingResponse());
                        }*/
                        /**
                         * If no row is returned (entity == null), we should
                         * still build a response with 0 as rating value
                         */
                        /*else {
                            responseObserver.onNext(GetRatingResponse.newBuilder()
                                    .setVideoId(request.getVideoId())
                                    .setRatingsCount(0L)
                                    .setRatingsTotal(0L)
                                    .build());
                        }
                        responseObserver.onCompleted();
                        LOGGER.debug("End get video rating request");
                    }
                    return entity;
                }); */

    }

    @Override
    public void getUserRating(GetUserRatingRequest request, StreamObserver<GetUserRatingResponse> responseObserver) {

        LOGGER.debug("Start get user rating request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        //:TODO Replace ratingByUserManager with supporting DSE driver
        /*
        ratingByUserManager
                .crud()
                .findById(videoId, userId)
                .getAsync()
                .handle((entity, ex) -> {
                    if (ex != null) {

                        LOGGER.error("Exception when getting user rating : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    } else {
                        if (entity != null) {
                            responseObserver.onNext(entity.toUserRatingResponse());
                        }
                        */
                        /**
                         * If no row is returned (entity == null), we should
                         * still build a response with 0 as rating value
                         */
                        /*
                        else {
                            responseObserver.onNext(GetUserRatingResponse
                                    .newBuilder()
                                    .setUserId(request.getUserId())
                                    .setVideoId(request.getVideoId())
                                    .setRating(0)
                                    .build());
                        }
                        responseObserver.onCompleted();
                        LOGGER.debug("End get user rating request");
                    }
                    return entity;
                });
                */
    }


}
