package killrvideo.service;

import static java.util.UUID.fromString;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.*;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

import killrvideo.common.CommonTypes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import killrvideo.comments.CommentsServiceGrpc.AbstractCommentsService;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents.UserCommentedOnVideo;
import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.CommentsByVideo;
import killrvideo.entity.CommentsByUser;
import killrvideo.entity.Schema;
import killrvideo.events.CassandraMutationError;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.utils.FutureUtils;


@Service
public class CommentService extends AbstractCommentsService {

    private static Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

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

        final Statement s1 = commentsByUserMapper
                .saveQuery(new CommentsByUser(userId, commentId, videoId, comment));

        final Statement s2 = commentsByVideoMapper
                .saveQuery(new CommentsByVideo(videoId, commentId, userId, comment));

        /**
         * We need to insert into comments_by_user and comments_by_video
         * simultaneously, thus using logged batch for automatic retries
         * in case of error
         */
        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(s1);
        batchStatement.add(s2);
        batchStatement.setDefaultTimestamp(now.getTime());

        FutureUtils.buildCompletableFuture(manager.getSession().executeAsync(batchStatement))
            .handle((rs, ex) -> {
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
        final Uuid userId = request.getUserId();
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        ResultSetFuture future;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first user comment as reference point
         */
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
            LOGGER.debug("Query without startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("userid")
                    .column("commentid")
                    .column("videoid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByUserTableName)
                    .where(QueryBuilder.eq("userid", fromString(userId.getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            //:TODO Figure out a more streamlined way to do this with Optional and java 8 lambda
            if (pagingStateString.isPresent()) {
                statement.setPagingState(PagingState.fromString(pagingStateString.get()));
            }

            future = session.executeAsync(statement);
        }

        /**
         * Subsequent requests always provide startingCommentId to load page
         * of user comments. Fetch size/page size is expected to be > 1
         */
        else {
            LOGGER.debug("Query WITH startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("userid")
                    .column("commentid")
                    .column("videoid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByUserTableName)
                    .where(QueryBuilder.eq("userid", fromString(userId.getValue())))
                    .and(QueryBuilder.lte("commentid", fromString(startingCommentId.getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            future = session.executeAsync(statement);
        }

        FutureUtils.buildCompletableFuture(future)
                .handle((commentResult, ex) -> {
                    try {
                        if (commentResult != null) {
                            final GetUserCommentsResponse.Builder builder = GetUserCommentsResponse.newBuilder();

                            //:TODO See if there is a proper way to handle @Computed 1) within the entity itself and 2) for the mapper
                            int remaining = commentResult.getAvailableWithoutFetching();
                            for (Row row : commentResult) {
                                CommentsByUser commentByUser = new CommentsByUser(
                                        row.getUUID("userid"), row.getUUID("commentid"),
                                        row.getUUID("videoid"), row.getString("comment")
                                );

                                /**
                                 * Explicitly set dateOfComment because it is not in the constructor.
                                 * This gives us the "proper" return object for the response to the front-end
                                 * UI.  It does not function if this value is null or not the correct type.
                                 */
                                commentByUser.setDateOfComment(row.getTimestamp("comment_timestamp"));
                                builder.addComments(commentByUser.toUserComment());

                                if (--remaining == 0) {
                                    break;
                                }
                            }

                            Optional.ofNullable(commentResult.getExecutionInfo().getPagingState())
                                    .map(PagingState::toString)
                                    .ifPresent(builder::setPagingState);
                            responseObserver.onNext(builder.build());
                            responseObserver.onCompleted();

                            LOGGER.debug("End get user comments request");

                        } else if (ex != null) {
                            LOGGER.error("Exception getting user comments : " + mergeStackTrace(ex));

                            responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                        }

                    } catch (Exception exception) {
                        LOGGER.error("CATCH Exception getting user comments : " + mergeStackTrace(ex));

                    }

                    return commentResult;

                });
    }

    @Override
    public void getVideoComments(GetVideoCommentsRequest request, StreamObserver<GetVideoCommentsResponse> responseObserver) {

        LOGGER.debug("Start get video comments request");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final TimeUuid startingCommentId = request.getStartingCommentId();
        final Uuid videoId = request.getVideoId();
        final Optional<String> pagingStateString = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        final ResultSetFuture future;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first video comment as reference point
         */
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
            LOGGER.debug("Query without startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("videoid")
                    .column("commentid")
                    .column("userid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByVideoTableName)
                    .where(QueryBuilder.eq("videoid", fromString(videoId.getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            //:TODO Figure out a more streamlined way to do this with Optional and java 8 lambda
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
            /**
             * Notice below I have a fcall to pull the timstamp out of the
             * commentid timeuuid field.  This should be handled by the @Computed
             * annotation in the CommentsByVideo entity, but it seems these only work with
             * simple "get" statements per
             * http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/mapping/annotations/Computed.html
             */
            //:TODO See if maybe I am simply pulling "dateOfComment" incorrectly when using @Computed
            LOGGER.debug("Query WITH startingCommentId");
            BuiltStatement statement = QueryBuilder
                    .select()
                    .column("videoid")
                    .column("commentid")
                    .column("userid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByVideoTableName)
                    .where(QueryBuilder.eq("videoid", fromString(videoId.getValue())))
                    .and(QueryBuilder.lte("commentid", fromString(startingCommentId.getValue())));

            statement
                    .setFetchSize(request.getPageSize());

            future = session.executeAsync(statement);
        }

        FutureUtils.buildCompletableFuture(future)
        .handle((commentResult, ex) -> {
            try {
                if (commentResult != null) {
                    final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();

                    /**
                     * This.....is not how I planned to do this, but it seems the mapper
                     * does not work when dealing with @Computed types as we are within the
                     * CommentsByVideo entity for dateOfComment nor does the constructor
                     * include dateOfComment.  I might simply change the constructor to include
                     * dateOfComment, but this flies against using @Computed in the first place.
                     * For now, this works.
                     */
                    //:TODO See if there is a proper way to handle @Computed 1) within the entity itself and 2) for the mapper
                    int remaining = commentResult.getAvailableWithoutFetching();
                    for (Row row : commentResult) {
                        CommentsByVideo commentByVideo = new CommentsByVideo(
                                row.getUUID("videoid"), row.getUUID("commentid"),
                                row.getUUID("userid"), row.getString("comment")
                        );

                        /**
                         * Explicitly set dateOfComment because it is not in the constructor.
                         * This gives us the "proper" return object for the response to the front-end
                         * UI.  It does not function if this value is null or not the correct type.
                         *
                         * Make sure to comment on THIS!!!
                         * https://docs.datastax.com/en/developer/java-driver/3.1/manual/paging/
                         */
                        //:TODO ensure to comment on the paging link as stated above
                        commentByVideo.setDateOfComment(row.getTimestamp("comment_timestamp"));
                        builder.addComments(commentByVideo.toVideoComment());

                        if (--remaining == 0) {
                            break;
                        }
                    }

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
