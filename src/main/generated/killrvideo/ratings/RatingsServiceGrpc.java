package killrvideo.ratings;

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
 * Service that manages user's ratings of videos
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: ratings/ratings_service.proto")
public class RatingsServiceGrpc {

  private RatingsServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.ratings.RatingsService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest,
      killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> METHOD_RATE_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.ratings.RatingsService", "RateVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest,
      killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> METHOD_GET_RATING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.ratings.RatingsService", "GetRating"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest,
      killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> METHOD_GET_USER_RATING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.ratings.RatingsService", "GetUserRating"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RatingsServiceStub newStub(io.grpc.Channel channel) {
    return new RatingsServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RatingsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RatingsServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static RatingsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RatingsServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service that manages user's ratings of videos
   * </pre>
   */
  public static interface RatingsService {

    /**
     * <pre>
     * Rate a video
     * </pre>
     */
    public void rateVideo(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> responseObserver);

    /**
     * <pre>
     * Gets the current rating stats for a video
     * </pre>
     */
    public void getRating(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> responseObserver);

    /**
     * <pre>
     * Gets a user's rating of a specific video and returns 0 if the user hasn't rated the video
     * </pre>
     */
    public void getUserRating(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractRatingsService implements RatingsService, io.grpc.BindableService {

    @java.lang.Override
    public void rateVideo(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_RATE_VIDEO, responseObserver);
    }

    @java.lang.Override
    public void getRating(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_RATING, responseObserver);
    }

    @java.lang.Override
    public void getUserRating(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_USER_RATING, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return RatingsServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service that manages user's ratings of videos
   * </pre>
   */
  public static interface RatingsServiceBlockingClient {

    /**
     * <pre>
     * Rate a video
     * </pre>
     */
    public killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse rateVideo(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request);

    /**
     * <pre>
     * Gets the current rating stats for a video
     * </pre>
     */
    public killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse getRating(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request);

    /**
     * <pre>
     * Gets a user's rating of a specific video and returns 0 if the user hasn't rated the video
     * </pre>
     */
    public killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse getUserRating(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request);
  }

  /**
   * <pre>
   * Service that manages user's ratings of videos
   * </pre>
   */
  public static interface RatingsServiceFutureClient {

    /**
     * <pre>
     * Rate a video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> rateVideo(
        killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request);

    /**
     * <pre>
     * Gets the current rating stats for a video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> getRating(
        killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request);

    /**
     * <pre>
     * Gets a user's rating of a specific video and returns 0 if the user hasn't rated the video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> getUserRating(
        killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request);
  }

  public static class RatingsServiceStub extends io.grpc.stub.AbstractStub<RatingsServiceStub>
      implements RatingsService {
    private RatingsServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingsServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingsServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingsServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void rateVideo(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RATE_VIDEO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getRating(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_RATING, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getUserRating(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request,
        io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_USER_RATING, getCallOptions()), request, responseObserver);
    }
  }

  public static class RatingsServiceBlockingStub extends io.grpc.stub.AbstractStub<RatingsServiceBlockingStub>
      implements RatingsServiceBlockingClient {
    private RatingsServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingsServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingsServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingsServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse rateVideo(killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RATE_VIDEO, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse getRating(killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_RATING, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse getUserRating(killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_USER_RATING, getCallOptions(), request);
    }
  }

  public static class RatingsServiceFutureStub extends io.grpc.stub.AbstractStub<RatingsServiceFutureStub>
      implements RatingsServiceFutureClient {
    private RatingsServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingsServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingsServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingsServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse> rateVideo(
        killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RATE_VIDEO, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse> getRating(
        killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_RATING, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse> getUserRating(
        killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_USER_RATING, getCallOptions()), request);
    }
  }

  private static final int METHODID_RATE_VIDEO = 0;
  private static final int METHODID_GET_RATING = 1;
  private static final int METHODID_GET_USER_RATING = 2;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RatingsService serviceImpl;
    private final int methodId;

    public MethodHandlers(RatingsService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RATE_VIDEO:
          serviceImpl.rateVideo((killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse>) responseObserver);
          break;
        case METHODID_GET_RATING:
          serviceImpl.getRating((killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse>) responseObserver);
          break;
        case METHODID_GET_USER_RATING:
          serviceImpl.getUserRating((killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse>) responseObserver);
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
      final RatingsService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_RATE_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest,
              killrvideo.ratings.RatingsServiceOuterClass.RateVideoResponse>(
                serviceImpl, METHODID_RATE_VIDEO)))
        .addMethod(
          METHOD_GET_RATING,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest,
              killrvideo.ratings.RatingsServiceOuterClass.GetRatingResponse>(
                serviceImpl, METHODID_GET_RATING)))
        .addMethod(
          METHOD_GET_USER_RATING,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest,
              killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingResponse>(
                serviceImpl, METHODID_GET_USER_RATING)))
        .build();
  }
}
