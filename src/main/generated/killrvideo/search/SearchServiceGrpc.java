package killrvideo.search;

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
 * Searches for videos
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: search/search_service.proto")
public class SearchServiceGrpc {

  private SearchServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.search.SearchService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.search.SearchServiceOuterClass.SearchVideosRequest,
      killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> METHOD_SEARCH_VIDEOS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.search.SearchService", "SearchVideos"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.search.SearchServiceOuterClass.SearchVideosResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest,
      killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> METHOD_GET_QUERY_SUGGESTIONS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.search.SearchService", "GetQuerySuggestions"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SearchServiceStub newStub(io.grpc.Channel channel) {
    return new SearchServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SearchServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new SearchServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static SearchServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new SearchServiceFutureStub(channel);
  }

  /**
   * <pre>
   * Searches for videos
   * </pre>
   */
  public static interface SearchService {

    /**
     * <pre>
     * Searches for videos by a given query term
     * </pre>
     */
    public void searchVideos(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> responseObserver);

    /**
     * <pre>
     * Gets search query suggestions (could be used for typeahead support)
     * </pre>
     */
    public void getQuerySuggestions(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractSearchService implements SearchService, io.grpc.BindableService {

    @java.lang.Override
    public void searchVideos(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SEARCH_VIDEOS, responseObserver);
    }

    @java.lang.Override
    public void getQuerySuggestions(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_QUERY_SUGGESTIONS, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return SearchServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * Searches for videos
   * </pre>
   */
  public static interface SearchServiceBlockingClient {

    /**
     * <pre>
     * Searches for videos by a given query term
     * </pre>
     */
    public killrvideo.search.SearchServiceOuterClass.SearchVideosResponse searchVideos(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request);

    /**
     * <pre>
     * Gets search query suggestions (could be used for typeahead support)
     * </pre>
     */
    public killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse getQuerySuggestions(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request);
  }

  /**
   * <pre>
   * Searches for videos
   * </pre>
   */
  public static interface SearchServiceFutureClient {

    /**
     * <pre>
     * Searches for videos by a given query term
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> searchVideos(
        killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request);

    /**
     * <pre>
     * Gets search query suggestions (could be used for typeahead support)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> getQuerySuggestions(
        killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request);
  }

  public static class SearchServiceStub extends io.grpc.stub.AbstractStub<SearchServiceStub>
      implements SearchService {
    private SearchServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SearchServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SearchServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SearchServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void searchVideos(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SEARCH_VIDEOS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getQuerySuggestions(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_QUERY_SUGGESTIONS, getCallOptions()), request, responseObserver);
    }
  }

  public static class SearchServiceBlockingStub extends io.grpc.stub.AbstractStub<SearchServiceBlockingStub>
      implements SearchServiceBlockingClient {
    private SearchServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SearchServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SearchServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SearchServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.search.SearchServiceOuterClass.SearchVideosResponse searchVideos(killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SEARCH_VIDEOS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse getQuerySuggestions(killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_QUERY_SUGGESTIONS, getCallOptions(), request);
    }
  }

  public static class SearchServiceFutureStub extends io.grpc.stub.AbstractStub<SearchServiceFutureStub>
      implements SearchServiceFutureClient {
    private SearchServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private SearchServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SearchServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new SearchServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse> searchVideos(
        killrvideo.search.SearchServiceOuterClass.SearchVideosRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SEARCH_VIDEOS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse> getQuerySuggestions(
        killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_QUERY_SUGGESTIONS, getCallOptions()), request);
    }
  }

  private static final int METHODID_SEARCH_VIDEOS = 0;
  private static final int METHODID_GET_QUERY_SUGGESTIONS = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SearchService serviceImpl;
    private final int methodId;

    public MethodHandlers(SearchService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEARCH_VIDEOS:
          serviceImpl.searchVideos((killrvideo.search.SearchServiceOuterClass.SearchVideosRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.SearchVideosResponse>) responseObserver);
          break;
        case METHODID_GET_QUERY_SUGGESTIONS:
          serviceImpl.getQuerySuggestions((killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse>) responseObserver);
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
      final SearchService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_SEARCH_VIDEOS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.search.SearchServiceOuterClass.SearchVideosRequest,
              killrvideo.search.SearchServiceOuterClass.SearchVideosResponse>(
                serviceImpl, METHODID_SEARCH_VIDEOS)))
        .addMethod(
          METHOD_GET_QUERY_SUGGESTIONS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest,
              killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsResponse>(
                serviceImpl, METHODID_GET_QUERY_SUGGESTIONS)))
        .build();
  }
}
