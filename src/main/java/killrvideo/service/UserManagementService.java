package killrvideo.service;


import java.time.Instant;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;

import com.google.common.eventbus.EventBus;

import info.archinnov.achilles.generated.manager.UserCredentials_Manager;
import info.archinnov.achilles.generated.manager.User_Manager;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.User;
import killrvideo.entity.UserCredentials;
import killrvideo.user_management.UserManagementServiceGrpc.AbstractUserManagementService;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.utils.HashUtils;
import killrvideo.utils.TypeConverter;

public class UserManagementService extends AbstractUserManagementService {

    @Inject
    UserCredentials_Manager userCredentialsManager;

    @Inject
    User_Manager userManager;


    @Inject
    EventBus eventBus;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

        Date now = new Date();
        final String hashedPassword = HashUtils.hashPassword(request.getPassword());
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        userCredentialsManager
                .crud()
                .insert(new UserCredentials(request.getEmail(), hashedPassword, userId))
                .ifNotExists()
                .withLwtResultListener(error -> responseObserver.onError(new IllegalStateException("An user with that email address already exists")))
                .executeAsync();

        userManager
                .crud()
                .insert(new User(userId, request.getFirstName(), request.getLastName(), request.getEmail(), now))
                .usingTimestamp(now.getTime())
                .executeAsync()
                .handle((rs, ex) -> {
                    if (rs != null) {
                        eventBus.post(UserCreated.newBuilder()
                            .setUserId(request.getUserId())
                            .setEmail(request.getEmail())
                            .setFirstName(request.getFirstName())
                            .setLastName(request.getLastName())
                            .setTimestamp(TypeConverter.instantToTimeStamp(now.toInstant())));
                        responseObserver.onNext(CreateUserResponse.newBuilder().build());
                        responseObserver.onCompleted();
                    } else {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return rs;
                });
    }

    @Override
    public void verifyCredentials(VerifyCredentialsRequest request, StreamObserver<VerifyCredentialsResponse> responseObserver) {

        final UserCredentials one = userCredentialsManager
                .crud()
                .findById(request.getEmail())
                .get();

        if (one == null || !HashUtils.isPasswordValid(request.getPassword(), one.getPassword())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email address or password are not correct.").asRuntimeException());
        } else {
            responseObserver.onNext(VerifyCredentialsResponse
                    .newBuilder()
                    .setUserId(TypeConverter.uuidToUuid(one.getUserid()))
                    .build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<GetUserProfileResponse> responseObserver) {

        final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();

        if (request.getUserIdsCount() == 0 || CollectionUtils.isEmpty(request.getUserIdsList())) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getUserIdsCount() > 20) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Cannot get more than 20 user profiles at once").asRuntimeException());
            return;
        }

        final UUID[] userIds = request
                .getUserIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .toArray(size -> new UUID[size]);

        userManager
                .dsl()
                .select()
                .userid()
                .firstname()
                .lastname()
                .email()
                .fromBaseTable()
                .where()
                .userid_IN(userIds)
                .getListAsync()
                .handle((entities, ex) -> {
                    if (entities != null) {
                        entities.stream().forEach(entity -> builder.addProfiles(entity.toUserProfile()));
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();
                    } else if (ex != null) {
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
                    }
                    return entities;
                });
    }
}
