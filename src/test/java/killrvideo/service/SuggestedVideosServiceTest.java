package killrvideo.service;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static killrvideo.service.SuggestedVideosService.RELATED_VIDEOS_TO_RETURN;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

//import info.archinnov.achilles.generated.ManagerFactory;
//import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.Video;
import killrvideo.entity.VideoByTag;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class SuggestedVideosServiceTest {

    //:TODO Fix this
//    @Rule
//    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
//            .forJunit()
//            .entityClassesToTruncate(Video.class, VideoByTag.class)
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
//    private SuggestedVideosService suggestedVideosService;
//
//    @Before
//    public void setUp() {
//        suggestedVideosService = new SuggestedVideosService();
//        suggestedVideosService.videoManager = resource.getManagerFactory().forVideo();
//        suggestedVideosService.videoByTagManager = resource.getManagerFactory().forVideoByTag();
//        suggestedVideosService.validator = new KillrVideoInputValidator();
//    }
//
//
//    @Test
//    public void should_return_no_result_for_get_related_videos_request() throws Exception {
//        //Given
//        UUID videoId = UUID.randomUUID();
//
//        GetRelatedVideosRequest request = GetRelatedVideosRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetRelatedVideosResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetRelatedVideosResponse> streamObserver = new StreamObserver<GetRelatedVideosResponse>() {
//            @Override
//            public void onNext(GetRelatedVideosResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
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
//        suggestedVideosService.getRelatedVideos(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetRelatedVideosResponse relatedVideosResponse = response.get();
//        assertThat(relatedVideosResponse).isNotNull();
//        assertThat(relatedVideosResponse.getVideosCount()).isEqualTo(0);
//    }
//
//    @Test
//    public void should_return_no_result_for_get_related_videos_request_because_untagged_video() throws Exception {
//        //Given
//        UUID videoId = UUID.randomUUID();
//        resource.getScriptExecutor().executeScriptTemplate("suggestedVideoServices/insertUntaggedVideo.cql", ImmutableMap.of("videoid",videoId));
//
//        GetRelatedVideosRequest request = GetRelatedVideosRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetRelatedVideosResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetRelatedVideosResponse> streamObserver = new StreamObserver<GetRelatedVideosResponse>() {
//            @Override
//            public void onNext(GetRelatedVideosResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
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
//        suggestedVideosService.getRelatedVideos(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetRelatedVideosResponse relatedVideosResponse = response.get();
//        assertThat(relatedVideosResponse).isNotNull();
//        assertThat(relatedVideosResponse.getVideosCount()).isEqualTo(0);
//
//    }
//
//    @Test
//    public void should_get_related_videos_request() throws Exception {
//        //Given
//        Map<String, Object> params = new HashMap<>();
//
//        UUID videoId1 = UUID.randomUUID();
//        UUID videoId2 = UUID.randomUUID();
//        UUID videoId3 = UUID.randomUUID();
//        UUID videoId4 = UUID.randomUUID();
//        UUID videoId5 = UUID.randomUUID();
//        UUID videoId6 = UUID.randomUUID();
//
//        params.put("videoid1", videoId1);
//        params.put("videoid2", videoId2);
//        params.put("videoid3", videoId3);
//        params.put("videoid4", videoId4);
//        params.put("videoid5", videoId5);
//        params.put("videoid6", videoId6);
//
//        resource.getScriptExecutor().executeScriptTemplate("suggestedVideoServices/insertRelatedVideos.cql", params);
//
//        GetRelatedVideosRequest request = GetRelatedVideosRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId1))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetRelatedVideosResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetRelatedVideosResponse> streamObserver = new StreamObserver<GetRelatedVideosResponse>() {
//            @Override
//            public void onNext(GetRelatedVideosResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
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
//        suggestedVideosService.getRelatedVideos(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetRelatedVideosResponse relatedVideosResponse = response.get();
//        assertThat(relatedVideosResponse).isNotNull();
//        assertThat(relatedVideosResponse.getVideosCount()).isEqualTo(5);
//
//        final List<SuggestedVideoPreview> videosList = relatedVideosResponse.getVideosList();
//
//        assertThat(asList(videoId2, videoId3, videoId4, videoId5, videoId6).stream().map(UUID::toString).collect(toList()))
//                .containsAll(videosList.stream().map(SuggestedVideoPreview::getVideoId).map(Uuid::getValue).collect(toList()));
//
//        assertThat(asList("new_lego_collection.mp4", "allied_avenger.mp4", "vintage_toys.mp4", "my_custom_lego_builds.mp4", "brickcon-2016.mp4"))
//                .containsAll(videosList.stream().map(SuggestedVideoPreview::getName).collect(toList()));
//    }
//
//    @Test
//    public void should_validate_get_related_videos_request() throws Exception {
//        //Given
//        GetRelatedVideosRequest request = GetRelatedVideosRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetRelatedVideosResponse> streamObserver = new StreamObserver<GetRelatedVideosResponse>() {
//            @Override
//            public void onNext(GetRelatedVideosResponse value) {
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
//        suggestedVideosService.getRelatedVideos(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("video id");
//    }
//
//    @Test
//    public void should_return_no_result_for_get_suggested_videos_for_user_request() throws Exception {
//        //Given
//        UUID userId = UUID.randomUUID();
//
//        GetSuggestedForUserRequest request = GetSuggestedForUserRequest
//                .newBuilder()
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetSuggestedForUserResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetSuggestedForUserResponse> streamObserver = new StreamObserver<GetSuggestedForUserResponse>() {
//            @Override
//            public void onNext(GetSuggestedForUserResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
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
//        suggestedVideosService.getSuggestedForUser(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetSuggestedForUserResponse suggestedForUserResponse = response.get();
//        assertThat(suggestedForUserResponse).isNotNull();
//        assertThat(suggestedForUserResponse.getVideosCount()).isEqualTo(0);
//    }
}