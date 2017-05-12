package killrvideo.service;

import static java.util.Arrays.asList;
import static java.util.Arrays.toString;
import static java.util.stream.Collectors.toList;
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

//import info.archinnov.achilles.generated.ManagerFactory;
//import info.archinnov.achilles.generated.ManagerFactoryBuilder;
//import info.archinnov.achilles.junit.AchillesTestResource;
//import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.VideoPlaybackStats;
import killrvideo.search.SearchServiceOuterClass;
import killrvideo.statistics.StatisticsServiceOuterClass;
import killrvideo.statistics.StatisticsServiceOuterClass.*;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceTest {

    //:TODO Fix this
//    @Rule
//    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
//            .forJunit()
//            .entityClassesToTruncate(VideoPlaybackStats.class)
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
//    private StatisticsService statisticsService;
//
//    @Before
//    public void setUp() {
//        statisticsService = new StatisticsService();
//        statisticsService.videoPlaybackStatsManager = resource.getManagerFactory().forVideoPlaybackStats();
//        statisticsService.validator = new KillrVideoInputValidator();
//    }
//
//    @Test
//    public void should_record_playback_started() throws Exception {
//        //Given
//        UUID videoId = UUID.randomUUID();
//
//        RecordPlaybackStartedRequest request = RecordPlaybackStartedRequest
//                .newBuilder()
//                .setVideoId(TypeConverter.uuidToUuid(videoId))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<RecordPlaybackStartedResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<RecordPlaybackStartedResponse> streamObserver = new StreamObserver<RecordPlaybackStartedResponse>() {
//            @Override
//            public void onNext(RecordPlaybackStartedResponse value) {
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
//        statisticsService.recordPlaybackStarted(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final RecordPlaybackStartedResponse playbackResponse = response.get();
//        assertThat(playbackResponse).isNotNull();
//    }
//
//    @Test
//    public void should_validate_search_videos_request() throws Exception {
//        //Given
//        RecordPlaybackStartedRequest request = RecordPlaybackStartedRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<RecordPlaybackStartedResponse> streamObserver = new StreamObserver<RecordPlaybackStartedResponse>() {
//            @Override
//            public void onNext(RecordPlaybackStartedResponse value) {
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
//        statisticsService.recordPlaybackStarted(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("video id");
//    }
//
//    @Test
//    public void should_get_number_of_plays() throws Exception {
//        //Given
//        Map<String, Object> params = new HashMap<>();
//        UUID videoId1 = UUID.randomUUID();
//        UUID videoId2 = UUID.randomUUID();
//        UUID videoId3 = UUID.randomUUID();
//        UUID videoId4 = UUID.randomUUID();
//        UUID videoId5 = UUID.randomUUID();
//        UUID notFoundVideoId = UUID.randomUUID();
//
//        params.put("videoid1", videoId1);
//        params.put("videoid2", videoId2);
//        params.put("videoid3", videoId3);
//        params.put("videoid4", videoId4);
//        params.put("videoid5", videoId5);
//
//        long view1 = 10L;
//        long view2 = 20L;
//        long view3 = 30L;
//        long view4 = 40L;
//        long view5 = 50L;
//
//        params.put("view1", view1);
//        params.put("view2", view2);
//        params.put("view3", view3);
//        params.put("view4", view4);
//        params.put("view5", view5);
//
//        resource.getScriptExecutor().executeScriptTemplate("statisticsService/insertVideoPlaybacks.cql", params);
//
//        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest
//                .newBuilder()
//                .addAllVideoIds(asList(videoId1, videoId2, videoId3, videoId4, videoId5, notFoundVideoId)
//                        .stream()
//                        .map(TypeConverter::uuidToUuid)
//                        .collect(toList()))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetNumberOfPlaysResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetNumberOfPlaysResponse> streamObserver = new StreamObserver<GetNumberOfPlaysResponse>() {
//            @Override
//            public void onNext(GetNumberOfPlaysResponse value) {
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
//        statisticsService.getNumberOfPlays(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final GetNumberOfPlaysResponse numberOfPlaysResponse = response.get();
//        assertThat(numberOfPlaysResponse).isNotNull();
//
//        final List<String> videoIdsList = numberOfPlaysResponse.getStatsList().stream().map(PlayStats::getVideoId).map(Uuid::getValue).collect(toList());
//        final List<Long> statViews = numberOfPlaysResponse.getStatsList().stream().map(PlayStats::getViews).collect(toList());
//
//        assertThat(videoIdsList).containsAll(asList(videoId1, videoId2, videoId3, videoId4, videoId5, notFoundVideoId)
//                        .stream().map(UUID::toString).collect(toList()));
//
//        assertThat(statViews).containsAll(asList(view1, view2, view3, view4, view5, 0L));
//    }
//
//    @Test
//    public void should_validate_get_numer_of_plays_request() throws Exception {
//        //Given
//        GetNumberOfPlaysRequest request = GetNumberOfPlaysRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetNumberOfPlaysResponse> streamObserver = new StreamObserver<GetNumberOfPlaysResponse>() {
//            @Override
//            public void onNext(GetNumberOfPlaysResponse value) {
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
//        statisticsService.getNumberOfPlays(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("video id");
//    }
}