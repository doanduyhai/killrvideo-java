package killrvideo.service;

import java.util.UUID;
import javax.inject.Inject;

import info.archinnov.achilles.generated.manager.Video_Manager;
import io.grpc.stub.StreamObserver;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.AbstractSuggestedVideoService;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse;

public class SuggestedVideosService extends AbstractSuggestedVideoService {

    @Inject
    Video_Manager videoManager;

    @Override
    public void getRelatedVideos(GetRelatedVideosRequest request, StreamObserver<GetRelatedVideosResponse> responseObserver) {

        final UUID videoId = UUID.fromString(request.getVideoId().getValue());

        videoManager
                .dsl()
                .select()
                .tags()
                .fromBaseTable()
                .where()
                .videoid_Eq(videoId);

        final GetRelatedVideosResponse.Builder builder = GetRelatedVideosResponse.newBuilder();
        builder.setVideoId(request.getVideoId());

    }

    @Override
    public void getSuggestedForUser(GetSuggestedForUserRequest request, StreamObserver<GetSuggestedForUserResponse> responseObserver) {
    }
}
