package killrvideo.service;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.Uninterruptibles;

import info.archinnov.achilles.generated.manager.VideoByTag_Manager;
import info.archinnov.achilles.generated.manager.Video_Manager;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.entity.Video;
import killrvideo.entity.VideoByTag;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.AbstractSuggestedVideoService;
import killrvideo.suggested_videos.SuggestedVideosService.*;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class SuggestedVideosService extends AbstractSuggestedVideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestedVideosService.class);

    public static final int RELATED_VIDEOS_TO_RETURN = 4;
    @Inject
    Video_Manager videoManager;

    @Inject
    VideoByTag_Manager videoByTagManager;

    @Inject
    KillrVideoInputValidator validator;

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
        final Video video = videoManager
                .dsl()
                .select()
                .allColumns_FromBaseTable()
                .where()
                .videoid().Eq(videoId)
                .getOne();

        if (video == null) {
            returnNoResult(responseObserver, builder);
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

                final List<CompletableFuture<List<VideoByTag>>> inFlightQueries = new ArrayList<>();

                /** Kick off a query for each tag and track them in the inflight requests list **/
                for (int i = 0; i < tags.size(); i++) {
                    String tag = tags.get(i);

                    final CompletableFuture<List<VideoByTag>> listAsync = videoByTagManager
                            .dsl()
                            .select()
                            .allColumns_FromBaseTable()
                            .where()
                            .tag().Eq(tag)
                            .withFetchSize(pageSize)
                            .getListAsync();

                    inFlightQueries.add(listAsync);

                    /** Every third query, or if this is the last tag, wait on all the query results **/
                    if (inFlightQueries.size() == 3 || i == tags.size() - 1) {

                        for (CompletableFuture<List<VideoByTag>> future : inFlightQueries) {
                            try {
                                results.putAll(Uninterruptibles.getUninterruptibly(future)
                                .stream()
                                .map(VideoByTag::toSuggestedVideoPreview)
                                .filter(previewVid -> !previewVid.getVideoId().equals(request.getVideoId()) && !results.containsKey(previewVid.getVideoId()))
                                .collect(toMap(preview -> preview.getVideoId(), preview -> preview)));

                            } catch (ExecutionException e) {
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
