package killrvideo.sample_data;

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
 * Service for managing sample data on the site
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: sample-data/sample_data_service.proto")
public class SampleDataServiceGrpc {

  private SampleDataServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.sample_data.SampleDataService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> METHOD_ADD_SAMPLE_COMMENTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "AddSampleComments"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> METHOD_ADD_SAMPLE_RATINGS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "AddSampleRatings"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> METHOD_ADD_SAMPLE_USERS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "AddSampleUsers"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> METHOD_ADD_SAMPLE_VIDEO_VIEWS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "AddSampleVideoViews"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "AddSampleYouTubeVideos"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest,
      killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> METHOD_REFRESH_YOU_TUBE_SOURCES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.sample_data.SampleDataService", "RefreshYouTubeSources"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SampleDataServiceStub newStub(io.grpc.Channel channel) {
    return new SampleDataServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SampleDataServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SampleDataServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static SampleDataServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SampleDataServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service for managing sample data on the site
   * </pre>
   */
  public static interface SampleDataService {

    /**
     * <pre>
     * Adds sample comment data
     * </pre>
     */
    public void addSampleComments(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> responseObserver);

    /**
     * <pre>
     * Adds sample ratings data
     * </pre>
     */
    public void addSampleRatings(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> responseObserver);

    /**
     * <pre>
     * Adds sample users data
     * </pre>
     */
    public void addSampleUsers(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> responseObserver);

    /**
     * <pre>
     * Adds sample video views data
     * </pre>
     */
    public void addSampleVideoViews(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> responseObserver);

    /**
     * <pre>
     * Adds sample YouTube video data
     * </pre>
     */
    public void addSampleYouTubeVideos(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> responseObserver);

    /**
     * <pre>
     * Triggers a refresh of the YouTube sample video data sources
     * </pre>
     */
    public void refreshYouTubeSources(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractSampleDataService implements SampleDataService, io.grpc.BindableService {

    @java.lang.Override
    public void addSampleComments(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_SAMPLE_COMMENTS, responseObserver);
    }

    @java.lang.Override
    public void addSampleRatings(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_SAMPLE_RATINGS, responseObserver);
    }

    @java.lang.Override
    public void addSampleUsers(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_SAMPLE_USERS, responseObserver);
    }

