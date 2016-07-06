package killrvideo.user_management;

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
 * The service responsible for managing user information
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: user-management/user_management_service.proto")
public class UserManagementServiceGrpc {

  private UserManagementServiceGrpc() {}

  public static final String SERVICE_NAME = "killrvideo.user_management.UserManagementService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest,
      killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> METHOD_CREATE_USER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.user_management.UserManagementService", "CreateUser"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest,
      killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> METHOD_VERIFY_CREDENTIALS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.user_management.UserManagementService", "VerifyCredentials"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest,
      killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> METHOD_GET_USER_PROFILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "killrvideo.user_management.UserManagementService", "GetUserProfile"),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserManagementServiceStub newStub(io.grpc.Channel channel) {
    return new UserManagementServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserManagementServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UserManagementServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static UserManagementServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UserManagementServiceFutureStub(channel);
  }

  /**
   * <pre>
   * The service responsible for managing user information
   * </pre>
   */
  public static interface UserManagementService {

    /**
     * <pre>
     * Creates a new user
     * </pre>
     */
    public void createUser(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> responseObserver);

    /**
     * <pre>
     * Verify a user's username and password
     * </pre>
     */
    public void verifyCredentials(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> responseObserver);

    /**
     * <pre>
     * Gets a user or group of user's profiles
     * </pre>
     */
    public void getUserProfile(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractUserManagementService implements UserManagementService, io.grpc.BindableService {

    @java.lang.Override
    public void createUser(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_USER, responseObserver);
    }

    @java.lang.Override
    public void verifyCredentials(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_VERIFY_CREDENTIALS, responseObserver);
    }

    @java.lang.Override
    public void getUserProfile(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_USER_PROFILE, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return UserManagementServiceGrpc.bindService(this);
    }
  }

  /**
   * <pre>
   * The service responsible for managing user information
   * </pre>
   */
  public static interface UserManagementServiceBlockingClient {

    /**
     * <pre>
     * Creates a new user
     * </pre>
     */
    public killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse createUser(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request);

    /**
     * <pre>
     * Verify a user's username and password
     * </pre>
     */
    public killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse verifyCredentials(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request);

    /**
     * <pre>
     * Gets a user or group of user's profiles
     * </pre>
     */
    public killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse getUserProfile(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request);
  }

  /**
   * <pre>
   * The service responsible for managing user information
   * </pre>
   */
  public static interface UserManagementServiceFutureClient {

    /**
     * <pre>
     * Creates a new user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> createUser(
        killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request);

    /**
     * <pre>
     * Verify a user's username and password
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> verifyCredentials(
        killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request);

    /**
     * <pre>
     * Gets a user or group of user's profiles
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> getUserProfile(
        killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request);
  }

  public static class UserManagementServiceStub extends io.grpc.stub.AbstractStub<UserManagementServiceStub>
      implements UserManagementService {
    private UserManagementServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserManagementServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserManagementServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserManagementServiceStub(channel, callOptions);
    }

    @java.lang.Override
    public void createUser(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_USER, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void verifyCredentials(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_VERIFY_CREDENTIALS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getUserProfile(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request,
        io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_USER_PROFILE, getCallOptions()), request, responseObserver);
    }
  }

  public static class UserManagementServiceBlockingStub extends io.grpc.stub.AbstractStub<UserManagementServiceBlockingStub>
      implements UserManagementServiceBlockingClient {
    private UserManagementServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserManagementServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserManagementServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserManagementServiceBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse createUser(killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_USER, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse verifyCredentials(killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_VERIFY_CREDENTIALS, getCallOptions(), request);
    }

    @java.lang.Override
    public killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse getUserProfile(killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_USER_PROFILE, getCallOptions(), request);
    }
  }

  public static class UserManagementServiceFutureStub extends io.grpc.stub.AbstractStub<UserManagementServiceFutureStub>
      implements UserManagementServiceFutureClient {
    private UserManagementServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UserManagementServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserManagementServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UserManagementServiceFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse> createUser(
        killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_USER, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse> verifyCredentials(
        killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_VERIFY_CREDENTIALS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse> getUserProfile(
        killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_USER_PROFILE, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_USER = 0;
  private static final int METHODID_VERIFY_CREDENTIALS = 1;
  private static final int METHODID_GET_USER_PROFILE = 2;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UserManagementService serviceImpl;
    private final int methodId;

    public MethodHandlers(UserManagementService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_USER:
          serviceImpl.createUser((killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse>) responseObserver);
          break;
        case METHODID_VERIFY_CREDENTIALS:
          serviceImpl.verifyCredentials((killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse>) responseObserver);
          break;
        case METHODID_GET_USER_PROFILE:
          serviceImpl.getUserProfile((killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest) request,
              (io.grpc.stub.StreamObserver<killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse>) responseObserver);
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
      final UserManagementService serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_CREATE_USER,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest,
              killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse>(
                serviceImpl, METHODID_CREATE_USER)))
        .addMethod(
          METHOD_VERIFY_CREDENTIALS,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest,
              killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse>(
                serviceImpl, METHODID_VERIFY_CREDENTIALS)))
        .addMethod(
          METHOD_GET_USER_PROFILE,
          asyncUnaryCall(
            new MethodHandlers<
              killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest,
              killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse>(
                serviceImpl, METHODID_GET_USER_PROFILE)))
        .build();
  }
}
