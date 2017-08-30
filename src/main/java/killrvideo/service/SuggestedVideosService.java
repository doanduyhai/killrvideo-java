package killrvideo.service;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.Vertex;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.dse.graph.api.DseGraph;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.Schema;
import killrvideo.entity.Video;
import killrvideo.entity.VideoByTag;
import killrvideo.graph.KillrVideoTraversal;
import killrvideo.graph.KillrVideoTraversalSource;
import killrvideo.ratings.events.RatingsEvents.UserRatedVideo;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.AbstractSuggestedVideoService;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.utils.FutureUtils;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.video_catalog.events.VideoCatalogEvents.YouTubeVideoAdded;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toMap;
import static killrvideo.graph.KillrVideoTraversalConstants.VERTEX_USER;
import static killrvideo.graph.KillrVideoTraversalConstants.VERTEX_VIDEO;
import static killrvideo.graph.__.*;

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
    DseSession dseSession;

    @Inject
    KillrVideoTraversalSource killr;

    @Inject
    KillrVideoInputValidator validator;

    private String videoByTagTableName;
    private PreparedStatement getRelatedVideos_getVideosByTagPrepared;

    @PostConstruct
    public void init(){
        videoByTagTableName = videoByTagMapper.getTableMetadata().getName();

        getRelatedVideos_getVideosByTagPrepared = dseSession.prepare(
                QueryBuilder
                        .select().all()
                        .from(Schema.KEYSPACE, videoByTagTableName)
                        .where(QueryBuilder.eq("tag", QueryBuilder.bindMarker()))
        ).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
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
                /**
                 * I use the *Async() version of .handle below because I am
                 * chaining multiple async futures.  In testing we found that chains like
                 * this would cause timeouts possibly from starvation.
                 */
                .handleAsync((video, ex) -> {
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
                                inFlightQueries.add(videoByTagMapper.mapAsync(dseSession.executeAsync(statement)));

                                //:TODO Refactor this section to break it up and make it easier to follow/read
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

        final Uuid userId = request.getUserId();
        final GetSuggestedForUserResponse.Builder builder = GetSuggestedForUserResponse.newBuilder();
        builder.setUserId(userId);

        final List<SuggestedVideoPreview> result = new ArrayList<>();
        final String userIdString = userId.getValue();

        try {
            /**
             * Use our KillrVideo DSL (Domain Specific Language ->
             * http://docs.datastax.com/en/developer/java-driver-dse/1.4/manual/tinkerpop/#gremlin-domain-specific-languages-dsl)
             * to first traverse our current user and then grab any recommended videos
             * from our recommendation engine.
             *
             * Parameters for recommendByUserRating are as follows:
             * number of recommendations - the number of recommended movies to return
             * min rating - the minimum rating to allow for
             * number of ratings to sample - the number of global user ratings to sample (smaller means faster traversal)
             * local user ratings to sample - the number of local user ratings to limit by
             */
            GraphStatement gStatement = DseGraph.statementFromTraversal(killr.users(userIdString)
                    .recommendByUserRating(100, 4, 500, 10)
            );

            CompletableFuture<GraphResultSet> future = FutureUtils.buildCompletableFuture(dseSession.executeGraphAsync(gStatement));

            future.whenComplete((vertices, ex) -> {
                if (vertices != null) {
                    List<GraphNode> videosAndUser = vertices.all();
                    for (GraphNode node : videosAndUser) {
                        Vertex v = node.get(VERTEX_VIDEO).asVertex();
                        Vertex u = node.get(VERTEX_USER).asVertex();

                        result.add(
                                SuggestedVideoPreview.newBuilder()
                                        .setAddedDate(TypeConverter.instantToTimeStamp(v.getProperty("added_date").getValue().as(Instant.class)))
                                        .setName(v.getProperty("name").getValue().asString())
                                        .setPreviewImageLocation(v.getProperty("preview_image_location").getValue().asString())
                                        .setUserId(TypeConverter.uuidToUuid(u.getId().get("userId").as(UUID.class)))
                                        .setVideoId(TypeConverter.uuidToUuid(v.getId().get("videoId").as(UUID.class)))
                                        .build()
                        );
                    }
                    // Add suggested videos...
                    builder.addAllVideos(result);
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                } else {
                    LOGGER.warn("Exception in SuggestedVideosService.getSuggestedForUser recommendByUserRating() recommendation traversal: " + ex);
                }
            });

        } catch (Exception ex) {
            LOGGER.error("Exception in SuggestedVideosService.getSuggestedForUser: " + ex);
        }

        LOGGER.debug("End getting suggested videos for user");
    }

    /**
     * Make @Subscribe subscriber magic happen anytime a youTube video is added from
     * VideoCatalogService.submitYouTubeVideo() with a call to eventBus.post().
     * We use this to create entries in our graph database for use with our
     * SuggestedVideos recommendation service which is why this exists here.
     * @param youTubeVideoAdded
     */
    @Subscribe
    public void handle(YouTubeVideoAdded youTubeVideoAdded) {
        final String className = this.getClass().getName();

        LOGGER.debug("Start handling YouTubeVideoAdded for " + className);

        final UUID userId = UUID.fromString(youTubeVideoAdded.getUserId().getValue());
        final UUID videoId = UUID.fromString(youTubeVideoAdded.getVideoId().getValue());
        final HashSet<String> tags = Sets.newHashSet(youTubeVideoAdded.getTagsList());
        final String name = youTubeVideoAdded.getName();
        final String previewImageLocation = youTubeVideoAdded.getPreviewImageLocation();
        final String description = youTubeVideoAdded.getDescription();
        Date addedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getAddedDate().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));
        Date taggedDate = Date.from(Instant.ofEpochSecond(youTubeVideoAdded.getTimestamp().getSeconds(), youTubeVideoAdded.getTimestamp().getNanos()));

        /**
         * Below we are using our KillrVideoTraversal DSL (Domain Specific Language)
         * to create our video vertex, then ensure we connect up the user responsible
         * for uploading the video with the "uploaded" edge, and then follow up with
         * any and all tags using the "taggedWith" edge.  Since we may have multiple
         * tags make sure to loop through and get them all in there.
         */
        final KillrVideoTraversal traversal =
                killr.video(videoId, name, addedDate, description, previewImageLocation)
                        .ensure(uploaded(userId));

        tags.forEach(tag -> {
            traversal.ensure(taggedWith(tag, taggedDate));
        });

        /**
         * Now that our video is successfully applied lets
         * insert that video into our graph for the recommendation engine
         */
        CompletableFuture<GraphResultSet> videoVertexFuture =
                FutureUtils.buildCompletableFuture(dseSession.executeGraphAsync(
                        DseGraph.statementFromTraversal(traversal))
                );

        videoVertexFuture.whenComplete((graphResultSet, ex) -> {
            if (graphResultSet != null) {
                LOGGER.debug("Added video vertex, uploaded, and taggedWith edges: " + graphResultSet.all());

            }  else {
                //TODO: Potentially add some robustness code here
                LOGGER.warn("Error handling YouTubeVideoAdded for graph: " + ex);
            }
        });
    }

    /**
     * Make @Subscribe subscriber magic happen anytime a user is created from
     * UserManagementService.createUser() with a call to eventBus.post().
     * We use this to create entries in our graph database for use with our
     * SuggestedVideos recommendation service which is why this exists here.
     * @param userCreated
     */
    @Subscribe
    public void handle(UserCreated userCreated) {
        final String className = this.getClass().getName();

        LOGGER.debug("Start handling UserCreated for " + className);

        /**
         * This will create a user vertex in our graph if it does not already exist
         */
        GraphStatement gStatement = DseGraph.statementFromTraversal(
                killr.user(
                        UUID.fromString(userCreated.getUserId().getValue()),
                        userCreated.getEmail(),
                        TypeConverter.dateFromTimestamp(userCreated.getTimestamp())
                ));

        CompletableFuture<GraphResultSet> graphResultFuture =
                FutureUtils.buildCompletableFuture(dseSession.executeGraphAsync(gStatement));

        graphResultFuture.whenComplete((graphResultSet, ex) -> {
            if (graphResultSet != null) {
                LOGGER.debug("Added user vertex: " + graphResultSet.one());

            } else {
                //TODO: Potentially add some robustness code here
                LOGGER.warn("Error creating user vertex: " + ex);
            }
        });
    }

    /**
     * Make @Subscribe subscriber magic happen anytime a user rates a video
     * RatingsService.rateVideo() with a call to eventBus.post().
     * We use this to create entries in our graph database for use with our
     * SuggestedVideos recommendation service which is why this exists here.
     * @param userRatedVideo
     */
    @Subscribe
    public void handle(UserRatedVideo userRatedVideo) {
        final String className = this.getClass().getName();

        LOGGER.debug("Start handling UserRatedVideo for " + className);

        /**
         * Note that if either the user or video does not exist in the graph
         * the rating will not be applied nor will the user or video be
         * automatically created in this case.  This assumes both the user and video
         * already exist.
         */
        final KillrVideoTraversal traversal =
                killr.videos(userRatedVideo.getVideoId().getValue())
                        .ensure(rated(
                                UUID.fromString(userRatedVideo.getUserId().getValue()),
                                userRatedVideo.getRating()
                        ));

        final CompletableFuture<GraphResultSet> ratingFuture =
                FutureUtils.buildCompletableFuture(dseSession.executeGraphAsync(DseGraph.statementFromTraversal(traversal)));

        ratingFuture.whenComplete((graphResultSet, ex) -> {
            if (graphResultSet != null) {
                LOGGER.debug("Added rating between user and video: " + graphResultSet.one());

            } else {
                //TODO: Potentially add some robustness code here
                LOGGER.warn("Error Adding rating between user and video: " + ex);
            }
        });
    }
}
