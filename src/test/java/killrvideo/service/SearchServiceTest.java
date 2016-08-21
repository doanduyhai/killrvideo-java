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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.protobuf.ProtocolStringList;

import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.TagsByLetter;
import killrvideo.entity.VideoByTag;
import killrvideo.entity.VideoRating;
import killrvideo.entity.VideoRatingByUser;
import killrvideo.search.SearchServiceOuterClass;
import killrvideo.search.SearchServiceOuterClass.*;
import killrvideo.validation.KillrVideoInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {


    @Rule
    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
            .forJunit()
            .entityClassesToTruncate(VideoByTag.class, TagsByLetter.class)
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

    private SearchService searchService;

    @Before
    public void setUp() {
        searchService = new SearchService();
        searchService.videoByTagManager = resource.getManagerFactory().forVideoByTag();
        searchService.tagsByLetterManager= resource.getManagerFactory().forTagsByLetter();
        searchService.validator = new KillrVideoInputValidator();
    }
    
    @Test
    public void should_search_videos_by_tags() throws Exception {
        //Given
        Map<String, Object> params = new HashMap<>();
        UUID videoId1 = new UUID(10L, 0L);
        UUID videoId2 = new UUID(20L, 0L);
        UUID videoId3 = new UUID(30L, 0L);
        UUID videoId4 = new UUID(40L, 0L);
        UUID videoId5 = new UUID(50L, 0L);
        UUID userId = UUID.randomUUID();

        params.put("videoid1", videoId1);
        params.put("videoid2", videoId2);
        params.put("videoid3", videoId3);
        params.put("videoid4", videoId4);
        params.put("videoid5", videoId5);
        params.put("userid", userId);

        resource.getScriptExecutor().executeScriptTemplate("searchService/insertVideosByTags.cql", params);

        SearchVideosRequest request = SearchVideosRequest.newBuilder()
                .setQuery("sci-fi")
                .setPageSize(3)
                .build();


        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<SearchVideosResponse> response = new AtomicReference<>(null);

        StreamObserver<SearchVideosResponse> streamObserver = new StreamObserver<SearchVideosResponse>() {
            @Override
            public void onNext(SearchVideosResponse value) {
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
        searchService.searchVideos(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final SearchVideosResponse videosResponse = response.get();
        assertThat(videosResponse).isNotNull();

        final List<SearchResultsVideoPreview> videosList = videosResponse.getVideosList();
        assertThat(videosList).isNotEmpty().hasSize(3);

        assertThat(videosList.get(0).getVideoId().getValue()).isEqualTo(videoId1.toString());
        assertThat(videosList.get(0).getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(videosList.get(0).getName()).isEqualTo("Matrix");

        assertThat(videosList.get(1).getVideoId().getValue()).isEqualTo(videoId2.toString());
        assertThat(videosList.get(1).getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(videosList.get(1).getName()).isEqualTo("Equilibrium");

        assertThat(videosList.get(2).getVideoId().getValue()).isEqualTo(videoId3.toString());
        assertThat(videosList.get(2).getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(videosList.get(2).getName()).isEqualTo("PayCheck");

        assertThat(videosResponse.getQuery()).isEqualTo("sci-fi");
        assertThat(videosResponse.getPagingState()).isNotEmpty();
    }

    @Test
    public void should_validate_search_videos_request() throws Exception {
        //Given
        SearchVideosRequest request = SearchVideosRequest
                .newBuilder()
                .setPageSize(0)
                .build();

        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<SearchVideosResponse> streamObserver = new StreamObserver<SearchVideosResponse>() {
            @Override
            public void onNext(SearchVideosResponse value) {

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
        searchService.searchVideos(request, streamObserver);

        //Then
        assertThat(error.get()).isNotNull();
        assertThat(error.get().getMessage()).contains("query string");
        assertThat(error.get().getMessage()).contains("page size");
    }

    @Test
    public void should_get_query_suggestions() throws Exception {
        //Given

        resource.getScriptExecutor().executeScript("searchService/insertTagsByLetter.cql");

        GetQuerySuggestionsRequest request = GetQuerySuggestionsRequest.newBuilder()
                .setQuery("science")
                .setPageSize(3)
                .build();


        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<GetQuerySuggestionsResponse> response = new AtomicReference<>(null);

        StreamObserver<GetQuerySuggestionsResponse> streamObserver = new StreamObserver<GetQuerySuggestionsResponse>() {
            @Override
            public void onNext(GetQuerySuggestionsResponse value) {
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
        searchService.getQuerySuggestions(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final GetQuerySuggestionsResponse videosResponse = response.get();
        assertThat(videosResponse).isNotNull();

        final List<String> suggestionsList = videosResponse.getSuggestionsList();
        assertThat(suggestionsList).isNotEmpty().hasSize(3);

        assertThat(suggestionsList.get(0)).isEqualTo("science");
        assertThat(suggestionsList.get(1)).isEqualTo("serious");
        assertThat(suggestionsList.get(2)).isEqualTo("sex");
        assertThat(videosResponse.getQuery()).isEqualTo("science");
    }

    @Test
    public void should_validate_get_video_suggestions_request() throws Exception {
        //Given
        GetQuerySuggestionsRequest request = GetQuerySuggestionsRequest
                .newBuilder()
                .setPageSize(0)
                .build();

        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final AtomicBoolean completed = new AtomicBoolean(false);

        StreamObserver<GetQuerySuggestionsResponse> streamObserver = new StreamObserver<GetQuerySuggestionsResponse>() {
            @Override
            public void onNext(GetQuerySuggestionsResponse value) {

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
        searchService.getQuerySuggestions(request, streamObserver);

        //Then
        assertThat(error.get()).isNotNull();
        assertThat(error.get().getMessage()).contains("query string");
        assertThat(error.get().getMessage()).contains("page size");
    }
}