package killrvideo.video_catalog;

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
 * Service responsible for tracking the catalog of available videos for playback
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: video-catalog/video_catalog_service.proto")
public class VideoCatalogServiceGrpc {

  private VideoCatalogServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.video_catalog.VideoCatalogService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> METHOD_SUBMIT_UPLOADED_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "SubmitUploadedVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> METHOD_SUBMIT_YOU_TUBE_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "SubmitYouTubeVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> METHOD_GET_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "GetVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> METHOD_GET_VIDEO_PREVIEWS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "GetVideoPreviews"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> METHOD_GET_LATEST_VIDEO_PREVIEWS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "GetLatestVideoPreviews"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest,
      killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> METHOD_GET_USER_VIDEO_PREVIEWS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.video_catalog.VideoCatalogService", "GetUserVideoPreviews"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static VideoCatalogServiceStub newStub(io.grpc.Channel channel) {
    return new VideoCatalogServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static VideoCatalogServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new VideoCatalogServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static VideoCatalogServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new VideoCatalogServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Service responsible for tracking the catalog of available videos for playback
   * </pre>
   */
  public static interface VideoCatalogService {

    /**
     * <pre>
     * Submit an uploaded video to the catalog
     * </pre>
     */
    public void submitUploadedVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> responseObserver);

