package killrvideo.service;

import static java.util.stream.Collectors.toList;
import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import killrvideo.entity.Schema;
import killrvideo.utils.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.VideoPlaybackStats;
import killrvideo.events.CassandraMutationError;
import killrvideo.statistics.StatisticsServiceGrpc.AbstractStatisticsService;
import killrvideo.statistics.StatisticsServiceOuterClass.*;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class StatisticsService extends AbstractStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);

    @Inject
    Mapper<VideoPlaybackStats> videoPlaybackStatsMapper;

    @Inject
    MappingManager manager;

    @Inject
    EventBus eventBus;

    @Inject
    KillrVideoInputValidator validator;

    Session session;
    private String videoPlaybackStatsTableName;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        videoPlaybackStatsTableName = videoPlaybackStatsMapper.getTableMetadata().getName();
    }

    @Override
    public void recordPlaybackStarted(RecordPlaybackStartedRequest request, StreamObserver<RecordPlaybackStartedResponse> responseObserver) {

        LOGGER.debug("Start recording playback");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        /**
         * Increment video playback counter
         * In case of mutation error, record the request into
         * a mutation log file for later replay by another
         * micro-service
         */
        BuiltStatement statement = QueryBuilder
                .update(Schema.KEYSPACE, videoPlaybackStatsTableName)
                .with(QueryBuilder.incr("views")) //use incr() call to increment my counter field https://docs.datastax.com/en/developer/java-driver/3.2/faq/#how-do-i-increment-counters-with-query-builder
                .where(QueryBuilder.eq("videoid", videoId));

        FutureUtils.buildCompletableFuture(session.executeAsync(statement))
                .handle((rs, ex) -> {
                    if (rs != null) {
                        responseObserver.onNext(RecordPlaybackStartedResponse.newBuilder().build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End recording playback");

                    } else if (ex != null) {
                        LOGGER.error("Exception recording playback : " + mergeStackTrace(ex));

                        eventBus.post(new CassandraMutationError(request, ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                });
    }

    @Override
    public void getNumberOfPlays(GetNumberOfPlaysRequest request, StreamObserver<GetNumberOfPlaysResponse> responseObserver) {

        LOGGER.debug("-----Start getting number of plays------");
        LOGGER.debug("Statistics request is: " + request.toString());

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final List<CompletableFuture<VideoPlaybackStats>> statsFuture = request
                .getVideoIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .map(uuid -> videoPlaybackStatsMapper.getAsync(uuid))
                .map(uuid -> FutureUtils.buildCompletableFuture(uuid))
                .collect(toList());

        final GetNumberOfPlaysResponse.Builder builder = GetNumberOfPlaysResponse
                .newBuilder();

        /**
         * We fire a list of async SELECT request and wait for all of them
         * to complete before returning a response to the client
         */
        CompletableFuture
                .allOf(statsFuture.toArray(new CompletableFuture[statsFuture.size()]))
                .thenApply(v -> statsFuture.stream().map(CompletableFuture::join).collect(toList()))
                .handle((list, ex) ->{
                    if (list != null) {
                        final Map<Uuid, PlayStats> result = list.stream()
                                .filter(x -> x != null)
                                .map(VideoPlaybackStats::toPlayStats)
                                .collect(Collectors.toMap(x -> x.getVideoId(), x -> x));

                        for (Uuid requestedVideoId : request.getVideoIdsList()) {
                            if (result.containsKey(requestedVideoId)) {
                                builder.addStats(result.get(requestedVideoId));
                            } else {
                                builder.addStats(PlayStats
                                        .newBuilder()
                                        .setVideoId(requestedVideoId)
                                        .setViews(0L)
                                        .build());
                            }
                        }
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting number of plays");

                    } else if (ex != null) {

                        LOGGER.error("Exception getting number of plays : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return list;
                });
    }
}
