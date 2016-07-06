package killrvideo.uploads;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * Service that handles processing/re-encoding of uploaded videos
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: uploads/uploads_service.proto")
public class UploadsServiceGrpc {

  private UploadsServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.uploads.UploadsService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest,
      killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> METHOD_GET_UPLOAD_DESTINATION =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.uploads.UploadsService", "GetUploadDestination"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest,
      killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> METHOD_MARK_UPLOAD_COMPLETE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.uploads.UploadsService", "MarkUploadComplete"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest,
      killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> METHOD_GET_STATUS_OF_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.uploads.UploadsService", "GetStatusOfVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UploadsServiceStub newStub(io.grpc.Channel channel) {
    return new UploadsServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UploadsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UploadsServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static UploadsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UploadsServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service that handles processing/re-encoding of uploaded videos
   * </pre>
   */
  public static interface UploadsService {

    /**
     * <pre>
     * Gets an upload destination for a user to upload a video
     * </pre>
     */
    public void getUploadDestination(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> responseObserver);

    /**
     * <pre>
     * Marks an upload as complete
     * </pre>
     */
    public void markUploadComplete(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> responseObserver);

    /**
     * <pre>
     * Gets the status of an uploaded video
     * </pre>
     */
    public void getStatusOfVideo(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractUploadsService implements UploadsService, io.grpc.BindableService {

    @java.lang.Override
    public void getUploadDestination(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_UPLOAD_DESTINATION, responseObserver);
    }

    @java.lang.Override
    public void markUploadComplete(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_MARK_UPLOAD_COMPLETE, responseObserver);
    }

    @java.lang.Override
    public void getStatusOfVideo(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_STATUS_OF_VIDEO, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return UploadsServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service that handles processing/re-encoding of uploaded videos
   * </pre>
   */
  public static interface UploadsServiceBlockingClient {

    /**
     * <pre>
     * Gets an upload destination for a user to upload a video
     * </pre>
     */
    public killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse getUploadDestination(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request);

    /**
     * <pre>
     * Marks an upload as complete
     * </pre>
     */
    public killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse markUploadComplete(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request);

    /**
     * <pre>
     * Gets the status of an uploaded video
     * </pre>
     */
    public killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse getStatusOfVideo(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request);
  }

  /**
   * <pre>
   * Service that handles processing/re-encoding of uploaded videos
   * </pre>
   */
  public static interface UploadsServiceFutureClient {

    /**
     * <pre>
     * Gets an upload destination for a user to upload a video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> getUploadDestination(
        killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request);

    /**
     * <pre>
     * Marks an upload as complete
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> markUploadComplete(
        killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request);

    /**
     * <pre>
     * Gets the status of an uploaded video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> getStatusOfVideo(
        killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request);
  }

  public static class UploadsServiceStub extends io.grpc.stub.AbstractStub<UploadsServiceStub>
      implements UploadsService {
    private UploadsServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UploadsServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadsServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UploadsServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void getUploadDestination(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_UPLOAD_DESTINATION, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void markUploadComplete(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_MARK_UPLOAD_COMPLETE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getStatusOfVideo(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS_OF_VIDEO, getCallOptions()), request, responseObserver);
    }
  }

  public static class UploadsServiceBlockingStub extends io.grpc.stub.AbstractStub<UploadsServiceBlockingStub>
      implements UploadsServiceBlockingClient {
    private UploadsServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UploadsServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadsServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UploadsServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse getUploadDestination(killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_UPLOAD_DESTINATION, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse markUploadComplete(killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_MARK_UPLOAD_COMPLETE, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse getStatusOfVideo(killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_STATUS_OF_VIDEO, getCallOptions(), request);
    }
  }

  public static class UploadsServiceFutureStub extends io.grpc.stub.AbstractStub<UploadsServiceFutureStub>
      implements UploadsServiceFutureClient {
    private UploadsServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UploadsServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadsServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UploadsServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse> getUploadDestination(
        killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_UPLOAD_DESTINATION, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse> markUploadComplete(
        killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_MARK_UPLOAD_COMPLETE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse> getStatusOfVideo(
        killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS_OF_VIDEO, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_UPLOAD_DESTINATION = 0;
  private static final int METHODID_MARK_UPLOAD_COMPLETE = 1;
  private static final int METHODID_GET_STATUS_OF_VIDEO = 2;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UploadsService serviceImpl;
    private final int methodId;

    public MethodHandlers(UploadsService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_UPLOAD_DESTINATION:
          serviceImpl.getUploadDestination((killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse>) responseObserver);
          break;
        case METHODID_MARK_UPLOAD_COMPLETE:
          serviceImpl.markUploadComplete((killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse>) responseObserver);
          break;
        case METHODID_GET_STATUS_OF_VIDEO:
          serviceImpl.getStatusOfVideo((killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final UploadsService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_GET_UPLOAD_DESTINATION,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationRequest,
              killrvideo.uploads.UploadsServiceOuterClass.GetUploadDestinationResponse>(
                serviceImpl, METHODID_GET_UPLOAD_DESTINATION)))
        .addMethod(
          METHOD_MARK_UPLOAD_COMPLETE,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteRequest,
              killrvideo.uploads.UploadsServiceOuterClass.MarkUploadCompleteResponse>(
                serviceImpl, METHODID_MARK_UPLOAD_COMPLETE)))
        .addMethod(
          METHOD_GET_STATUS_OF_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoRequest,
              killrvideo.uploads.UploadsServiceOuterClass.GetStatusOfVideoResponse>(
                serviceImpl, METHODID_GET_STATUS_OF_VIDEO)))
        .build();
  }
}