    /**
     * <pre>
     * Submit a YouTube video to the catalog
     * </pre>
     */
    public void submitYouTubeVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> responseObserver);

    /**
     * <pre>
     * Gets a video from the catalog
     * </pre>
     */
    public void getVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> responseObserver);

    /**
     * <pre>
     * Gets video previews for a limited number of videos from the catalog
     * </pre>
     */
    public void getVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> responseObserver);

    /**
     * <pre>
     * Gets video previews for the latest (i.e. newest) videos from the catalog
     * </pre>
     */
    public void getLatestVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> responseObserver);

    /**
     * <pre>
     * Gets video previews for videos added to the site by a particular user
     * </pre>
     */
    public void getUserVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractVideoCatalogService implements VideoCatalogService, io.grpc.BindableService {

    @java.lang.Override
    public void submitUploadedVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SUBMIT_UPLOADED_VIDEO, responseObserver);
    }

    @java.lang.Override
    public void submitYouTubeVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SUBMIT_YOU_TUBE_VIDEO, responseObserver);
    }

    @java.lang.Override
    public void getVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_VIDEO, responseObserver);
    }

    @java.lang.Override
    public void getVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_VIDEO_PREVIEWS, responseObserver);
    }

    @java.lang.Override
    public void getLatestVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_LATEST_VIDEO_PREVIEWS, responseObserver);
    }

    @java.lang.Override
    public void getUserVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_USER_VIDEO_PREVIEWS, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return VideoCatalogServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Service responsible for tracking the catalog of available videos for playback
   * </pre>
   */
  public static interface VideoCatalogServiceBlockingClient {

    /**
     * <pre>
     * Submit an uploaded video to the catalog
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse submitUploadedVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request);

    /**
     * <pre>
     * Submit a YouTube video to the catalog
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse submitYouTubeVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request);

    /**
     * <pre>
     * Gets a video from the catalog
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse getVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request);

    /**
     * <pre>
     * Gets video previews for a limited number of videos from the catalog
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse getVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request);

    /**
     * <pre>
     * Gets video previews for the latest (i.e. newest) videos from the catalog
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse getLatestVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request);

    /**
     * <pre>
     * Gets video previews for videos added to the site by a particular user
     * </pre>
     */
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse getUserVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request);
  }

  /**
   * <pre>
   * Service responsible for tracking the catalog of available videos for playback
   * </pre>
   */
  public static interface VideoCatalogServiceFutureClient {

    /**
     * <pre>
     * Submit an uploaded video to the catalog
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> submitUploadedVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request);

    /**
     * <pre>
     * Submit a YouTube video to the catalog
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> submitYouTubeVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request);

    /**
     * <pre>
     * Gets a video from the catalog
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> getVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request);

    /**
     * <pre>
     * Gets video previews for a limited number of videos from the catalog
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> getVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request);

    /**
     * <pre>
     * Gets video previews for the latest (i.e. newest) videos from the catalog
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> getLatestVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request);

    /**
     * <pre>
     * Gets video previews for videos added to the site by a particular user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> getUserVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request);
  }

  public static class VideoCatalogServiceStub extends io.grpc.stub.AbstractStub<VideoCatalogServiceStub>
      implements VideoCatalogService {
    private VideoCatalogServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private VideoCatalogServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VideoCatalogServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new VideoCatalogServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void submitUploadedVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SUBMIT_UPLOADED_VIDEO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void submitYouTubeVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SUBMIT_YOU_TUBE_VIDEO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO_PREVIEWS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getLatestVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_LATEST_VIDEO_PREVIEWS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getUserVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_USER_VIDEO_PREVIEWS, getCallOptions()), request, responseObserver);
    }
  }

  public static class VideoCatalogServiceBlockingStub extends io.grpc.stub.AbstractStub<VideoCatalogServiceBlockingStub>
      implements VideoCatalogServiceBlockingClient {
    private VideoCatalogServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private VideoCatalogServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VideoCatalogServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new VideoCatalogServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse submitUploadedVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SUBMIT_UPLOADED_VIDEO, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse submitYouTubeVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SUBMIT_YOU_TUBE_VIDEO, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse getVideo(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_VIDEO, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse getVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_VIDEO_PREVIEWS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse getLatestVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_LATEST_VIDEO_PREVIEWS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse getUserVideoPreviews(killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_USER_VIDEO_PREVIEWS, getCallOptions(), request);
    }
  }

  public static class VideoCatalogServiceFutureStub extends io.grpc.stub.AbstractStub<VideoCatalogServiceFutureStub>
      implements VideoCatalogServiceFutureClient {
    private VideoCatalogServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private VideoCatalogServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected VideoCatalogServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new VideoCatalogServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse> submitUploadedVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SUBMIT_UPLOADED_VIDEO, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse> submitYouTubeVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SUBMIT_YOU_TUBE_VIDEO, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse> getVideo(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse> getVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO_PREVIEWS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse> getLatestVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_LATEST_VIDEO_PREVIEWS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse> getUserVideoPreviews(
        killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_USER_VIDEO_PREVIEWS, getCallOptions()), request);
    }
  }

  private static final int METHODID_SUBMIT_UPLOADED_VIDEO = 0;
  private static final int METHODID_SUBMIT_YOU_TUBE_VIDEO = 1;
  private static final int METHODID_GET_VIDEO = 2;
  private static final int METHODID_GET_VIDEO_PREVIEWS = 3;
  private static final int METHODID_GET_LATEST_VIDEO_PREVIEWS = 4;
  private static final int METHODID_GET_USER_VIDEO_PREVIEWS = 5;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final VideoCatalogService serviceImpl;
    private final int methodId;

    public MethodHandlers(VideoCatalogService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SUBMIT_UPLOADED_VIDEO:
          serviceImpl.submitUploadedVideo((killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse>) responseObserver);
          break;
        case METHODID_SUBMIT_YOU_TUBE_VIDEO:
          serviceImpl.submitYouTubeVideo((killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse>) responseObserver);
          break;
        case METHODID_GET_VIDEO:
          serviceImpl.getVideo((killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse>) responseObserver);
          break;
        case METHODID_GET_VIDEO_PREVIEWS:
          serviceImpl.getVideoPreviews((killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse>) responseObserver);
          break;
        case METHODID_GET_LATEST_VIDEO_PREVIEWS:
          serviceImpl.getLatestVideoPreviews((killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse>) responseObserver);
          break;
        case METHODID_GET_USER_VIDEO_PREVIEWS:
          serviceImpl.getUserVideoPreviews((killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse>) responseObserver);
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
      final VideoCatalogService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_SUBMIT_UPLOADED_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoResponse>(
                serviceImpl, METHODID_SUBMIT_UPLOADED_VIDEO)))
        .addMethod(
          METHOD_SUBMIT_YOU_TUBE_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoResponse>(
                serviceImpl, METHODID_SUBMIT_YOU_TUBE_VIDEO)))
        .addMethod(
          METHOD_GET_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoResponse>(
                serviceImpl, METHODID_GET_VIDEO)))
        .addMethod(
          METHOD_GET_VIDEO_PREVIEWS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsResponse>(
                serviceImpl, METHODID_GET_VIDEO_PREVIEWS)))
        .addMethod(
          METHOD_GET_LATEST_VIDEO_PREVIEWS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsResponse>(
                serviceImpl, METHODID_GET_LATEST_VIDEO_PREVIEWS)))
        .addMethod(
          METHOD_GET_USER_VIDEO_PREVIEWS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest,
              killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsResponse>(
                serviceImpl, METHODID_GET_USER_VIDEO_PREVIEWS)))
        .build();
  }
}
