package killrvideo.service;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;

//import info.archinnov.achilles.generated.ManagerFactory;
//import info.archinnov.achilles.generated.ManagerFactoryBuilder;
//import info.archinnov.achilles.junit.AchillesTestResource;
//import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import info.archinnov.achilles.type.tuples.Tuple2;
import info.archinnov.achilles.type.tuples.Tuple3;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.LatestVideos;
import killrvideo.entity.UserVideos;
import killrvideo.entity.Video;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.*;
import killrvideo.video_catalog.events.VideoCatalogEvents.UploadedVideoAccepted;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

@RunWith(MockitoJUnitRunner.class)
public class VideoCatalogServiceTest {

    //:TODO Fix this

//    @Rule
//    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
//            .forJunit()
//            .entityClassesToTruncate(Video.class, UserVideos.class, LatestVideos.class)
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
//    private VideoCatalogService videoCatalogService;
//
//    @Mock
//    EventBus eventBus;
//
//    @Captor
//    ArgumentCaptor<UploadedVideoAccepted> uploadedVideoCaptor = ArgumentCaptor.forClass(UploadedVideoAccepted.class);
//
//    @Captor
//    ArgumentCaptor<YouTubeVideoAdded> youtubeVideoCaptor = ArgumentCaptor.forClass(YouTubeVideoAdded.class);
//
//    @Before
//    public void setUp() {
//        videoCatalogService = new VideoCatalogService();
//        videoCatalogService.videoManager = resource.getManagerFactory().forVideo();
//        videoCatalogService.userVideosManager = resource.getManagerFactory().forUserVideos();
//        videoCatalogService.latestVideosManager = resource.getManagerFactory().forLatestVideos();
//        videoCatalogService.validator = new KillrVideoInputValidator();
//        videoCatalogService.eventBus = eventBus;
//        videoCatalogService.session = resource.getNativeSession();
//        videoCatalogService.executorService = MoreExecutors.newDirectExecutorService();
//    }
//
//    @Test
//    public void should_submit_uploaded_video() throws Exception {
//        //Given
//        final UUID videoId = UUID.randomUUID();
//        final UUID userId = UUID.randomUUID();
//        final String name = "video.mp4";
//        final String description = "sample video";
//        final String uploadUrl = "http://xxx";
//
//        SubmitUploadedVideoRequest request = SubmitUploadedVideoRequest
//                .newBuilder()
//                .setDescription(description)
//                .setName(name)
//                .setUploadUrl(uploadUrl)
//                .addAllTags(Sets.newHashSet("selfie", "world", "travel"))
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<SubmitUploadedVideoResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<SubmitUploadedVideoResponse> streamObserver = new StreamObserver<SubmitUploadedVideoResponse>() {
//            @Override
//            public void onNext(SubmitUploadedVideoResponse value) {
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
//        //When
//        videoCatalogService.submitUploadedVideo(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(response.get()).isNotNull();
//
//        verify(eventBus).post(uploadedVideoCaptor.capture());
//
//        final UploadedVideoAccepted captorValue = uploadedVideoCaptor.getValue();
//        assertThat(captorValue).isNotNull();
//        assertThat(captorValue.getVideoId().getValue()).isEqualTo(videoId.toString());
//        assertThat(captorValue.getUploadUrl()).isEqualTo(uploadUrl);
//        assertThat(captorValue.getTimestamp()).isNotNull();
//
//        final List<Row> allVideos = resource.getNativeSession().execute("SELECT * FROM killrvideo.videos").all();
//        assertThat(allVideos).isNotEmpty().hasSize(1);
//        assertThat(allVideos.get(0).getUUID("videoid")).isEqualTo(videoId);
//
//        final List<Row> userVideos = resource.getNativeSession().execute("SELECT * FROM killrvideo.user_videos").all();
//        assertThat(userVideos).isNotEmpty().hasSize(1);
//        assertThat(userVideos.get(0).getUUID("videoid")).isEqualTo(videoId);
//        assertThat(userVideos.get(0).getUUID("userid")).isEqualTo(userId);
//
//    }
//
//    @Test
//    public void should_validate_submitting_uploaded_video() throws Exception {
//        //Given
//        SubmitUploadedVideoRequest request = SubmitUploadedVideoRequest.newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<SubmitUploadedVideoResponse> streamObserver = new StreamObserver<SubmitUploadedVideoResponse>() {
//            @Override
//            public void onNext(SubmitUploadedVideoResponse value) {
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
//        videoCatalogService.submitUploadedVideo(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("user id");
//        assertThat(error.get().getMessage()).contains("video id");
//        assertThat(error.get().getMessage()).contains("video name");
//        assertThat(error.get().getMessage()).contains("video description");
//        assertThat(error.get().getMessage()).contains("video tags list");
//        assertThat(error.get().getMessage()).contains("video upload url");
//    }
//
//    @Test
//    public void should_submit_youtube_video() throws Exception {
//        //Given
//        final UUID videoId = UUID.randomUUID();
//        final UUID userId = UUID.randomUUID();
//        final String name = "video.mp4";
//        final String description = "sample video";
//        final String youtubeId = "xyz";
//
//        SubmitYouTubeVideoRequest request = SubmitYouTubeVideoRequest
//                .newBuilder()
//                .setDescription(description)
//                .setName(name)
//                .setYouTubeVideoId(youtubeId)
//                .addAllTags(Sets.newHashSet("selfie", "world", "travel"))
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<SubmitYouTubeVideoResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<SubmitYouTubeVideoResponse> streamObserver = new StreamObserver<SubmitYouTubeVideoResponse>() {
//            @Override
//            public void onNext(SubmitYouTubeVideoResponse value) {
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
//        //When
//        videoCatalogService.submitYouTubeVideo(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(response.get()).isNotNull();
//
//        verify(eventBus).post(youtubeVideoCaptor.capture());
//
//        final YouTubeVideoAdded captorValue = youtubeVideoCaptor.getValue();
//        assertThat(captorValue).isNotNull();
//        assertThat(captorValue.getAddedDate()).isNotNull();
//        assertThat(captorValue.getTimestamp()).isNotNull();
//        assertThat(captorValue.getLocation()).isEqualTo(youtubeId);
//        assertThat(captorValue.getName()).isEqualTo(name);
//        assertThat(captorValue.getDescription()).isEqualTo(description);
//        assertThat(captorValue.getPreviewImageLocation()).isEqualTo("//img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg");
//        assertThat(captorValue.getVideoId().getValue()).isEqualTo(videoId.toString());
//        assertThat(captorValue.getUserId().getValue()).isEqualTo(userId.toString());
//
//        final List<Row> allVideos = resource.getNativeSession().execute("SELECT * FROM killrvideo.videos").all();
//        assertThat(allVideos).isNotEmpty().hasSize(1);
//        assertThat(allVideos.get(0).getUUID("videoid")).isEqualTo(videoId);
//
//        final List<Row> userVideos = resource.getNativeSession().execute("SELECT * FROM killrvideo.user_videos").all();
//        assertThat(userVideos).isNotEmpty().hasSize(1);
//        assertThat(userVideos.get(0).getUUID("videoid")).isEqualTo(videoId);
//        assertThat(userVideos.get(0).getUUID("userid")).isEqualTo(userId);
//
//        final List<Row> latestVideos = resource.getNativeSession().execute("SELECT * FROM killrvideo.latest_videos").all();
//        assertThat(latestVideos).isNotEmpty().hasSize(1);
//        assertThat(latestVideos.get(0).getUUID("videoid")).isEqualTo(videoId);
//        assertThat(latestVideos.get(0).getUUID("userid")).isEqualTo(userId);
//
//    }
//
//    @Test
//    public void should_validate_submitting_youtube_video() throws Exception {
//        //Given
//        SubmitYouTubeVideoRequest request = SubmitYouTubeVideoRequest.newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<SubmitYouTubeVideoResponse> streamObserver = new StreamObserver<SubmitYouTubeVideoResponse>() {
//            @Override
//            public void onNext(SubmitYouTubeVideoResponse value) {
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
//        videoCatalogService.submitYouTubeVideo(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("user id");
//        assertThat(error.get().getMessage()).contains("video id");
//        assertThat(error.get().getMessage()).contains("video name");
//        assertThat(error.get().getMessage()).contains("video description");
//        assertThat(error.get().getMessage()).contains("video tags list");
//        assertThat(error.get().getMessage()).contains("video youtube id");
//    }
//
//    @Test
//    public void should_get_video() throws Exception {
//        //Given
//        UUID videoId = UUID.randomUUID();
//        resource.getScriptExecutor().executeScriptTemplate("videoCatalogService/insertVideo.cql", ImmutableMap.of("videoid", videoId));
//
//        GetVideoRequest request = GetVideoRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetVideoResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoResponse> streamObserver = new StreamObserver<GetVideoResponse>() {
//            @Override
//            public void onNext(GetVideoResponse value) {
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
//        videoCatalogService.getVideo(request, streamObserver);
//        latch.await();
//
//        //Then
//        final GetVideoResponse getVideoResponse = response.get();
//        assertThat(getVideoResponse).isNotNull();
//        assertThat(getVideoResponse.getUserId().getValue()).isEqualTo("00000000-1111-0000-0000-000000000000");
//        assertThat(getVideoResponse.getVideoId().getValue()).isEqualTo(videoId.toString());
//        assertThat(getVideoResponse.getName()).isEqualTo("random.mp4");
//    }
//
//    @Test
//    public void should_not_get_non_existing_video() throws Exception {
//        //Given
//        UUID videoId = UUID.randomUUID();
//
//        GetVideoRequest request = GetVideoRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<Throwable> throwable = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoResponse> streamObserver = new StreamObserver<GetVideoResponse>() {
//            @Override
//            public void onNext(GetVideoResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                throwable.getAndSet(t);
//                latch.countDown();
//                completed.getAndSet(true);
//
//            }
//
//            @Override
//            public void onCompleted() {
//
//            }
//        };
//
//        //When
//        videoCatalogService.getVideo(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(throwable.get().getMessage()).contains("Video with id " + videoId + " was not found");
//    }
//
//    @Test
//    public void should_validate_get_video() throws Exception {
//        //Given
//        GetVideoRequest request = GetVideoRequest.newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetVideoResponse> streamObserver = new StreamObserver<GetVideoResponse>() {
//            @Override
//            public void onNext(GetVideoResponse value) {
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
//        videoCatalogService.getVideo(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("video id");
//    }
//
//    @Test
//    public void should_get_video_previews() throws Exception {
//        //Given
//        Map<String, Object> params = new HashMap<>();
//        UUID videoId1 = UUID.randomUUID();
//        UUID videoId2 = UUID.randomUUID();
//        UUID videoId3 = UUID.randomUUID();
//        UUID videoId4 = UUID.randomUUID();
//        UUID videoId5 = UUID.randomUUID();
//
//        params.put("videoid1", videoId1);
//        params.put("videoid2", videoId2);
//        params.put("videoid3", videoId3);
//        params.put("videoid4", videoId4);
//        params.put("videoid5", videoId5);
//
//        resource.getScriptExecutor().executeScriptTemplate("videoCatalogService/insert5Videos.cql", params);
//
//        GetVideoPreviewsRequest request =GetVideoPreviewsRequest
//                .newBuilder()
//                .addAllVideoIds(Arrays.asList(videoId1, videoId2, videoId3, videoId4, videoId5)
//                    .stream().map(TypeConverter::uuidToUuid).collect(toList()))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoPreviewsResponse> streamObserver = new StreamObserver<GetVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetVideoPreviewsResponse value) {
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
//        videoCatalogService.getVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetVideoPreviewsResponse videoPreviewsResponse = response.get();
//        assertThat(videoPreviewsResponse).isNotNull();
//
//        final List<VideoPreview> videoList = videoPreviewsResponse.getVideoPreviewsList();
//        assertThat(videoList).isNotEmpty().hasSize(5);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("preview_video1.mp4", "preview_video2.mp4", "preview_video3.mp4",
//                        "preview_video4.mp4", "preview_video5.mp4");
//    }
//
//
//    @Test
//    public void should_get_no_video_previews() throws Exception {
//        //Given
//        GetVideoPreviewsRequest request =GetVideoPreviewsRequest
//                .newBuilder()
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetVideoPreviewsResponse> streamObserver = new StreamObserver<GetVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetVideoPreviewsResponse value) {
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
//        videoCatalogService.getVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetVideoPreviewsResponse videoPreviewsResponse = response.get();
//        assertThat(videoPreviewsResponse).isNotNull();
//
//        final List<VideoPreview> videoList = videoPreviewsResponse.getVideoPreviewsList();
//        assertThat(videoList).isEmpty();
//    }
//
//    @Test
//    public void should_validate_get_video_previews() throws Exception {
//        //Given
//        GetVideoPreviewsRequest request = GetVideoPreviewsRequest.newBuilder()
//                .addAllVideoIds(IntStream.range(0,23)
//                    .mapToObj(x -> UUID.randomUUID())
//                    .map(TypeConverter::uuidToUuid)
//                    .collect(toList()))
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetVideoPreviewsResponse> streamObserver = new StreamObserver<GetVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetVideoPreviewsResponse value) {
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
//        videoCatalogService.getVideoPreviews(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("cannot get more than 20 videos at once");
//    }
//
//    @Test
//    public void should_get_latest_video_previews() throws Exception {
//        //Given
//        insert9LatestVideos();
//
//        GetLatestVideoPreviewsRequest request =GetLatestVideoPreviewsRequest
//                .newBuilder()
//                .setPageSize(4)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetLatestVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetLatestVideoPreviewsResponse> streamObserver = new StreamObserver<GetLatestVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetLatestVideoPreviewsResponse value) {
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
//        videoCatalogService.getLatestVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetLatestVideoPreviewsResponse videoPreviewsResponse = response.get();
//        assertThat(videoPreviewsResponse).isNotNull();
//
//        final List<VideoPreview> videoList = videoPreviewsResponse.getVideoPreviewsList();
//        assertThat(videoList).isNotEmpty().hasSize(4);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("latest_video1.mp4", "latest_video2.mp4", "latest_video3.mp4",
//                        "latest_video4.mp4");
//
//        assertThat(videoPreviewsResponse.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_get_latest_video_previews_with_start_video_id_and_start_added_date() throws Exception {
//        //Given
//        Map<String, Object> params = new HashMap<>();
//        UUID videoId1 = UUID.randomUUID();
//        UUID videoId2 = UUID.randomUUID();
//        UUID videoId3 = UUID.randomUUID();
//        UUID videoId4 = UUID.randomUUID();
//        UUID videoId5 = UUID.randomUUID();
//        UUID videoId6 = UUID.randomUUID();
//        UUID videoId7 = UUID.randomUUID();
//        UUID videoId8 = UUID.randomUUID();
//        UUID videoId9 = UUID.randomUUID();
//
//        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
//
//        params.put("today", now.format(formatter));
//        params.put("videoid1", videoId1);
//        params.put("videoid2", videoId2);
//        params.put("videoid3", videoId3);
//        params.put("videoid4", videoId4);
//        params.put("videoid5", videoId5);
//        params.put("videoid6", videoId6);
//        params.put("videoid7", videoId7);
//        params.put("videoid8", videoId8);
//        params.put("videoid9", videoId9);
//
//        resource.getScriptExecutor().executeScriptTemplate("videoCatalogService/insert9LatestVideosForSingleDay.cql", params);
//
//        GetLatestVideoPreviewsRequest request =GetLatestVideoPreviewsRequest
//                .newBuilder()
//                .setStartingVideoId(TypeConverter.uuidToUuid(videoId7))
//                .setStartingAddedDate(TypeConverter.instantToTimeStamp(Instant.parse("2016-08-01T17:00:00.000Z")))
//                .setPageSize(2)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetLatestVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetLatestVideoPreviewsResponse> streamObserver = new StreamObserver<GetLatestVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetLatestVideoPreviewsResponse value) {
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
//        videoCatalogService.getLatestVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetLatestVideoPreviewsResponse videoPreviewsResponse = response.get();
//        assertThat(videoPreviewsResponse).isNotNull();
//
//        final List<VideoPreview> videoList = videoPreviewsResponse.getVideoPreviewsList();
//        assertThat(videoList).isNotEmpty().hasSize(2);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("latest_video7.mp4", "latest_video6.mp4");
//
//        assertThat(videoPreviewsResponse.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_get_latest_video_previews_with_paging_state() throws Exception {
//        //Given
//        insert9LatestVideos();
//
//        GetLatestVideoPreviewsRequest request =GetLatestVideoPreviewsRequest
//                .newBuilder()
//                .setPageSize(4)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetLatestVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetLatestVideoPreviewsResponse> streamObserver = new StreamObserver<GetLatestVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetLatestVideoPreviewsResponse value) {
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
//        videoCatalogService.getLatestVideoPreviews(request, streamObserver);
//        latch.await();
//
//        assertThat(response.get().getVideoPreviewsCount()).isEqualTo(4);
//
//        final CountDownLatch newLatch = new CountDownLatch(1);
//        final AtomicBoolean newCompleted = new AtomicBoolean(false);
//        final AtomicReference<GetLatestVideoPreviewsResponse> newResponse = new AtomicReference<>(null);
//
//        StreamObserver<GetLatestVideoPreviewsResponse> newStreamObserver = new StreamObserver<GetLatestVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetLatestVideoPreviewsResponse value) {
//                newResponse.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onCompleted() {
//                newLatch.countDown();
//                newCompleted.getAndSet(true);
//            }
//        };
//
//        GetLatestVideoPreviewsRequest newRequest = GetLatestVideoPreviewsRequest
//                .newBuilder()
//                .setPageSize(10)
//                .setPagingState(response.get().getPagingState())
//                .build();
//
//        //When
//        videoCatalogService.getLatestVideoPreviews(newRequest, newStreamObserver);
//        newLatch.await();
//
//        //Then
//        assertThat(newCompleted.get()).isTrue();
//        final GetLatestVideoPreviewsResponse newVideoResponse = newResponse.get();
//        assertThat(newVideoResponse).isNotNull();
//
//        final List<VideoPreview> videoList = newVideoResponse.getVideoPreviewsList();
//
//        /**
//         * We only have 3 results although we asked for 10 (page size = 10)
//         * It is normal because KillrVideo only fetches results back to maximum
//         * 7 days in the past. In the 1st query we already fetched 4 results so
//         * now it's normal to get only 3 results back
//         */
//        assertThat(videoList).isNotEmpty().hasSize(3);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("latest_video5.mp4", "latest_video6.mp4", "latest_video7.mp4");
//
//        assertThat(newVideoResponse.getPagingState()).isEmpty();
//    }
//
//    @Test
//    public void should_validate_get_latest_video_preview() throws Exception {
//        //Given
//        GetLatestVideoPreviewsRequest request = GetLatestVideoPreviewsRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetLatestVideoPreviewsResponse> streamObserver = new StreamObserver<GetLatestVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetLatestVideoPreviewsResponse value) {
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
//        videoCatalogService.getLatestVideoPreviews(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("page size should be strictly positive");
//    }
//
//
//    @Test
//    public void should_get_user_video_previews() throws Exception {
//        //Given
//        UUID userId = insert9UserVideos();
//
//        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest
//                .newBuilder()
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setPageSize(4)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserVideoPreviewsResponse> streamObserver = new StreamObserver<GetUserVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetUserVideoPreviewsResponse value) {
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
//        videoCatalogService.getUserVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetUserVideoPreviewsResponse userVideoPreviews = response.get();
//        assertThat(userVideoPreviews).isNotNull();
//
//        final List<VideoPreview> videoList = userVideoPreviews.getVideoPreviewsList();
//        assertThat(videoList).isNotEmpty().hasSize(4);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("user_video9.mp4", "user_video8.mp4", "user_video7.mp4",
//                        "user_video6.mp4");
//
//        assertThat(userVideoPreviews.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_get_user_video_previews_with_startingAddedDate() throws Exception {
//        //Given
//        UUID userId = insert9UserVideos();
//
//        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
//        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest
//                .newBuilder()
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setStartingAddedDate(TypeConverter.instantToTimeStamp(Instant.from(formatter.parse("2016-08-04 12:00:00.000+0000"))))
//                .setStartingVideoId(TypeConverter.uuidToUuid(UUID.fromString("00000000-1114-0000-0000-000000000000")))
//                .setPageSize(4)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserVideoPreviewsResponse> streamObserver = new StreamObserver<GetUserVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetUserVideoPreviewsResponse value) {
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
//        videoCatalogService.getUserVideoPreviews(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetUserVideoPreviewsResponse userVideoPreviews = response.get();
//        assertThat(userVideoPreviews).isNotNull();
//
//        final List<VideoPreview> videoList = userVideoPreviews.getVideoPreviewsList();
//        assertThat(videoList).isNotEmpty().hasSize(4);
//
//        assertThat(videoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("user_video4.mp4", "user_video3.mp4", "user_video2.mp4", "user_video1.mp4");
//
//        assertThat(userVideoPreviews.getPagingState()).isNotEmpty();
//    }
//
//    @Test
//    public void should_get_user_video_previews_with_paging_state() throws Exception {
//        //Given
//        UUID userId = insert9UserVideos();
//        GetUserVideoPreviewsRequest request = GetUserVideoPreviewsRequest
//                .newBuilder()
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setPageSize(4)
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserVideoPreviewsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserVideoPreviewsResponse> streamObserver = new StreamObserver<GetUserVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetUserVideoPreviewsResponse value) {
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
//        videoCatalogService.getUserVideoPreviews(request, streamObserver);
//        latch.await();
//
//        GetUserVideoPreviewsRequest newRequest = GetUserVideoPreviewsRequest
//                .newBuilder()
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setPagingState(response.get().getPagingState())
//                .setPageSize(10)
//                .build();
//
//        final CountDownLatch newLatch = new CountDownLatch(1);
//        final AtomicBoolean newCompleted = new AtomicBoolean(false);
//        final AtomicReference<GetUserVideoPreviewsResponse> newResponse = new AtomicReference<>(null);
//
//        StreamObserver<GetUserVideoPreviewsResponse> newStreamObserver = new StreamObserver<GetUserVideoPreviewsResponse>() {
//            @Override
//            public void onNext(GetUserVideoPreviewsResponse value) {
//                newResponse.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onCompleted() {
//                newLatch.countDown();
//                newCompleted.getAndSet(true);
//            }
//        };
//        //When
//        videoCatalogService.getUserVideoPreviews(newRequest, newStreamObserver);
//        newLatch.await();
//
//
//        //Then
//        assertThat(newCompleted.get()).isTrue();
//        final GetUserVideoPreviewsResponse newUserVideoPreviews = newResponse.get();
//        assertThat(newUserVideoPreviews).isNotNull();
//
//        final List<VideoPreview> newVideoList = newUserVideoPreviews.getVideoPreviewsList();
//        assertThat(newVideoList).isNotEmpty().hasSize(5);
//
//        assertThat(newVideoList.stream().map(VideoPreview::getName).collect(toList()))
//                .contains("user_video5.mp4", "user_video4.mp4",
//                          "user_video3.mp4", "user_video2.mp4", "user_video1.mp4");
//
//        assertThat(newUserVideoPreviews.getPagingState()).isEmpty();
//    }
//
//    private UUID insert9UserVideos() {
//        UUID userId = UUID.randomUUID();
//        resource.getScriptExecutor().executeScriptTemplate("videoCatalogService/insert9UserVideos.cql", ImmutableMap.of("userid", userId));
//        return userId;
//    }
//
//    private void insert9LatestVideos() {
//        Map<String, Object> params = new HashMap<>();
//        UUID videoId1 = UUID.randomUUID();
//        UUID videoId2 = UUID.randomUUID();
//        UUID videoId3 = UUID.randomUUID();
//        UUID videoId4 = UUID.randomUUID();
//        UUID videoId5 = UUID.randomUUID();
//        UUID videoId6 = UUID.randomUUID();
//        UUID videoId7 = UUID.randomUUID();
//        UUID videoId8 = UUID.randomUUID();
//        UUID videoId9 = UUID.randomUUID();
//
//        params.put("videoid1", videoId1);
//        params.put("videoid2", videoId2);
//        params.put("videoid3", videoId3);
//        params.put("videoid4", videoId4);
//        params.put("videoid5", videoId5);
//        params.put("videoid6", videoId6);
//        params.put("videoid7", videoId7);
//        params.put("videoid8", videoId8);
//        params.put("videoid9", videoId9);
//
//        final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyyMMdd");
//        final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
//        final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
//
//        LongStream.rangeClosed(1L, 9L).boxed()
//                .map(index -> Tuple2.of(index, now.minusDays(index)))
//                .map(tuple2 -> Tuple3.of(tuple2._1(), tuple2._2().format(formatter1), tuple2._2().format(formatter2)))
//                .forEach(tuple3 -> {
//                    String yyyymmddLabel = "yyyymmdd" + tuple3._1();
//                    String yyyymmdd = tuple3._2().toString();
//                    String dateLabel = "date" + tuple3._1();
//                    String date = tuple3._3().toString();
//                    params.put(yyyymmddLabel, yyyymmdd);
//                    params.put(dateLabel, date);
//                });
//
//        resource.getScriptExecutor().executeScriptTemplate("videoCatalogService/insert9LatestVideos.cql", params);
//    }

}
