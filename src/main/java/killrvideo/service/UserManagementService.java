package killrvideo.service;


import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.utils.MoreFutures;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import killrvideo.entity.CommentsByVideo;
import killrvideo.entity.Schema;
import killrvideo.utils.FutureUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

//import info.archinnov.achilles.generated.manager.UserCredentials_Manager;
//import info.archinnov.achilles.generated.manager.User_Manager;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.core.querybuilder.QueryBuilder;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.User;
import killrvideo.entity.UserCredentials;
import killrvideo.events.CassandraMutationError;
import killrvideo.user_management.UserManagementServiceGrpc.AbstractUserManagementService;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.utils.HashUtils;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;

@Service
public class UserManagementService extends AbstractUserManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

    @Inject
    Mapper<UserCredentials> userCredentialsMapper;

    @Inject
    Mapper<User> userMapper;

    @Inject
    MappingManager manager;

    @Inject
    EventBus eventBus;

    @Inject
    KillrVideoInputValidator validator;

    Session session;
    private String usersTableName;
    private String userCredentialsTableName;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        usersTableName = userMapper.getTableMetadata().getName();
        userCredentialsTableName = userCredentialsMapper.getTableMetadata().getName();
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

        LOGGER.debug("-----Start creating user-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        Date now = new Date();
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        /**
         * Boolean value to know whether we have intercepted
         * a LightWeightTransaction error in the LightWeightTransaction
         * result listener
         */
        final AtomicBoolean emailAlreadyExists = new AtomicBoolean(false);

        /** Trim the password **/
        final String hashedPassword = HashUtils.hashPassword(request.getPassword().trim());
        final String email = request.getEmail();
        final String exceptionMessage = String.format("Exception creating user because it already exists with email %s", email);

        /**
         * We insert first the credentials since
         * the LWT condition is on the user email
         */
        //UserCredentials credentials = new UserCredentials(email, hashedPassword, userId);
        BuiltStatement checkEmailQuery = QueryBuilder
                .insertInto(Schema.KEYSPACE, userCredentialsTableName)
                .value("email", email)
                .value("pass", hashedPassword)
                .value("userid", userId)
                .ifNotExists(); // use lightweight transaction

        ResultSet checkEmailResult = session.execute(checkEmailQuery);

        /** Check the result of the LWT, if it's false
         * the email already exists within our user_credentials
         * table and must not be duplicated.
         * Note the use of wasApplied(), this is a convenience method
         * described here ->
         * http://docs.datastax.com/en/drivers/java/2.1/com/datastax/driver/core/ResultSet.html#wasApplied--
         * that allows an easy check of a conditional statement.
         */
        if (!checkEmailResult.wasApplied()) {
            emailAlreadyExists.getAndSet(true);
            responseObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(exceptionMessage).asRuntimeException());
            LOGGER.debug("exception is: " + exceptionMessage);
        }

        /**
         * No LWT error, we can proceed further
         */
        if (!emailAlreadyExists.get()) {
            /*Statement userInsert = userMapper
                    .saveQuery(new User(userId, request.getFirstName(), request.getLastName(), email, now));*/
            BuiltStatement userInsert = QueryBuilder
                    .insertInto(Schema.KEYSPACE, usersTableName)
                    .value("userid", userId)
                    .value("firstname", request.getFirstName())
                    .value("lastname", request.getLastName())
                    .value("email", email)
                    .value("created_date", now)
                    .ifNotExists(); // use lightweight transaction

            ResultSetFuture userResultsFuture = session.executeAsync(userInsert);
            Futures.addCallback(userResultsFuture,
                    new FutureCallback<ResultSet>() {
                        @Override
                        public void onSuccess(@Nullable ResultSet result) {

                            /** Check to see if userInsert was applied.
                             * userId should be unique, if not, the insert
                             * should fail
                             */
                            if (!result.wasApplied()) {
                                Throwable t = new Throwable("User ID already exists");
                                LOGGER.error("Exception creating user : " + mergeStackTrace(t));

                                eventBus.post(new CassandraMutationError(request, t));
                                responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());

                            } else {
                                LOGGER.debug("User id is unique, creating user");
                                eventBus.post(UserCreated.newBuilder()
                                        .setUserId(request.getUserId())
                                        .setEmail(email)
                                        .setFirstName(request.getFirstName())
                                        .setLastName(request.getLastName())
                                        .setTimestamp(TypeConverter.instantToTimeStamp(now.toInstant()))
                                        .build());
                                responseObserver.onNext(CreateUserResponse.newBuilder().build());
                                responseObserver.onCompleted();
                            }

                            LOGGER.debug("End creating user");
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            LOGGER.error("Exception creating user : " + mergeStackTrace(t));

                            eventBus.post(new CassandraMutationError(request, t));
                            responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());
                        }
                    },
                    MoreExecutors.sameThreadExecutor()
            );
        }

    }

    @Override
    public void verifyCredentials(VerifyCredentialsRequest request, StreamObserver<VerifyCredentialsResponse> responseObserver) {

        LOGGER.debug("------Start verifying user credentials------");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        /**
         * Since email is the partitionKey for the UserCredentials
         * entity I can simply pass it to the mapper get() method
         * to get my result
         */
        final UserCredentials one = userCredentialsMapper
                .get(request.getEmail());

        if (one == null || !HashUtils.isPasswordValid(request.getPassword(), one.getPassword())) {
            final String errorMessage = "Email address or password are not correct.";

            LOGGER.error(errorMessage);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(errorMessage).asRuntimeException());

        } else {
            responseObserver.onNext(VerifyCredentialsResponse
                    .newBuilder()
                    .setUserId(TypeConverter.uuidToUuid(one.getUserid()))
                    .build());
            responseObserver.onCompleted();

            LOGGER.debug("End verifying user credentials");
        }

    }

    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<GetUserProfileResponse> responseObserver) {

        LOGGER.debug("------Start getting user profile------");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();

        if (request.getUserIdsCount() == 0 || CollectionUtils.isEmpty(request.getUserIdsList())) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

            LOGGER.debug("No user id provided");

            return;
        }

        final UUID[] userIds = request
                .getUserIdsList()
                .stream()
                .map(uuid -> UUID.fromString(uuid.getValue()))
                .toArray(size -> new UUID[size]);

        LOGGER.debug("userId list is: " + userIds[0]);

        /**
         * Instead of firing multiple async SELECT, we can as well use
         * the IN(..) clause to fetch multiple user infos. It is recommended
         * to limit the number of values inside the IN clause to a dozen
         */
        BuiltStatement bs = QueryBuilder
                .select().all()
                .from(Schema.KEYSPACE, usersTableName)
                .where(QueryBuilder.in("userid", userIds));

        ResultSetFuture future = session.executeAsync(bs);
        FutureUtils.buildCompletableFuture(future)
                .handle((entities, ex) -> {
                    Result<User> users = userMapper.map(entities);

                    if (users != null) {
                        users.all().stream().forEach(user -> builder.addProfiles(user.toUserProfile()));
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting user profile");

                    } else if (ex != null) {

                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(ex));

                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    }
                    return entities;
                });

//        BuiltStatement bs = QueryBuilder
//                .select().all()
//                .from(Schema.KEYSPACE,"users")
//                .where(QueryBuilder.in("userid",userIds));
//
//        ResultSetFuture future = session.executeAsync(bs);
//        Futures.addCallback(future,
//                new FutureCallback<ResultSet>() {
//                    @Override
//                    public void onSuccess(@Nullable ResultSet result) {
//                        Result<User> users = userMapper.map(result);
//                        users.forEach(user -> builder.addProfiles(user.toUserProfile()));
//                        responseObserver.onNext(builder.build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End getting user profile");
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(t));
//
//                        responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());
//                    }
//                },
//                MoreExecutors.sameThreadExecutor()
//        );
    }
}
