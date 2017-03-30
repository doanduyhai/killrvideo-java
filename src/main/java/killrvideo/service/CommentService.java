package killrvideo.service;

//import static info.archinnov.achilles.internals.futures.FutureUtils.toCompletableFuture;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.*;
import java.time.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.stream.events.Comment;

import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.ListenableFuture;
import killrvideo.entity.*;
import killrvideo.utils.FutureUtils;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.*;
import com.google.common.eventbus.EventBus;

//import info.archinnov.achilles.generated.manager.CommentsByUser_Manager;
//import info.archinnov.achilles.generated.manager.CommentsByVideo_Manager;
//import info.archinnov.achilles.type.tuples.Tuple2;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceGrpc.AbstractCommentsService;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents.UserCommentedOnVideo;
import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.events.CassandraMutationError;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class CommentService extends AbstractCommentsService {

    private static Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    //:TODO Replace Comments managers
    /*
    @Inject
    CommentsByUser_Manager commentsByUserManager;

    @Inject
    CommentsByVideo_Manager commentsByVideoManager;
    */

    @Inject
    Mapper<CommentsByUser> commentsByUserMapper;

    @Inject
    Mapper<CommentsByVideo> commentsByVideoMapper;

    @Inject
    MappingManager manager;

    @Inject
    EventBus eventBus;

    @Inject
    ExecutorService executorService;

    @Inject
    KillrVideoInputValidator validator;

    Session session;
    private String commentsByUserTableName;
    private String commentsByVideoTableName;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        commentsByUserTableName = commentsByUserMapper.getTableMetadata().getName();
        commentsByVideoTableName = commentsByVideoMapper.getTableMetadata().getName();
    }

    @Override
    public void commentOnVideo(final CommentOnVideoRequest request, StreamObserver<CommentOnVideoResponse> responseObserver) {

        LOGGER.debug("-----Start comment on video request-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Date now = new Date();
        final UUID userId = UUID.fromString(request.getUserId().getValue());
        final UUID videoId = UUID.fromString(request.getVideoId().getValue());
        final UUID commentId = UUID.fromString(request.getCommentId().getValue());
        final String comment = request.getComment();

        //:TODO Fix this
//        final BoundStatement bs1 = commentsByUserManager.crud().insert(commentsByUser).generateAndGetBoundStatement();
//        final BoundStatement bs2 = commentsByVideoManager.crud().insert(commentsByVideo).generateAndGetBoundStatement();

        final Statement s1 = commentsByUserMapper
                .saveQuery(new CommentsByUser(userId, videoId, commentId, comment));

        final Statement s2 = commentsByVideoMapper
                .saveQuery(new CommentsByVideo(videoId, commentId, userId, comment));

        /**
         * We need to insert into comments_by_user and comments_by_video
         * simultaneously, thus using logged batch for automatic retries
         * in case of error
         */

        //:TODO Fix this
        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(s1);
        batchStatement.add(s2);
        batchStatement.setDefaultTimestamp(now.getTime());

        FutureUtils.buildCompletableFuture(manager.getSession().executeAsync(batchStatement))
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

        //:TODO Fix this
        final TimeUuid startingCommentId = request.getStartingCommentId();
        //final CompletableFuture<Tuple2<List<CommentsByUser>, ExecutionInfo>> future;
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);
        ResultSetFuture future;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first user comment as reference point
         */
        //TODO: Fix this
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
//            future = commentsByUserManager
//                    .dsl()
//                    .select()
//                    .commentid()
//                    .videoid()
//                    .comment()
//                    .dateOfComment()
//                    .fromBaseTable()
//                    .where()
//                    .userid().Eq(fromString(request.getUserId().getValue()))
//                    .withFetchSize(request.getPageSize())
//                    .withOptionalPagingStateString(pagingStateString)
//                    .getListAsyncWithStats();

            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("commentid")
                    .column("videoid")
                    .column("comment")
                    .from(Schema.KEYSPACE, commentsByUserTableName)
                    .where(QueryBuilder.eq("userid", fromString(request.getUserId().getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            //:TODO Figure out a more streamlined way to do this with Optional and java 8 lambada
            if (pagingStateString.isPresent()) {
                statement.setPagingState(PagingState.fromString(pagingStateString.get()));
            }

            future = session.executeAsync(statement);
        }

        /**
         * Subsequent requests always provide startingCommentId to load page
         * of user comments. Fetch size/page size is expected to be > 1
         */
        //:TODO Fix this
        else {
//            future = commentsByUserManager
//                    .dsl()
//                    .select()
//                    .commentid()
//                    .videoid()
//                    .comment()
//                    .dateOfComment()
//                    .fromBaseTable()
//                    .where()
//                    .userid().Eq(fromString(request.getUserId().getValue()))
//                    .commentid().Lte(fromString(request.getStartingCommentId().getValue()))
//                    .withFetchSize(request.getPageSize())
//                    .getListAsyncWithStats();

            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("commentid")
                    .column("videoid")
                    .column("comment")
                    .from(Schema.KEYSPACE, commentsByUserTableName)
                    .where(QueryBuilder.eq("userid", fromString(request.getUserId().getValue())))
                    .and(QueryBuilder.lte("commentid", fromString(request.getStartingCommentId().getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            future = session.executeAsync(statement);
        }

        FutureUtils.buildCompletableFuture(future)
                .handle((commentResult, ex) -> {
                    Result<CommentsByUser> comments = commentsByUserMapper.map(commentResult);

                    if(comments != null) {
                        final GetUserCommentsResponse.Builder builder = GetUserCommentsResponse.newBuilder();
                        comments.all().forEach(commentsByUser-> builder.addComments(commentsByUser.toUserComment()));
                        Optional.ofNullable(comments.getExecutionInfo().getPagingState())
                                .map(PagingState::toString)
                                .ifPresent(builder::setPagingState);
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End get user comments request");

                    } else if (ex != null) {
                        LOGGER.error("Exception getting user comments : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return commentResult;
                });
    }

    @Override
    public void getVideoComments(GetVideoCommentsRequest request, StreamObserver<GetVideoCommentsResponse> responseObserver) {

        LOGGER.debug("-----Start get video comments request-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final TimeUuid startingCommentId = request.getStartingCommentId();
        //final CompletableFuture<Tuple2<List<CommentsByVideo>, ExecutionInfo>> future;
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);
        LOGGER.debug("Paging state is: " + pagingStateString.toString());

        TupleType tuple2Type = session.getCluster().getMetadata()
                .newTupleType(DataType.list(
                        DataType.custom("CommentsByVideo")),
                        DataType.custom("ExecutionInfo"));

        final ResultSetFuture future;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first video comment as reference point
         */
        //:TODO Fix this
        if (startingCommentId == null || isBlank(request.getStartingCommentId().getValue())) {
            /*future = commentsByVideoMapper
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
                    .getListAsyncWithStats();*/

            LOGGER.debug("Query without startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    //.all()
                    .column("videoid")
                    .column("commentid")
                    .column("userid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByVideoTableName)
                    .where(QueryBuilder.eq("videoid", fromString(request.getVideoId().getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            //:TODO Figure out a more streamlined way to do this with Optional and java 8 lambada
            if (pagingStateString.isPresent()) {
                statement.setPagingState(PagingState.fromString(pagingStateString.get()));
            }

            future = session.executeAsync(statement);

        }
        /**
         * Subsequent requests always provide startingCommentId to load page
         * of video comments. Fetch size/page size is expected to be > 1
         */
        else {
//            future = commentsByVideoMapper
//                    .dsl()
//                    .select()
//                    .commentid()
//                    .userid()
//                    .comment()
//                    .dateOfComment()
//                    .fromBaseTable()
//                    .where()
//                    .videoid().Eq(fromString(request.getVideoId().getValue()))
//                    .commentid().Lte(fromString(request.getStartingCommentId().getValue()))
//                    .withFetchSize(request.getPageSize())
//                    .getListAsyncWithStats();

            LOGGER.debug("Query WITH startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    //.all()
                    .column("videoid")
                    .column("commentid")
                    .column("userid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByVideoTableName)
                    .where(QueryBuilder.eq("videoid", fromString(request.getVideoId().getValue())))
                    .and(QueryBuilder.lte("commentid", fromString(request.getStartingCommentId().getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            future = session.executeAsync(statement);
        }

        //CompletableFuture<List<CommentsByVideo>, ExecutionInfo> test =  FutureUtils.buildCompletableFuture(future);
        //CompletableFuture<Tuple2<List<CommentsByVideo>, ExecutionInfo>> futureTest =

        FutureUtils.buildCompletableFuture(future)
        .handle((commentResult, ex) -> {
            try {
                if (commentResult != null && !commentResult.isExhausted()) {
                    final List<CommentsByVideo> commentsList = new ArrayList<>();

                    List<Row> rows = commentResult.all();
                    final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();
                    //Result<CommentsByVideo> comments = commentsByVideoMapper.map(commentResult);
//                    for (CommentsByVideo comment : comments.all()) {
//                        builder.addComments(comment.toVideoComment());
//                        LOGGER.debug(comment.getComment());
//                    }
////
                    for (Row row : rows) {
                        CommentsByVideo wtf = new CommentsByVideo(
                                row.getUUID(0), row.getUUID(1), row.getUUID(2), row.getString(3)
                        );

                        //Date date = row.getTimestamp(4);
                        wtf.setDateOfComment(row.getTimestamp(4));
                        commentsList.add(wtf);
                        LOGGER.debug(wtf.getComment());
                        LOGGER.debug("DATEOF: " + wtf.getDateOfComment());

                        builder.addComments(wtf.toVideoComment());
                    }
//
//
//                    //final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();
//                    //commentsList.forEach(comment -> builder.addComments(comment.toVideoComment()));
//                    //comments.all().stream().forEach(comment -> builder.addComments(comment.toVideoComment()));
//                    //comments.forEach(commentsByVideo -> builder.addComments(commentsByVideo.toVideoComment()));
//
                    Optional.ofNullable(commentResult.getExecutionInfo().getPagingState())
                            .map(PagingState::toString)
                            .ifPresent(builder::setPagingState);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();

                    LOGGER.debug("End get video comments request");

                } else if (ex != null) {

                    LOGGER.error("Exception getting video comments : " + mergeStackTrace(ex));

                    responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                }

            } catch (Exception exception) {
                LOGGER.error("CATCH Exception getting video comments : " + mergeStackTrace(exception));
            }

                return commentResult;
        });
    }
}
