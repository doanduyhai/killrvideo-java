package killrvideo.service;

import static java.util.UUID.fromString;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

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
    KillrVideoInputValidator validator;

    @Inject
    DseSession dseSession;

    private String commentsByUserTableName;
    private String commentsByVideoTableName;
    private PreparedStatement commentsByUserPrepared;
    private PreparedStatement commentsByVideoPrepared;
    private PreparedStatement getUserComments_startingPointPrepared;
    private PreparedStatement getUserComments_noStartingPointPrepared;
    private PreparedStatement getVideoComments_startingPointPrepared;
    private PreparedStatement getVideoComments_noStartingPointPrepared;

    @PostConstruct
    public void init(){
        /**
         * Set the following up in PostConstruct because 1) we have to
         * wait until after dependency injection for these to work,
         * and 2) we only want to load the prepared statements once at
         * the start of the service.  From here the prepared statements should
         * be cached on our Cassandra nodes.
         *
         * Alrighty, here is a case where I provide prepared statements
         * both with and without using QueryBuilder. The end result is essentially
         * the same and the one you choose largely comes down to style.
         */

        commentsByUserTableName = commentsByUserMapper.getTableMetadata().getName();
        commentsByVideoTableName = commentsByVideoMapper.getTableMetadata().getName();

        // Prepared statements for commentOnVideo()
        commentsByUserPrepared = dseSession.prepare(
                "INSERT INTO " + Schema.KEYSPACE + "." + commentsByUserTableName + " " +
                        "(userid, commentid, comment, videoid) " +
                        "VALUES (?, ?, ?, ?)"
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        commentsByVideoPrepared = dseSession.prepare(
                "INSERT INTO " + Schema.KEYSPACE + "." + commentsByVideoTableName + " " +
                        "(videoid, commentid, comment, userid) " +
                        "VALUES (?, ?, ?, ?)"
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        // Prepared statements for getUserComments()
        /**
         * Notice below I execute fcall() to pull the timestamp out of the
         * commentid timeuuid field, yet I am using the @Computed annotation
         * to do the same thing within the CommentsByUser entity for the dateOfComment
         * field.  I do this because I am using QueryBuilder for the query below.
         * @Computed is only supported when using the mapper stated per
         * http://docs.datastax.com/en/drivers/java/3.2/com/datastax/driver/mapping/annotations/Computed.html.
         * So, I essentially have 2 ways to get the timestamp out of my timeUUID column
         * depending on the type of query I am executing.
         */
        getUserComments_noStartingPointPrepared = dseSession.prepare(
                QueryBuilder
                        .select()
                        .column("userid")
                        .column("commentid")
                        .column("videoid")
                        .column("comment")
                        .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                        .from(Schema.KEYSPACE, commentsByUserTableName)
                        .where(QueryBuilder.eq("userid", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        getUserComments_startingPointPrepared = dseSession.prepare(
                QueryBuilder
                        .select()
                        .column("userid")
                        .column("commentid")
                        .column("videoid")
                        .column("comment")
                        .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                        .from(Schema.KEYSPACE, commentsByUserTableName)
                        .where(QueryBuilder.eq("userid", QueryBuilder.bindMarker()))
                        .and(QueryBuilder.lte("commentid", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        // Prepared statements for getVideoComments()
        getVideoComments_noStartingPointPrepared = dseSession.prepare(
                QueryBuilder
                    .select()
                    .column("videoid")
                    .column("commentid")
                    .column("userid")
                    .column("comment")
                    .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                    .from(Schema.KEYSPACE, commentsByVideoTableName)
                    .where(QueryBuilder.eq("videoid", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);

        getVideoComments_startingPointPrepared = dseSession.prepare(
                QueryBuilder
                        .select()
                        .column("videoid")
                        .column("commentid")
                        .column("userid")
                        .column("comment")
                        .fcall("toTimestamp", QueryBuilder.column("commentid")).as("comment_timestamp")
                        .from(Schema.KEYSPACE, commentsByVideoTableName)
                        .where(QueryBuilder.eq("videoid", QueryBuilder.bindMarker()))
                        .and(QueryBuilder.lte("commentid", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
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

        //:TODO Potential future work to use the mapper with saveAsync()
        BoundStatement bs1 = commentsByUserPrepared.bind(
                userId, commentId, comment, videoId
        );

        BoundStatement bs2 = commentsByVideoPrepared.bind(
                videoId, commentId, comment, userId
        );

        /**
         * We need to insert into comments_by_user and comments_by_video
         * simultaneously, thus using logged batch for automatic retries
         * in case of error
         */
        final BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
        batchStatement.add(bs1);
        batchStatement.add(bs2);
        batchStatement.setDefaultTimestamp(now.getTime());

        FutureUtils.buildCompletableFuture(dseSession.executeAsync(batchStatement))
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
        final Optional<String> pagingState = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        BoundStatement statement;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first user comment as reference point
         */
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
            LOGGER.debug("Query without startingCommentId");
            statement = getUserComments_noStartingPointPrepared.bind()
                    .setUUID("userid", fromString(userId.getValue()));
        }

        /**
         * Subsequent requests always provide startingCommentId to load page
         * of user comments. Fetch size/page size is expected to be > 1
         */
        else {
            LOGGER.debug("Query WITH startingCommentId");
            statement = getUserComments_startingPointPrepared.bind()
                    .setUUID("userid", fromString(userId.getValue()))
                    .setUUID("commentid", fromString(startingCommentId.getValue()));
        }

        statement.setFetchSize(request.getPageSize());

        pagingState.ifPresent( x -> statement.setPagingState(PagingState.fromString(x)));

        FutureUtils.buildCompletableFuture(dseSession.executeAsync(statement))
                .handle((commentResult, ex) -> {
                    try {
                        if (commentResult != null) {
                            final GetUserCommentsResponse.Builder builder = GetUserCommentsResponse.newBuilder();

                            int remaining = commentResult.getAvailableWithoutFetching();
                            for (Row row : commentResult) {
                                CommentsByUser commentByUser = new CommentsByUser(
                                        row.getUUID("userid"), row.getUUID("commentid"),
                                        row.getUUID("videoid"), row.getString("comment")
                                );

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
        final Optional<String> pagingState = Optional
                .ofNullable(request.getPagingState())
                .filter(StringUtils::isNotBlank);

        BoundStatement statement;

        /**
         * Query without startingCommentId to get a reference point
         * Normally, the requested fetch size/page size is 1 to get
         * the first video comment as reference point
         */
        if (startingCommentId == null || isBlank(startingCommentId.getValue())) {
            LOGGER.debug("Query without startingCommentId");
            statement = getVideoComments_noStartingPointPrepared.bind()
                    .setUUID("videoid", fromString(videoId.getValue()));
        }
        /**
         * Subsequent requests always provide startingCommentId to load page
         * of video comments. Fetch size/page size is expected to be > 1
         */
        else {
            LOGGER.debug("Query WITH startingCommentId");
            statement = getVideoComments_startingPointPrepared.bind()
                    .setUUID("videoid", fromString(videoId.getValue()))
                    .setUUID("commentid", fromString(startingCommentId.getValue()));
        }

        statement.setFetchSize(request.getPageSize());

        pagingState.ifPresent( x -> statement.setPagingState(PagingState.fromString(x)));

        FutureUtils.buildCompletableFuture(dseSession.executeAsync(statement))
                .handle((commentResult, ex) -> {
                    try {
                        if (commentResult != null) {
                            final GetVideoCommentsResponse.Builder builder = GetVideoCommentsResponse.newBuilder();

                            int remaining = commentResult.getAvailableWithoutFetching();
                            for (Row row : commentResult) {
                                CommentsByVideo commentByVideo = new CommentsByVideo(
                                        row.getUUID("videoid"), row.getUUID("commentid"),
                                        row.getUUID("userid"), row.getString("comment")
                                );

                                /**
                                 * Explicitly set dateOfComment because I cannot use the @Computed
                                 * annotation set on the dateOfComment field when using QueryBuilder.
                                 * This gives us the "proper" return object expected for the response to the front-end
                                 * UI.  It does not function if this value is null or not the correct type.
                                 */
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
