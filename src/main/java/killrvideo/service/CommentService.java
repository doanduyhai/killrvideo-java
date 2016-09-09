package killrvideo.service;

import static info.archinnov.achilles.internals.futures.FutureUtils.toCompletableFuture;
import static java.util.UUID.fromString;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.*;
import com.google.common.eventbus.EventBus;

import info.archinnov.achilles.generated.manager.CommentsByUser_Manager;
import info.archinnov.achilles.generated.manager.CommentsByVideo_Manager;
import info.archinnov.achilles.type.tuples.Tuple2;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceGrpc.AbstractCommentsService;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents.UserCommentedOnVideo;
import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.entity.CommentsByUser;
import killrvideo.entity.CommentsByVideo;
import killrvideo.events.CassandraMutationError;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class CommentService extends AbstractCommentsService {

    private static Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    @Inject
    CommentsByUser_Manager commentsByUserManager;

    @Inject
    CommentsByVideo_Manager commentsByVideoManager;

    @Inject
    EventBus eventBus;

    @Inject
    ExecutorService executorService;

    @Inject
    KillrVideoInputValidator validator;

    Session session;

    @PostConstruct
    public void init(){
        this.session = commentsByUserManager.getNativeSession();
    }

    @Override
    public void commentOnVideo(final CommentOnVideoRequest request, StreamObserver<CommentOnVideoResponse> responseObserver) {

        LOGGER.debug("Start comment on video request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Date now = new Date();

        final CommentsByVideo commentsByVideo = new CommentsByVideo(request);
        final CommentsByUser commentsByUser = new CommentsByUser(request);

        final BoundStatement bs1 = commentsByUserManager.crud().insert(commentsByUser).generateAndGetBoundStatement();
        final BoundStatement bs2 = commentsByVideoManager.crud().insert(commentsByVideo).generateAndGetBoundStatement();

        /**
         * We need to insert into comments_by_user and comments_by_video
         * simultaneously, thus using logged batch for automatic retries
         * in case of error
         */

        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(bs1);
        batchStatement.add(bs2);
        batchStatement.setDefaultTimestamp(now.getTime());

        toCompletableFuture(session.executeAsync(batchStatement), executorService)
            .handle((rs,ex) -> {
                if(rs != null) {
                    eventBus.post(UserCommentedOnVideo.newBuilder()
                            .setCommentId(request.getCommentId())
                            .setVideoId(request.getVideoId())
                            .setUserId(request.getUserId())
                            .setCommentTimestamp(TypeConverter.dateToTimestamp(now))
                            .build());
                    responseObserver.onNext(CommentOnVideoResponse.newBuilder().build());
                    responseObserver.onCompleted();

                    LOGGER.debug("End comment on video request");

                } else if (ex != null) {

                    LOGGER.error("Exception commenting on video : " + mergeStackTrace(ex));

                    eventBus.post(new CassandraMutationError(request, ex));
                    responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                }
                return rs;
            });
    }

    @Override
    public void getUserComments(GetUserCommentsRequest request, StreamObserver<GetUserCommentsResponse> responseObserver) {

        LOGGER.debug("Start get user comments request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final TimeUuid startingCommentId = request.getStartingCommentId();
        final CompletableFuture<Tuple2<List<CommentsByUser>, ExecutionInfo>> future;
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first user comment as reference point
         */
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
            future = commentsByUserManager
                    .dsl()
                    .select()
                    .commentid()
                    .videoid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .userid().Eq(fromString(request.getUserId().getValue()))
                    .withFetchSize(request.getPageSize())
                    .withOptionalPagingStateString(pagingStateString)
                    .getListAsyncWithStats();

        }
        /**
         * Subsequent requests always provide startingCommentId to load page
         * of user comments. Fetch size/page size is expected to be > 1
         */
        else {
            future = commentsByUserManager
                    .dsl()
                    .select()
                    .commentid()
                    .videoid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .userid().Eq(fromString(request.getUserId().getValue()))
                    .commentid().Lte(fromString(request.getStartingCommentId().getValue()))
                    .withFetchSize(request.getPageSize())
                    .getListAsyncWithStats();
        }

        future.handle((tuple2, ex) -> {
            if(tuple2 != null) {
                final GetUserCommentsResponse.Builder builder = GetUserCommentsResponse.newBuilder();
                tuple2._1().forEach(commentsByUser-> builder.addComments(commentsByUser.toUserComment()));
                Optional.ofNullable(tuple2._2().getPagingState())
                        .map(PagingState::toString)
                        .ifPresent(builder::setPagingState);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

                LOGGER.debug("End get user comments request");

            } else if (ex != null) {

                LOGGER.error("Exception getting user comments : " + mergeStackTrace(ex));

                responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
            }
            return tuple2;
        });
    }

    @Override
    public void getVideoComments(GetVideoCommentsRequest request, StreamObserver<GetVideoCommentsResponse> responseObserver) {

        LOGGER.debug("Start get video comments request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final TimeUuid startingCommentId = request.getStartingCommentId();
        final CompletableFuture<Tuple2<List<CommentsByVideo>, ExecutionInfo>> future;
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first video comment as reference point
         */
        if (startingCommentId == null || isBlank(request.getStartingCommentId().getValue())) {
            future = commentsByVideoManager
                    .dsl()
                    .select()
                    .commentid()
                    .userid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .videoid().Eq(fromString(request.getVideoId().getValue()))
                    .withFetchSize(request.getPageSize())
                    .withOptionalPagingStateString(pagingStateString)
                    .getListAsyncWithStats();

        }
        /**
         * Subsequent requests always provide startingCommentId to load page
         * of video comments. Fetch size/page size is expected to be > 1
         */
        else {
            future = commentsByVideoManager
                    .dsl()
                    .select()
                    .commentid()
                    .userid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .videoid().Eq(fromString(request.getVideoId().getValue()))
                    .commentid().Lte(fromString(request.getStartingCommentId().getValue()))
                    .withFetchSize(request.getPageSize())
                    .getListAsyncWithStats();
        }

        future.handle((tuple2, ex) -> {
            if (tuple2 != null) {
                final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();
                tuple2._1().forEach(commentsByVideo -> builder.addComments(commentsByVideo.toVideoComment()));
                Optional.ofNullable(tuple2._2().getPagingState())
                        .map(PagingState::toString)
                        .ifPresent(builder::setPagingState);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

                LOGGER.debug("End get video comments request");

            } else if (ex != null) {

                LOGGER.error("Exception getting video comments : " + mergeStackTrace(ex));

                responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
            }
            return tuple2;
        });
    }
}
