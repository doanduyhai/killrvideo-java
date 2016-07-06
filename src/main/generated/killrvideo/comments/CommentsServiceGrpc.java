package killrvideo.comments;

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
 * Manages comments
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: comments/comments_service.proto")
public class CommentsServiceGrpc {

  private CommentsServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.comments.CommentsService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest,
      killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> METHOD_COMMENT_ON_VIDEO =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.comments.CommentsService", "CommentOnVideo"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest,
      killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> METHOD_GET_USER_COMMENTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.comments.CommentsService", "GetUserComments"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest,
      killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> METHOD_GET_VIDEO_COMMENTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.comments.CommentsService", "GetVideoComments"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CommentsServiceStub newStub(io.grpc.Channel channel) {
    return new CommentsServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CommentsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CommentsServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static CommentsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CommentsServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Manages comments
   * </pre>
   */
  public static interface CommentsService {

    /**
     * <pre>
     * Add a new comment to a video
     * </pre>
     */
    public void commentOnVideo(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> responseObserver);

    /**
     * <pre>
     * Get comments made by a user
     * </pre>
     */
    public void getUserComments(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> responseObserver);

    /**
     * <pre>
     * Get comments made on a video
     * </pre>
     */
    public void getVideoComments(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractCommentsService implements CommentsService, io.grpc.BindableService {

    @java.lang.Override
    public void commentOnVideo(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_COMMENT_ON_VIDEO, responseObserver);
    }

    @java.lang.Override
    public void getUserComments(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_USER_COMMENTS, responseObserver);
    }

    @java.lang.Override
    public void getVideoComments(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_VIDEO_COMMENTS, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return CommentsServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Manages comments
   * </pre>
   */
  public static interface CommentsServiceBlockingClient {

    /**
     * <pre>
     * Add a new comment to a video
     * </pre>
     */
    public killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse commentOnVideo(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request);

    /**
     * <pre>
     * Get comments made by a user
     * </pre>
     */
    public killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse getUserComments(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request);

    /**
     * <pre>
     * Get comments made on a video
     * </pre>
     */
    public killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse getVideoComments(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request);
  }

  /**
   * <pre>
   * Manages comments
   * </pre>
   */
  public static interface CommentsServiceFutureClient {

    /**
     * <pre>
     * Add a new comment to a video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> commentOnVideo(
        killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request);

    /**
     * <pre>
     * Get comments made by a user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> getUserComments(
        killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request);

    /**
     * <pre>
     * Get comments made on a video
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> getVideoComments(
        killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request);
  }

  public static class CommentsServiceStub extends io.grpc.stub.AbstractStub<CommentsServiceStub>
      implements CommentsService {
    private CommentsServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommentsServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommentsServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommentsServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void commentOnVideo(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COMMENT_ON_VIDEO, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getUserComments(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_USER_COMMENTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getVideoComments(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO_COMMENTS, getCallOptions()), request, responseObserver);
    }
  }

  public static class CommentsServiceBlockingStub extends io.grpc.stub.AbstractStub<CommentsServiceBlockingStub>
      implements CommentsServiceBlockingClient {
    private CommentsServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommentsServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommentsServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommentsServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse commentOnVideo(killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COMMENT_ON_VIDEO, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse getUserComments(killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_USER_COMMENTS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse getVideoComments(killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_VIDEO_COMMENTS, getCallOptions(), request);
    }
  }

  public static class CommentsServiceFutureStub extends io.grpc.stub.AbstractStub<CommentsServiceFutureStub>
      implements CommentsServiceFutureClient {
    private CommentsServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommentsServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommentsServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommentsServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse> commentOnVideo(
        killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COMMENT_ON_VIDEO, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse> getUserComments(
        killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_USER_COMMENTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse> getVideoComments(
        killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_VIDEO_COMMENTS, getCallOptions()), request);
    }
  }

  private static final int METHODID_COMMENT_ON_VIDEO = 0;
  private static final int METHODID_GET_USER_COMMENTS = 1;
  private static final int METHODID_GET_VIDEO_COMMENTS = 2;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CommentsService serviceImpl;
    private final int methodId;

    public MethodHandlers(CommentsService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_COMMENT_ON_VIDEO:
          serviceImpl.commentOnVideo((killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse>) responseObserver);
          break;
        case METHODID_GET_USER_COMMENTS:
          serviceImpl.getUserComments((killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse>) responseObserver);
          break;
        case METHODID_GET_VIDEO_COMMENTS:
          serviceImpl.getVideoComments((killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse>) responseObserver);
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
      final CommentsService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_COMMENT_ON_VIDEO,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest,
              killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoResponse>(
                serviceImpl, METHODID_COMMENT_ON_VIDEO)))
        .addMethod(
          METHOD_GET_USER_COMMENTS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest,
              killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsResponse>(
                serviceImpl, METHODID_GET_USER_COMMENTS)))
        .addMethod(
          METHOD_GET_VIDEO_COMMENTS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest,
              killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsResponse>(
                serviceImpl, METHODID_GET_VIDEO_COMMENTS)))
        .build();
  }
}
