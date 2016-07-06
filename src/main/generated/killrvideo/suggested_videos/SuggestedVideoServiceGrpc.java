package killrvideo.suggested_videos;

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
 * Service responsible for generating video suggestions
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: suggested-videos/suggested_videos_service.proto")
public class SuggestedVideoServiceGrpc {

  private SuggestedVideoServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.suggested_videos.SuggestedVideoService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest,
      killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> METHOD_GET_RELATED_VIDEOS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.suggested_videos.SuggestedVideoService", "GetRelatedVideos"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest,
      killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> METHOD_GET_SUGGESTED_FOR_USER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.suggested_videos.SuggestedVideoService", "GetSuggestedForUser"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SuggestedVideoServiceStub newStub(io.grpc.Channel channel) {
    return new SuggestedVideoServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SuggestedVideoServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SuggestedVideoServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static SuggestedVideoServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SuggestedVideoServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service responsible for generating video suggestions
   * </pre>
   */
  public static interface SuggestedVideoService {

    /**
     * <pre>
     * Gets videos related to another video
     * </pre>
     */
    public void getRelatedVideos(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> responseObserver);

    /**
     * <pre>
     * Gets personalized video suggestions for a user
     * </pre>
     */
    public void getSuggestedForUser(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractSuggestedVideoService implements SuggestedVideoService, io.grpc.BindableService {

    @java.lang.Override
    public void getRelatedVideos(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_RELATED_VIDEOS, responseObserver);
    }

    @java.lang.Override
    public void getSuggestedForUser(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_SUGGESTED_FOR_USER, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return SuggestedVideoServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service responsible for generating video suggestions
   * </pre>
   */
  public static interface SuggestedVideoServiceBlockingClient {

    /**
     * <pre>
     * Gets videos related to another video
     * </pre>
     */
    public killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse getRelatedVideos(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request);

    /**
     * <pre>
     * Gets personalized video suggestions for a user
     * </pre>
     */
    public killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse getSuggestedForUser(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request);
  }

  /**
   * <pre>
   * Service responsible for generating video suggestions
   * </pre>
   */
  public static interface SuggestedVideoServiceFutureClient {

    /**
     * <pre>
     * Gets videos related to another video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> getRelatedVideos(
        killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request);

    /**
     * <pre>
     * Gets personalized video suggestions for a user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> getSuggestedForUser(
        killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request);
  }

  public static class SuggestedVideoServiceStub extends io.grpc.stub.AbstractStub<SuggestedVideoServiceStub>
      implements SuggestedVideoService {
    private SuggestedVideoServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SuggestedVideoServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SuggestedVideoServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SuggestedVideoServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void getRelatedVideos(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_RELATED_VIDEOS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getSuggestedForUser(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_SUGGESTED_FOR_USER, getCallOptions()), request, responseObserver);
    }
  }

  public static class SuggestedVideoServiceBlockingStub extends io.grpc.stub.AbstractStub<SuggestedVideoServiceBlockingStub>
      implements SuggestedVideoServiceBlockingClient {
    private SuggestedVideoServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SuggestedVideoServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SuggestedVideoServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SuggestedVideoServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse getRelatedVideos(killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_RELATED_VIDEOS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse getSuggestedForUser(killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_SUGGESTED_FOR_USER, getCallOptions(), request);
    }
  }

  public static class SuggestedVideoServiceFutureStub extends io.grpc.stub.AbstractStub<SuggestedVideoServiceFutureStub>
      implements SuggestedVideoServiceFutureClient {
    private SuggestedVideoServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SuggestedVideoServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SuggestedVideoServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SuggestedVideoServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse> getRelatedVideos(
        killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_RELATED_VIDEOS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse> getSuggestedForUser(
        killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_SUGGESTED_FOR_USER, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_RELATED_VIDEOS = 0;
  private static final int METHODID_GET_SUGGESTED_FOR_USER = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SuggestedVideoService serviceImpl;
    private final int methodId;

    public MethodHandlers(SuggestedVideoService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_RELATED_VIDEOS:
          serviceImpl.getRelatedVideos((killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse>) responseObserver);
          break;
        case METHODID_GET_SUGGESTED_FOR_USER:
          serviceImpl.getSuggestedForUser((killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse>) responseObserver);
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
      final SuggestedVideoService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_GET_RELATED_VIDEOS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest,
              killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosResponse>(
                serviceImpl, METHODID_GET_RELATED_VIDEOS)))
        .addMethod(
          METHOD_GET_SUGGESTED_FOR_USER,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserRequest,
              killrvideo.suggested_videos.SuggestedVideosService.GetSuggestedForUserResponse>(
                serviceImpl, METHODID_GET_SUGGESTED_FOR_USER)))
        .build();
  }
}
