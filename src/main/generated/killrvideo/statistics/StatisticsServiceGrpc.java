package killrvideo.statistics;

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
 * Service that tracks playback statistics for videos
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: statistics/statistics_service.proto")
public class StatisticsServiceGrpc {

  private StatisticsServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.statistics.StatisticsService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest,
      killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> METHOD_RECORD_PLAYBACK_STARTED =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.statistics.StatisticsService", "RecordPlaybackStarted"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest,
      killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> METHOD_GET_NUMBER_OF_PLAYS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.statistics.StatisticsService", "GetNumberOfPlays"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StatisticsServiceStub newStub(io.grpc.Channel channel) {
    return new StatisticsServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StatisticsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new StatisticsServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static StatisticsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new StatisticsServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service that tracks playback statistics for videos
   * </pre>
   */
  public static interface StatisticsService {

    /**
     * <pre>
     * Record that playback started for a given video
     * </pre>
     */
    public void recordPlaybackStarted(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> responseObserver);

    /**
     * <pre>
     * Get the number of plays for a given video or set of videos
     * </pre>
     */
    public void getNumberOfPlays(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractStatisticsService implements StatisticsService, io.grpc.BindableService {

    @java.lang.Override
    public void recordPlaybackStarted(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_RECORD_PLAYBACK_STARTED, responseObserver);
    }

    @java.lang.Override
    public void getNumberOfPlays(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_NUMBER_OF_PLAYS, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return StatisticsServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service that tracks playback statistics for videos
   * </pre>
   */
  public static interface StatisticsServiceBlockingClient {

    /**
     * <pre>
     * Record that playback started for a given video
     * </pre>
     */
    public killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse recordPlaybackStarted(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request);

    /**
     * <pre>
     * Get the number of plays for a given video or set of videos
     * </pre>
     */
    public killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse getNumberOfPlays(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request);
  }

  /**
   * <pre>
   * Service that tracks playback statistics for videos
   * </pre>
   */
  public static interface StatisticsServiceFutureClient {

    /**
     * <pre>
     * Record that playback started for a given video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> recordPlaybackStarted(
        killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request);

    /**
     * <pre>
     * Get the number of plays for a given video or set of videos
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> getNumberOfPlays(
        killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request);
  }

  public static class StatisticsServiceStub extends io.grpc.stub.AbstractStub<StatisticsServiceStub>
      implements StatisticsService {
    private StatisticsServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StatisticsServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatisticsServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StatisticsServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void recordPlaybackStarted(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RECORD_PLAYBACK_STARTED, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getNumberOfPlays(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request,
        io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NUMBER_OF_PLAYS, getCallOptions()), request, responseObserver);
    }
  }

  public static class StatisticsServiceBlockingStub extends io.grpc.stub.AbstractStub<StatisticsServiceBlockingStub>
      implements StatisticsServiceBlockingClient {
    private StatisticsServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StatisticsServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatisticsServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StatisticsServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse recordPlaybackStarted(killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RECORD_PLAYBACK_STARTED, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse getNumberOfPlays(killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_NUMBER_OF_PLAYS, getCallOptions(), request);
    }
  }

  public static class StatisticsServiceFutureStub extends io.grpc.stub.AbstractStub<StatisticsServiceFutureStub>
      implements StatisticsServiceFutureClient {
    private StatisticsServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private StatisticsServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StatisticsServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new StatisticsServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse> recordPlaybackStarted(
        killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RECORD_PLAYBACK_STARTED, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse> getNumberOfPlays(
        killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NUMBER_OF_PLAYS, getCallOptions()), request);
    }
  }

  private static final int METHODID_RECORD_PLAYBACK_STARTED = 0;
  private static final int METHODID_GET_NUMBER_OF_PLAYS = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StatisticsService serviceImpl;
    private final int methodId;

    public MethodHandlers(StatisticsService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RECORD_PLAYBACK_STARTED:
          serviceImpl.recordPlaybackStarted((killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse>) responseObserver);
          break;
        case METHODID_GET_NUMBER_OF_PLAYS:
          serviceImpl.getNumberOfPlays((killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse>) responseObserver);
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
      final StatisticsService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_RECORD_PLAYBACK_STARTED,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest,
              killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedResponse>(
                serviceImpl, METHODID_RECORD_PLAYBACK_STARTED)))
        .addMethod(
          METHOD_GET_NUMBER_OF_PLAYS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest,
              killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysResponse>(
                serviceImpl, METHODID_GET_NUMBER_OF_PLAYS)))
        .build();
  }
}
