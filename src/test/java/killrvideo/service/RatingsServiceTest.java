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

import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.Row;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;

import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass;
import killrvideo.comments.events.CommentsEvents;
import killrvideo.entity.CommentsByUser;
import killrvideo.entity.CommentsByVideo;
import killrvideo.entity.VideoRating;
import killrvideo.entity.VideoRatingByUser;
import killrvideo.ratings.RatingsServiceOuterClass;
import killrvideo.ratings.RatingsServiceOuterClass.*;
import killrvideo.ratings.events.RatingsEvents;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class RatingsServiceTest {

    @Rule
    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
            .forJunit()
            .entityClassesToTruncate(VideoRating.class, VideoRatingByUser.class)
            .truncateBeforeAndAfterTest()
            .createAndUseKeyspace("killrvideo")
            .build((cluster, statementsCache) -> ManagerFactoryBuilder
                    .builder(cluster)
                    .doForceSchemaCreation(true)
                    .withStatementsCache(statementsCache)
                    .withBeanValidation(true)
                    .withPostLoadBeanValidation(true)
                    .build()
            );

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<UserRatedVideo> userRatedVideoCaptor = ArgumentCaptor.forClass(UserRatedVideo.class);

    private RatingsService ratingsService;

    @Before
    public void setUp() {
        ratingsService = new RatingsService();
        ratingsService.ratingManager = resource.getManagerFactory().forVideoRating();
        ratingsService.ratingByUserManager= resource.getManagerFactory().forVideoRatingByUser();
        ratingsService.eventBus = eventBus;
        ratingsService.validator = new KillrVideoInputValidator();
    }

    @Test
    public void should_rate_video() throws Exception {
        //Given
        UUID videoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RateVideoRequest request = RateVideoRequest.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoId))
                .setUserId(TypeConverter.uuidToUuid(userId))
                .setRating(11)
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<RateVideoResponse> response = new AtomicReference<>(null);

        StreamObserver<RateVideoResponse> streamObserver = new StreamObserver<RateVideoResponse>() {
            @Override
            public void onNext(RateVideoResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };
        //When
        ratingsService.rateVideo(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        assertThat(response.get()).isNotNull();

        final List<Row> rows = resource.getNativeSession().execute("SELECT * FROM killrvideo.video_ratings").all();

        assertThat(rows).isNotEmpty().hasSize(1);
        final Row row = rows.get(0);
        assertThat(row.getUUID("videoid")).isEqualTo(videoId);
        assertThat(row.getLong("rating_counter")).isEqualTo(1L);
        assertThat(row.getLong("rating_total")).isEqualTo(11L);

        verify(eventBus).post(userRatedVideoCaptor.capture());

        final UserRatedVideo userRatedVideo = userRatedVideoCaptor.getValue();

        assertThat(userRatedVideo).isNotNull();
        assertThat(userRatedVideo.getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(userRatedVideo.getVideoId().getValue()).isEqualTo(videoId.toString());
        assertThat(userRatedVideo.getRating()).isEqualTo(11);
    }

    @Test
    public void should_validate_rate_video_request() throws Exception {
        //Given
        RateVideoRequest request = RateVideoRequest.newBuilder().build();

        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<RateVideoResponse> streamObserver = new StreamObserver<RateVideoResponse>() {
            @Override
            public void onNext(RateVideoResponse value) {

            }

            @Override
            public void onError(Throwable t) {
                error.getAndSet(t);
            }

            @Override
            public void onCompleted() {
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.rateVideo(request, streamObserver);

        //Then
        assertThat(error.get()).isNotNull();
        assertThat(error.get().getMessage()).contains("video id");
        assertThat(error.get().getMessage()).contains("user id");
    }

    @Test
    public void should_get_video_rating() throws Exception {
        //Given
        UUID videoId = UUID.randomUUID();
        Map<String, Object> params = new HashMap<>();
        params.put("videoid", videoId);

        GetRatingRequest request = GetRatingRequest.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoId))
                .build();

        resource.getScriptExecutor().executeScriptTemplate("ratingsService/rateVideo.cql", params);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<GetRatingResponse> response = new AtomicReference<>(null);

        StreamObserver<GetRatingResponse> streamObserver = new StreamObserver<GetRatingResponse>() {
            @Override
            public void onNext(GetRatingResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getRating(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final GetRatingResponse ratingResponse = response.get();

        assertThat(ratingResponse).isNotNull();
        assertThat(ratingResponse.getRatingsCount()).isEqualTo(5L);
        assertThat(ratingResponse.getRatingsTotal()).isEqualTo(123L);
        assertThat(ratingResponse.getVideoId().getValue()).isEqualTo(videoId.toString());
    }

    @Test
    public void should_return_0_for_video_rating_if_not_found() throws Exception {
        //Given
        UUID videoId = UUID.randomUUID();
        GetRatingRequest request = GetRatingRequest.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoId))
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<GetRatingResponse> response = new AtomicReference<>(null);

        StreamObserver<GetRatingResponse> streamObserver = new StreamObserver<GetRatingResponse>() {
            @Override
            public void onNext(GetRatingResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getRating(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final GetRatingResponse ratingResponse = response.get();

        assertThat(ratingResponse).isNotNull();
        assertThat(ratingResponse.getRatingsCount()).isEqualTo(0L);
        assertThat(ratingResponse.getRatingsTotal()).isEqualTo(0L);
        assertThat(ratingResponse.getVideoId().getValue()).isEqualTo(videoId.toString());
    }

    @Test
    public void should_validate_get_video_rating_request() throws Exception {
        //Given
        GetRatingRequest request = GetRatingRequest.newBuilder().build();

        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<GetRatingResponse> streamObserver = new StreamObserver<GetRatingResponse>() {
            @Override
            public void onNext(GetRatingResponse value) {

            }

            @Override
            public void onError(Throwable t) {
                error.getAndSet(t);
            }

            @Override
            public void onCompleted() {
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getRating(request, streamObserver);

        //Then
        assertThat(error.get()).isNotNull();
        assertThat(error.get().getMessage()).contains("video id");
    }

    @Test
    public void should_get_user_rating() throws Exception {
        //Given
        UUID videoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int rating = 12;

        Map<String, Object> params = new HashMap<>();
        params.put("videoid", videoId);
        params.put("userid", userId);
        params.put("rating", rating);

        GetUserRatingRequest request = GetUserRatingRequest.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoId))
                .setUserId(TypeConverter.uuidToUuid(userId))
                .build();

        resource.getScriptExecutor().executeScriptTemplate("ratingsService/rateVideoByUser.cql", params);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<GetUserRatingResponse> response = new AtomicReference<>(null);

        StreamObserver<GetUserRatingResponse> streamObserver = new StreamObserver<GetUserRatingResponse>() {
            @Override
            public void onNext(GetUserRatingResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getUserRating(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final GetUserRatingResponse ratingResponse = response.get();

        assertThat(ratingResponse).isNotNull();
        assertThat(ratingResponse.getRating()).isEqualTo(rating);
        assertThat(ratingResponse.getVideoId().getValue()).isEqualTo(videoId.toString());
        assertThat(ratingResponse.getUserId().getValue()).isEqualTo(userId.toString());
    }

    @Test
    public void should_return_0_for_user_rating_if_not_found() throws Exception {
        //Given
        UUID videoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GetUserRatingRequest request = GetUserRatingRequest.newBuilder()
                .setVideoId(TypeConverter.uuidToUuid(videoId))
                .setUserId(TypeConverter.uuidToUuid(userId))
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<GetUserRatingResponse> response = new AtomicReference<>(null);

        StreamObserver<GetUserRatingResponse> streamObserver = new StreamObserver<GetUserRatingResponse>() {
            @Override
            public void onNext(GetUserRatingResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getUserRating(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final GetUserRatingResponse ratingResponse = response.get();

        assertThat(ratingResponse).isNotNull();
        assertThat(ratingResponse.getRating()).isEqualTo(0);
        assertThat(ratingResponse.getVideoId().getValue()).isEqualTo(videoId.toString());
        assertThat(ratingResponse.getUserId().getValue()).isEqualTo(userId.toString());
    }

    @Test
    public void should_validate_get_video_rating_by_user_request() throws Exception {
        //Given
        GetUserRatingRequest request = GetUserRatingRequest.newBuilder().build();

        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<GetUserRatingResponse> streamObserver = new StreamObserver<GetUserRatingResponse>() {
            @Override
            public void onNext(GetUserRatingResponse value) {

            }

            @Override
            public void onError(Throwable t) {
                error.getAndSet(t);
            }

            @Override
            public void onCompleted() {
                completed.getAndSet(true);
            }
        };

        //When
        ratingsService.getUserRating(request, streamObserver);

        //Then
        assertThat(error.get()).isNotNull();
        assertThat(error.get().getMessage()).contains("video id");
        assertThat(error.get().getMessage()).contains("user id");
    }
}