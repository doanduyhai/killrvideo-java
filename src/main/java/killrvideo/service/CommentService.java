package killrvideo.service;

import static info.archinnov.achilles.internals.futures.FutureUtils.toCompletableFuture;
import static java.util.UUID.fromString;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
import killrvideo.utils.TypeConverter;

@Service
public class CommentService extends AbstractCommentsService {

    @Inject
    CommentsByUser_Manager commentsByUserManager;

    @Inject
    CommentsByVideo_Manager commentsByVideoManager;

    @Inject
    EventBus eventBus;

    @Inject
    ExecutorService executorService;

    private Session session;

    @PostConstruct
    public void init(){
        this.session = commentsByUserManager.getNativeSession();
    }

    @Override
    public void commentOnVideo(CommentOnVideoRequest request, StreamObserver<CommentOnVideoResponse> responseObserver) {
        final Date now = new Date();

        final CommentsByVideo commentsByVideo = new CommentsByVideo(request);
        final CommentsByUser commentsByUser = new CommentsByUser(request);

        final BoundStatement bs1 = commentsByUserManager.crud().insert(commentsByUser).generateAndGetBoundStatement();
        final BoundStatement bs2 = commentsByVideoManager.crud().insert(commentsByVideo).generateAndGetBoundStatement();

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
                } else if (ex != null) {
                    responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                }
                return rs;
            });

    }

    @Override
    public void getUserComments(GetUserCommentsRequest request, StreamObserver<GetUserCommentsResponse> responseObserver) {
        final TimeUuid startingCommentId = request.getStartingCommentId();
        final CompletableFuture<Tuple2<List<CommentsByUser>, ExecutionInfo>> future;

        if (startingCommentId == null) {
            future = commentsByUserManager
                    .dsl()
                    .select()
                    .commentid()
                    .videoid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .userid_Eq(fromString(request.getUserId().getValue()))
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
                    .getListAsyncWithStats();

        } else {
            future = commentsByUserManager
                    .dsl()
                    .select()
                    .commentid()
                    .videoid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .userid_Eq(fromString(request.getUserId().getValue()))
                    .commentid_Lte(fromString(request.getStartingCommentId().getValue()))
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
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
            } else if (ex != null) {
                responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
            }
            return tuple2;
        });
    }

    @Override
    public void getVideoComments(GetVideoCommentsRequest request, StreamObserver<GetVideoCommentsResponse> responseObserver) {
        final TimeUuid startingCommentId = request.getStartingCommentId();
        final CompletableFuture<Tuple2<List<CommentsByVideo>, ExecutionInfo>> future;

        if (startingCommentId == null) {
            future = commentsByVideoManager
                    .dsl()
                    .select()
                    .commentid()
                    .userid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .videoid_Eq(fromString(request.getVideoId().getValue()))
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
                    .getListAsyncWithStats();

        } else {
            future = commentsByVideoManager
                    .dsl()
                    .select()
                    .commentid()
                    .userid()
                    .comment()
                    .dateOfComment()
                    .fromBaseTable()
                    .where()
                    .videoid_Eq(fromString(request.getVideoId().getValue()))
                    .commentid_Lte(fromString(request.getStartingCommentId().getValue()))
                    .limit(request.getPageSize())
                    .withOptionalPagingState(request.getPagingState())
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
            } else if (ex != null) {
                responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
            }
            return tuple2;
        });
    }
}
