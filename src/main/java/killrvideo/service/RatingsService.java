package killrvideo.service;


import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import killrvideo.entity.*;
import killrvideo.utils.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Mapper;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.events.CassandraMutationError;
import killrvideo.ratings.RatingsServiceGrpc.AbstractRatingsService;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class RatingsService extends AbstractRatingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingsService.class);

    @Inject
    MappingManager manager;

    @Inject
    Mapper<VideoRating> videoRatingMapper;

    @Inject
    Mapper<VideoRatingByUser> videoRatingByUserMapper;

    @Inject
    EventBus eventBus;

    @Inject
    KillrVideoInputValidator validator;

    private Session session;
    private String videoRatingsTableName;
    private PreparedStatement rateVideo_updateRatingPrepared;


    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        videoRatingsTableName = videoRatingMapper.getTableMetadata().getName();

        rateVideo_updateRatingPrepared = session.prepare(
                QueryBuilder
                        .update(Schema.KEYSPACE, videoRatingsTableName)
                        .with(QueryBuilder.incr("rating_counter"))
                        .and(QueryBuilder.incr("rating_total", QueryBuilder.bindMarker()))
                        .where(QueryBuilder.eq("videoid", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
    }

    @Override
    public void rateVideo(RateVideoRequest request, StreamObserver<RateVideoResponse> responseObserver) {

        LOGGER.debug("-----Start rate video request-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Instant time = Instant.now();

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());
        final Integer rating = request.getRating();

        /**
         * Increment rating_counter by 1
         * Increment rating_total by amount of rating
         */
        BoundStatement counterUpdateStatement = rateVideo_updateRatingPrepared.bind()
                .setLong("rating_total", rating)
                .setUUID("videoid", videoId);

        /**
         * Here, instead of using logged batch, we can insert both mutations asynchronously
         * In case of error, we log the request into the mutation error log for replay later
         * by another micro-service
         *
         * Something else to notice is I am using both a prepared statement with executeAsync()
         * and a call to the mapper's saveAsync() methods.  I could have kept things uniform
         * and stuck with both prepared/bind statements, but I wanted to illustrate the combination
         * and use the mapper for the second statement because it is a simple save operation with no
         * options, increments, etc...  A key point is in the case you see below both statements are actually
         * prepared, the first one I did manually in a more traditional sense and in the second one the
         * mapper will prepare the statement for you automagically.
         */
        CompletableFuture
                .allOf(
                        FutureUtils.buildCompletableFuture(session.executeAsync(counterUpdateStatement)),
                        FutureUtils.buildCompletableFuture(videoRatingByUserMapper
                                .saveAsync(new VideoRatingByUser(videoId, userId, rating)))
                )
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
                });
    }

    @Override
    public void getRating(GetRatingRequest request, StreamObserver<GetRatingResponse> responseObserver) {

        LOGGER.debug("-----Start get video rating request-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        // videoId matches the partition key set in the VideoRating class
        FutureUtils.buildCompletableFuture(videoRatingMapper.getAsync(videoId))
                .handle((ratings, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Exception when getting video rating : " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    } else {
                        if (ratings != null) {
                            responseObserver.onNext((ratings.toRatingResponse()));
                        }
                        /**
                         * If no row is returned (entity == null), we should
                         * still build a response with 0 as rating value
                         */
                        else {
                            responseObserver.onNext(GetRatingResponse.newBuilder()
                                    .setVideoId(request.getVideoId())
                                    .setRatingsCount(0L)
                                    .setRatingsTotal(0L)
                                    .build());
                        }
                        responseObserver.onCompleted();
                        LOGGER.debug("End get video rating request");
                    }
                    return ratings;
                });

    }

    @Override
    public void getUserRating(GetUserRatingRequest request, StreamObserver<GetUserRatingResponse> responseObserver) {

        LOGGER.debug("-----Start get user rating request-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        FutureUtils.buildCompletableFuture(videoRatingByUserMapper.getAsync(videoId, userId))
                .handle((videoRating, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Exception when getting user rating : " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    } else {
                        if (videoRating != null) {
                            responseObserver.onNext(videoRating.toUserRatingResponse());
                        }
                        /**
                         * If no row is returned (entity == null), we should
                         * still build a response with 0 as rating value
                         */
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
                    return videoRating;
                });
    }

}
