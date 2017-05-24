package killrvideo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;

//import info.archinnov.achilles.generated.ManagerFactory;
//import info.archinnov.achilles.generated.ManagerFactoryBuilder;
//import info.archinnov.achilles.junit.AchillesTestResource;
//import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import info.archinnov.achilles.type.TypedMap;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass.*;
import killrvideo.comments.events.CommentsEvents.UserCommentedOnVideo;
import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.CommentsByUser;
import killrvideo.entity.CommentsByVideo;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceTest {

    //:TODO Fix this

//    @Rule
//    public AchillesTestResource<ManagerFactory> resource = AchillesTestResourceBuilder
//            .forJunit()
//            .entityClassesToTruncate(CommentsByUser.class, CommentsByVideo.class)
//            .truncateBeforeAndAfterTest()
//            .createAndUseKeyspace("killrvideo")
//            .build((cluster, statementsCache) -> ManagerFactoryBuilder
//                    .builder(cluster)
//                    .doForceSchemaCreation(true)
//                    .withStatementsCache(statementsCache)
//                    .withBeanValidation(true)
//                    .withPostLoadBeanValidation(true)
//                    .build()
//            );
//
//
//    @Mock
//    private EventBus eventBus;
//
//    @Captor
//    private ArgumentCaptor<UserCommentedOnVideo> userCommentedOnVideoCaptor = ArgumentCaptor.forClass(UserCommentedOnVideo.class);
//
//    private CommentService commentService;
//
//    @Before
//    public void setUp() {
//        commentService = new CommentService();
//        commentService.commentsByUserManager = resource.getManagerFactory().forCommentsByUser();
//        commentService.commentsByVideoManager = resource.getManagerFactory().forCommentsByVideo();
//        commentService.init();
//        commentService.executorService = MoreExecutors.newDirectExecutorService();
//        commentService.eventBus = eventBus;
//        commentService.validator = new KillrVideoInputValidator();
//    }
//
//    @Test
//    public void should_comment_on_video() throws Exception {
//        //Given
//        final String commentTimeUUID = UUIDs.timeBased().toString();
//        final String userId = UUID.randomUUID().toString();
//        final String videoId = UUID.randomUUID().toString();
//        final CommentOnVideoRequest request = CommentOnVideoRequest.newBuilder()
//                .setComment("test comment")
//                .setCommentId(TimeUuid.newBuilder().setValue(commentTimeUUID).build())
//                .setUserId(Uuid.newBuilder().setValue(userId).build())
//                .setVideoId(Uuid.newBuilder().setValue(videoId).build())
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<CommentOnVideoResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<CommentOnVideoResponse> streamObserver = new StreamObserver<CommentOnVideoResponse>() {
//            @Override
//            public void onNext(CommentOnVideoResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.commentOnVideo(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        assertThat(response.get()).isNotNull();
//
//        final List<Row> allCommentsByUser = resource.getNativeSession().execute("SELECT * FROM killrvideo.comments_by_user").all();
//        assertThat(allCommentsByUser).isNotNull().isNotEmpty().hasSize(1);
//
//        final Row commentByUser= allCommentsByUser.get(0);
//        assertThat(commentByUser.getString("comment")).isEqualTo("test comment");
//        assertThat(commentByUser.getUUID("userId").toString()).isEqualTo(userId);
//        assertThat(commentByUser.getUUID("videoId").toString()).isEqualTo(videoId);
//
//        final List<Row> allCommentsByVideo = commentService.session.execute("SELECT * FROM killrvideo.comments_by_video").all();
//        assertThat(allCommentsByVideo).isNotNull().isNotEmpty().hasSize(1);
//
//        final Row commentByVideo = allCommentsByVideo.get(0);
//        assertThat(commentByVideo.getString("comment")).isEqualTo("test comment");
//        assertThat(commentByVideo.getUUID("userId").toString()).isEqualTo(userId);
//        assertThat(commentByVideo.getUUID("videoId").toString()).isEqualTo(videoId);
//
//        verify(eventBus).post(userCommentedOnVideoCaptor.capture());
//        final UserCommentedOnVideo userCommentedOnVideo = userCommentedOnVideoCaptor.getValue();
//
//        assertThat(userCommentedOnVideo).isNotNull();
//        assertThat(userCommentedOnVideo.getCommentId().getValue()).isEqualTo(commentTimeUUID);
//        assertThat(userCommentedOnVideo.getUserId().getValue()).isEqualTo(userId);
//        assertThat(userCommentedOnVideo.getVideoId().getValue()).isEqualTo(videoId);
//        assertThat(userCommentedOnVideo.getCommentTimestamp()).isNotNull();
//    }
//
//    @Test
//    public void should_validate_comment_on_video_request() throws Exception {
//        //Given
//        CommentOnVideoRequest request = CommentOnVideoRequest.newBuilder().build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<CommentOnVideoResponse> streamObserver = new StreamObserver<CommentOnVideoResponse>() {
//            @Override
//            public void onNext(CommentOnVideoResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.commentOnVideo(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("user id");
//        assertThat(error.get().getMessage()).contains("video id");
//        assertThat(error.get().getMessage()).contains("comment id");
//        assertThat(error.get().getMessage()).contains("comment text");
//    }
//
//    /*****************
//     *               *
//     * USER COMMENTS *
//     *               *
//     *****************/
//    @Test
//    public void should_get_user_comments_without_starting_comment_id() throws Exception {
//        //Given
//        generate5UserComments();
//        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder()
//                .setPageSize(3)
//                .setUserId(TypeConverter.uuidToUuid(UUID.fromString("00000000-1111-0000-0000-000000000000")))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserCommentsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserCommentsResponse> streamObserver = new StreamObserver<GetUserCommentsResponse>() {
//            @Override
//            public void onNext(GetUserCommentsResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getUserComments(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetUserCommentsResponse userCommentsResponse = response.get();
//        assertThat(userCommentsResponse).isNotNull();
//
//        final List<UserComment> comments = userCommentsResponse.getCommentsList();
//        assertThat(comments).isNotEmpty().hasSize(3);
//        assertThat(comments.get(0).getComment()).isEqualTo("comment5");
//        assertThat(comments.get(1).getComment()).isEqualTo("comment4");
//        assertThat(comments.get(2).getComment()).isEqualTo("comment3");
//
//        assertThat(userCommentsResponse.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_get_user_comments_with_starting_comment_id() throws Exception {
//        //Given
//        TypedMap params = generate5UserComments();
//
//        final SimpleStatement statement = new SimpleStatement("SELECT * FROM killrvideo.comments_by_user");
//        statement.setFetchSize(2);
//        final ResultSet rs = commentService.session.execute(statement);
//        final String pagingState = rs.getExecutionInfo().getPagingState().toString();
//
//        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder()
//                .setPageSize(2)
//                .setUserId(TypeConverter.uuidToUuid(UUID.fromString("00000000-1111-0000-0000-000000000000")))
//                .setPagingState(pagingState)
//                .setStartingCommentId(TypeConverter.uuidToTimeUuid(params.<UUID>getTyped("commentId4")))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserCommentsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserCommentsResponse> streamObserver = new StreamObserver<GetUserCommentsResponse>() {
//            @Override
//            public void onNext(GetUserCommentsResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getUserComments(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetUserCommentsResponse userCommentsResponse = response.get();
//        assertThat(userCommentsResponse).isNotNull();
//
//        final List<UserComment> comments = userCommentsResponse.getCommentsList();
//        assertThat(comments).isNotEmpty().hasSize(2);
//        assertThat(comments.get(0).getComment()).isEqualTo("comment4");
//        assertThat(comments.get(1).getComment()).isEqualTo("comment3");
//
//        assertThat(userCommentsResponse.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_validate_get_user_comments_request() throws Exception {
//        //Given
//        GetUserCommentsRequest request = GetUserCommentsRequest.newBuilder().build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetUserCommentsResponse> streamObserver = new StreamObserver<GetUserCommentsResponse>() {
//            @Override
//            public void onNext(GetUserCommentsResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getUserComments(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("user id");
//        assertThat(error.get().getMessage()).contains("page size");
//    }
//
//    /******************
//     *                *
//     * VIDEO COMMENTS *
//     *                *
//     ******************/
//    @Test
//    public void should_get_video_comments_without_starting_comment_id() throws Exception {
//        //Given
//        generate5VideoComments();
//        GetVideoCommentsRequest request = GetVideoCommentsRequest.newBuilder()
//                .setPageSize(3)
//                .setVideoId(TypeConverter.uuidToUuid(UUID.fromString("00000000-1111-0000-0000-000000000000")))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetVideoCommentsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoCommentsResponse> streamObserver = new StreamObserver<GetVideoCommentsResponse>() {
//            @Override
//            public void onNext(GetVideoCommentsResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getVideoComments(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetVideoCommentsResponse videoCommentsResponse = response.get();
//        assertThat(videoCommentsResponse).isNotNull();
//
//        final List<VideoComment> comments = videoCommentsResponse.getCommentsList();
//        assertThat(comments).isNotEmpty().hasSize(3);
//        assertThat(comments.get(0).getComment()).isEqualTo("comment5");
//        assertThat(comments.get(1).getComment()).isEqualTo("comment4");
//        assertThat(comments.get(2).getComment()).isEqualTo("comment3");
//
//        assertThat(videoCommentsResponse.getPagingState()).isNotEmpty();
//    }
//
//
//    @Test
//    public void should_get_video_comments_with_starting_comment_id() throws Exception {
//        //Given
//        TypedMap params = generate5VideoComments();
//
//        final SimpleStatement statement = new SimpleStatement("SELECT * FROM killrvideo.comments_by_video");
//        statement.setFetchSize(2);
//        final ResultSet rs = commentService.session.execute(statement);
//        final String pagingState = rs.getExecutionInfo().getPagingState().toString();
//
//        GetVideoCommentsRequest request = GetVideoCommentsRequest.newBuilder()
//                .setPageSize(2)
//                .setVideoId(TypeConverter.uuidToUuid(UUID.fromString("00000000-1111-0000-0000-000000000000")))
//                .setPagingState(pagingState)
//                .setStartingCommentId(TypeConverter.uuidToTimeUuid(params.<UUID>getTyped("commentId4")))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetVideoCommentsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoCommentsResponse> streamObserver = new StreamObserver<GetVideoCommentsResponse>() {
//            @Override
//            public void onNext(GetVideoCommentsResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getVideoComments(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetVideoCommentsResponse videoCommentsResponse = response.get();
//        assertThat(videoCommentsResponse).isNotNull();
//
//        final List<VideoComment> comments = videoCommentsResponse.getCommentsList();
//        assertThat(comments).isNotEmpty().hasSize(2);
//        assertThat(comments.get(0).getComment()).isEqualTo("comment4");
//        assertThat(comments.get(1).getComment()).isEqualTo("comment3");
//
//        assertThat(videoCommentsResponse.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_validate_get_video_comments_request() throws Exception {
//        //Given
//        GetVideoCommentsRequest request = GetVideoCommentsRequest.newBuilder().build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetVideoCommentsResponse> streamObserver = new StreamObserver<GetVideoCommentsResponse>() {
//            @Override
//            public void onNext(GetVideoCommentsResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        commentService.getVideoComments(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("video id");
//        assertThat(error.get().getMessage()).contains("page size");
//    }
//
//    private TypedMap generate5UserComments() throws InterruptedException {
//        Map<String, Object> params = generate5TimeUUIDs();
//        resource.getScriptExecutor().executeScriptTemplate("commentService/generate_5_user_comments.cql", params);
//        return TypedMap.fromMap(params);
//    }
//
//    private TypedMap generate5VideoComments() throws InterruptedException {
//        Map<String, Object> params = generate5TimeUUIDs();
//        resource.getScriptExecutor().executeScriptTemplate("commentService/generate_5_video_comments.cql", params);
//        return TypedMap.fromMap(params);
//    }
//
//    private Map<String, Object> generate5TimeUUIDs() throws InterruptedException {
//        Map<String, Object> params = new HashMap<>();
//        UUID commentId1 = UUIDs.timeBased();
//        Thread.sleep(1);
//        UUID commentId2 = UUIDs.timeBased();
//        Thread.sleep(1);
//        UUID commentId3 = UUIDs.timeBased();
//        Thread.sleep(1);
//        UUID commentId4 = UUIDs.timeBased();
//        Thread.sleep(1);
//        UUID commentId5 = UUIDs.timeBased();
//
//        params.put("commentId1", commentId1);
//        params.put("commentId2", commentId2);
//        params.put("commentId3", commentId3);
//        params.put("commentId4", commentId4);
//        params.put("commentId5", commentId5);
//        return params;
//    }
}