    @java.lang.Override
    public void addSampleVideoViews(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_SAMPLE_VIDEO_VIEWS, responseObserver);
    }

    @java.lang.Override
    public void addSampleYouTubeVideos(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS, responseObserver);
    }

    @java.lang.Override
    public void refreshYouTubeSources(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REFRESH_YOU_TUBE_SOURCES, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return SampleDataServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service for managing sample data on the site
   * </pre>
   */
  public static interface SampleDataServiceBlockingClient {

    /**
     * <pre>
     * Adds sample comment data
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse addSampleComments(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request);

    /**
     * <pre>
     * Adds sample ratings data
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse addSampleRatings(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request);

    /**
     * <pre>
     * Adds sample users data
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse addSampleUsers(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request);

    /**
     * <pre>
     * Adds sample video views data
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse addSampleVideoViews(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request);

    /**
     * <pre>
     * Adds sample YouTube video data
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse addSampleYouTubeVideos(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request);

    /**
     * <pre>
     * Triggers a refresh of the YouTube sample video data sources
     * </pre>
     */
    public killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse refreshYouTubeSources(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request);
  }

  /**
   * <pre>
   * Service for managing sample data on the site
   * </pre>
   */
  public static interface SampleDataServiceFutureClient {

    /**
     * <pre>
     * Adds sample comment data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> addSampleComments(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request);

    /**
     * <pre>
     * Adds sample ratings data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> addSampleRatings(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request);

    /**
     * <pre>
     * Adds sample users data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> addSampleUsers(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request);

    /**
     * <pre>
     * Adds sample video views data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> addSampleVideoViews(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request);

    /**
     * <pre>
     * Adds sample YouTube video data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> addSampleYouTubeVideos(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request);

    /**
     * <pre>
     * Triggers a refresh of the YouTube sample video data sources
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> refreshYouTubeSources(
        killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request);
  }

  public static class SampleDataServiceStub extends io.grpc.stub.AbstractStub<SampleDataServiceStub>
      implements SampleDataService {
    private SampleDataServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SampleDataServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleDataServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SampleDataServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void addSampleComments(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_COMMENTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void addSampleRatings(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_RATINGS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void addSampleUsers(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_USERS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void addSampleVideoViews(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_VIDEO_VIEWS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void addSampleYouTubeVideos(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void refreshYouTubeSources(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request,
        io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REFRESH_YOU_TUBE_SOURCES, getCallOptions()), request, responseObserver);
    }
  }

  public static class SampleDataServiceBlockingStub extends io.grpc.stub.AbstractStub<SampleDataServiceBlockingStub>
      implements SampleDataServiceBlockingClient {
    private SampleDataServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SampleDataServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleDataServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SampleDataServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse addSampleComments(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_SAMPLE_COMMENTS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse addSampleRatings(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_SAMPLE_RATINGS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse addSampleUsers(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_SAMPLE_USERS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse addSampleVideoViews(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_SAMPLE_VIDEO_VIEWS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse addSampleYouTubeVideos(killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse refreshYouTubeSources(killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REFRESH_YOU_TUBE_SOURCES, getCallOptions(), request);
    }
  }

  public static class SampleDataServiceFutureStub extends io.grpc.stub.AbstractStub<SampleDataServiceFutureStub>
      implements SampleDataServiceFutureClient {
    private SampleDataServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SampleDataServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SampleDataServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SampleDataServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse> addSampleComments(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_COMMENTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse> addSampleRatings(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_RATINGS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse> addSampleUsers(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_USERS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse> addSampleVideoViews(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_VIDEO_VIEWS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse> addSampleYouTubeVideos(
        killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse> refreshYouTubeSources(
        killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REFRESH_YOU_TUBE_SOURCES, getCallOptions()), request);
    }
  }

  private static final int METHODID_ADD_SAMPLE_COMMENTS = 0;
  private static final int METHODID_ADD_SAMPLE_RATINGS = 1;
  private static final int METHODID_ADD_SAMPLE_USERS = 2;
  private static final int METHODID_ADD_SAMPLE_VIDEO_VIEWS = 3;
  private static final int METHODID_ADD_SAMPLE_YOU_TUBE_VIDEOS = 4;
  private static final int METHODID_REFRESH_YOU_TUBE_SOURCES = 5;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SampleDataService serviceImpl;
    private final int methodId;

    public MethodHandlers(SampleDataService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ADD_SAMPLE_COMMENTS:
          serviceImpl.addSampleComments((killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse>) responseObserver);
          break;
        case METHODID_ADD_SAMPLE_RATINGS:
          serviceImpl.addSampleRatings((killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse>) responseObserver);
          break;
        case METHODID_ADD_SAMPLE_USERS:
          serviceImpl.addSampleUsers((killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse>) responseObserver);
          break;
        case METHODID_ADD_SAMPLE_VIDEO_VIEWS:
          serviceImpl.addSampleVideoViews((killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse>) responseObserver);
          break;
        case METHODID_ADD_SAMPLE_YOU_TUBE_VIDEOS:
          serviceImpl.addSampleYouTubeVideos((killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse>) responseObserver);
          break;
        case METHODID_REFRESH_YOU_TUBE_SOURCES:
          serviceImpl.refreshYouTubeSources((killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse>) responseObserver);
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
      final SampleDataService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_ADD_SAMPLE_COMMENTS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleCommentsResponse>(
                serviceImpl, METHODID_ADD_SAMPLE_COMMENTS)))
        .addMethod(
          METHOD_ADD_SAMPLE_RATINGS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleRatingsResponse>(
                serviceImpl, METHODID_ADD_SAMPLE_RATINGS)))
        .addMethod(
          METHOD_ADD_SAMPLE_USERS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleUsersResponse>(
                serviceImpl, METHODID_ADD_SAMPLE_USERS)))
        .addMethod(
          METHOD_ADD_SAMPLE_VIDEO_VIEWS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleVideoViewsResponse>(
                serviceImpl, METHODID_ADD_SAMPLE_VIDEO_VIEWS)))
        .addMethod(
          METHOD_ADD_SAMPLE_YOU_TUBE_VIDEOS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.AddSampleYouTubeVideosResponse>(
                serviceImpl, METHODID_ADD_SAMPLE_YOU_TUBE_VIDEOS)))
        .addMethod(
          METHOD_REFRESH_YOU_TUBE_SOURCES,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesRequest,
              killrvideo.sample_data.SampleDataServiceOuterClass.RefreshYouTubeSourcesResponse>(
                serviceImpl, METHODID_REFRESH_YOU_TUBE_SOURCES)))
        .build();
  }
}
