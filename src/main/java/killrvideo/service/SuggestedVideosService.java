package killrvideo.service;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.ListenableFuture;
import killrvideo.entity.*;
import killrvideo.utils.FutureUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.mapping.Mapper;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.AbstractSuggestedVideoService;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class SuggestedVideosService extends AbstractSuggestedVideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosService.class);

    public static final int RELATED_VIDEOS_TO_RETURN = 4;

    @Inject
    Mapper<Video> videoMapper;

    @Inject
    Mapper<VideoByTag> videoByTagMapper;

    @Inject
    MappingManager manager;

    @Inject
    KillrVideoInputValidator validator;

    private Session session;
    private String videoByTagTableName;
    private PreparedStatement getRelatedVideos_getVideosByTagPrepared;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        videoByTagTableName = videoByTagMapper.getTableMetadata().getName();

        getRelatedVideos_getVideosByTagPrepared = session.prepare(
                QueryBuilder
                        .select().all()
                        .from(Schema.KEYSPACE, videoByTagTableName)
                        .where(QueryBuilder.eq("tag", QueryBuilder.bindMarker()))
        );
    }

    @Override
    public void getRelatedVideos(GetRelatedVideosRequest request, StreamObserver<GetRelatedVideosResponse> responseObserver) {

        LOGGER.debug("Start getting related videos");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        final GetRelatedVideosResponse.Builder builder = GetRelatedVideosResponse.newBuilder();
        builder.setVideoId(request.getVideoId());

        /**
         * Load the source video
         */
        FutureUtils.buildCompletableFuture(videoMapper.getAsync(videoId))
                .handleAsync((video, ex) -> {
                    LOGGER.debug("Handler thread " + Thread.currentThread().toString());
                    if (video == null) {
                        returnNoResult(responseObserver, builder);
                        return null;

                    } else {
                        /**
                         * Return immediately if the source video has
                         * no tags
                         */
                        if (CollectionUtils.isEmpty(video.getTags())) {
                            returnNoResult(responseObserver, builder);

                        } else {
                            final List<String> tags = new ArrayList<>(video.getTags());
                            Map<Uuid, SuggestedVideoPreview> results = new HashedMap();

                            /**
                             * Use the number of results we ultimately want * 2 when querying so that we can account
                             * for potentially having to filter out the video Id we're using as the basis for the query
                             * as well as duplicates
                             **/
                            final int pageSize = RELATED_VIDEOS_TO_RETURN * 2;
                            final List<ListenableFuture<Result<VideoByTag>>> inFlightQueries = new ArrayList<>();

                            /** Kick off a query for each tag and track them in the inflight requests list **/
                            for (int i = 0; i < tags.size(); i++) {
                                String tag = tags.get(i);

                                BoundStatement statement = getRelatedVideos_getVideosByTagPrepared.bind()
                                        .setString("tag", tag);

                                statement
                                        .setFetchSize(pageSize);

                                /**
                                 * By wrapping my session.executeAsync() method with
                                 * the DSE mapper videoByTagMapper.mapAsync() function I am
                                 * 1) automagically mapping the results to VideoByTag entities and
                                 * 2) automagically creating a prepared statement the mapper will use for
                                 * any subsequent calls to this same query.
                                 */
                                inFlightQueries.add(videoByTagMapper.mapAsync(session.executeAsync(statement)));

                                /** Every third query, or if this is the last tag, wait on all the query results **/
                                if (inFlightQueries.size() == 3 || i == tags.size() - 1) {
                                    for (ListenableFuture<Result<VideoByTag>> videos : inFlightQueries) {
                                        try {
                                            LOGGER.debug("Handler thread " + Thread.currentThread().toString());

                                            //TODO: Potentially move away from using get()
                                            results.putAll(videos.get().all()
                                                    .stream()
                                                    .map(VideoByTag::toSuggestedVideoPreview)
                                                    .filter(previewVid ->
                                                            !previewVid.getVideoId().equals(request.getVideoId())
                                                                    && !results.containsKey(previewVid.getVideoId())
                                                    )
                                                    .collect(toMap(preview -> preview.getVideoId(), preview -> preview)));

                                        } catch (Exception e) {
                                            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
                                        }
                                    }

                                    if (results.size() >= RELATED_VIDEOS_TO_RETURN) {
                                        break;
                                    } else {
                                        inFlightQueries.clear();
                                    }
                                }
                            }
                            builder.addAllVideos(results.values());
                            responseObserver.onNext(builder.build());
                            responseObserver.onCompleted();

                            LOGGER.debug(String.format("End getting related videos with %s videos", results.size()));
                        }
                    }
                    return video;
                });
    }

    private void returnNoResult(StreamObserver<GetRelatedVideosResponse> responseObserver, GetRelatedVideosResponse.Builder builder) {
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

        LOGGER.debug("End getting related videos with 0 video");
    }

    @Override
    public void getSuggestedForUser(GetSuggestedForUserRequest request, StreamObserver<GetSuggestedForUserResponse> responseObserver) {

        LOGGER.debug("Start getting suggested videos for user");

        // TODO: Can we implement suggestions without DSE and Spark? (Yeah, probably not)
        /** Not yet implemented **/
        final GetSuggestedForUserResponse response = GetSuggestedForUserResponse
                .newBuilder()
                .setUserId(request.getUserId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        LOGGER.debug("End getting suggested videos for user");
    }
}
