package killrvideo.service;

import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import killrvideo.uploads.UploadsServiceGrpc.AbstractUploadsService;
import killrvideo.uploads.UploadsServiceOuterClass.*;

public class UploadsService extends AbstractUploadsService {

    @Override
    public void getUploadDestination(GetUploadDestinationRequest request, StreamObserver<GetUploadDestinationResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED.withDescription("Uploading videos is currently not supported").asRuntimeException());
    }

    @Override
    public void markUploadComplete(MarkUploadCompleteRequest request, StreamObserver<MarkUploadCompleteResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED.withDescription("Uploading videos is currently not supported").asRuntimeException());
    }

    @Override
    public void getStatusOfVideo(GetStatusOfVideoRequest request, StreamObserver<GetStatusOfVideoResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED.withDescription("Uploading videos is currently not supported").asRuntimeException());
    }
}
