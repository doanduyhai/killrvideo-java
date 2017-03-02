package killrvideo.service;


import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import killrvideo.entity.Schema;
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
import com.datastax.driver.core.Row;


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

    //:TODO Fix this
    /*
    @Inject
    UserCredentials_Manager userCredentialsManager;

    @Inject
    User_Manager userManager;
    */

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

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

        LOGGER.debug("Start creating user");

        //userMapper = manager.mapper(User.class);
        //userCredentialsMapper = manager.mapper(UserCredentials.class);

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

//        /**
//         * We insert first the credentials since
//         * the LWT condition is on the user email
//         */
//        userCredentialsManager
//                .crud()
//                .insert(new UserCredentials(email, hashedPassword, userId))
//                .ifNotExists()
//                .withLwtResultListener(error -> {
//                        LOGGER.error(exceptionMessage);
//                        emailAlreadyExists.getAndSet(true);
//                        responseObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(exceptionMessage).asRuntimeException());
//                })
//                .execute();

        // By using saveQuery instead save I get access to the
        // result set and other options
        Statement credentialsInsert = userCredentialsMapper
                .saveQuery(new UserCredentials(email, hashedPassword, userId));

        ResultSet results = manager.getSession().execute(credentialsInsert);
        Result<UserCredentials> userCredentials = userCredentialsMapper.map(results);

        LOGGER.debug("Credentials results: " + userCredentials.toString());


        /**
         * No LWT error, we can proceed further
         */
//        if (emailAlreadyExists.get() == false) {
//            final AtomicBoolean userIdAlreadyExists = new AtomicBoolean(false);
//
//            userManager
//                    .crud()
//                    .insert(new User(userId, request.getFirstName(), request.getLastName(), email, now))
//                    .usingTimestamp(now.getTime())
//                    .withLwtResultListener(lwtResult -> {
//                        LOGGER.error(exceptionMessage);
//                        userIdAlreadyExists.getAndSet(true);
//                        responseObserver.onError(Status.INTERNAL.augmentDescription(exceptionMessage).asRuntimeException());
//                    })
//                    .executeAsync()
//                    .handle((rs, ex) -> {
//                        /**
//                         * Only return positive response if user id did not exist
//                         */
//                        if (ex == null && userIdAlreadyExists.get() == false) {
//                            eventBus.post(UserCreated.newBuilder()
//                                    .setUserId(request.getUserId())
//                                    .setEmail(email)
//                                    .setFirstName(request.getFirstName())
//                                    .setLastName(request.getLastName())
//                                    .setTimestamp(TypeConverter.instantToTimeStamp(now.toInstant()))
//                                    .build());
//                            responseObserver.onNext(CreateUserResponse.newBuilder().build());
//                            responseObserver.onCompleted();
//
//                            LOGGER.debug("End creating user");
//
//                        } else if(ex != null){
//                            LOGGER.error("Exception creating user : " + mergeStackTrace(ex));
//
//                            eventBus.post(new CassandraMutationError(request, ex));
//                            responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
//                        }
//                        return rs;
//                    });
//        }

    }

    @Override
    public void verifyCredentials(VerifyCredentialsRequest request, StreamObserver<VerifyCredentialsResponse> responseObserver) {

        LOGGER.debug("Start verifying user credentials");

        //userCredentialsMapper = manager.mapper(UserCredentials.class);

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

//        final UserCredentials one = userCredentialsManager
//                .crud()
//                .findById(request.getEmail())
//                .get();

        // Since email is the partitionKey for the UserCredentials
        // entity I can simply pass it to the mapper get() method
        // to get my result
        final UserCredentials one = userCredentialsMapper
                .get(request.getEmail());

        if (one == null || !HashUtils.isPasswordValid(request.getPassword(), one.getPassword())) {

            LOGGER.error("Email address or password are not correct.");

            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Email address or password are not correct.").asRuntimeException());

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

        LOGGER.debug("Start getting user profile");

        //userMapper = manager.mapper(User.class);

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


        // Loop through the userIds and grab data.  This is just one way to do this
        // and I haven't decided on the best method just yet
        for(UUID id : userIds) {
            LOGGER.debug("userId is: " + id);
            User user = userMapper.get(id);
            LOGGER.debug("user entity is: " + user);

            // Try direct method
            Statement query = QueryBuilder
                    .select()
                    .from(Schema.KEYSPACE,"users");
//                    .where(QueryBuilder.eq("userid",id));

            LOGGER.debug("Keyspace is: " + query.getKeyspace());
            LOGGER.debug("Query is: " + query.toString());

            ResultSet results = manager.getSession().execute(query);
            Result<User> users = userMapper.map(results);

            for(User u : users) {
                LOGGER.debug("user data is: " + u.getEmail());
                // Attempt to re-save these same users to see if something like
                // IfNotExists() is inherit within the mapper
                userMapper.save(u);
            }
        }

//        /**
//         * Instead of firing multiple async SELECT, we can as well use
//         * the IN(..) clause to fetch multiple user infos. It is recommended
//         * to limit the number of values inside the IN clause to a dozen
//         */
//        userManager
//                .dsl()
//                .select()
//                .allColumns_FromBaseTable()
//                .where()
//                .userid().IN(userIds)
//                .getListAsync()
//                .handle((entities, ex) -> {
//                    if (entities != null) {
//                        entities.stream().forEach(entity -> builder.addProfiles(entity.toUserProfile()));
//                        responseObserver.onNext(builder.build());
//                        responseObserver.onCompleted();
//
//                        LOGGER.debug("End getting user profile");
//
//                    } else if (ex != null) {
//
//                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(ex));
//
//                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());
//
//                    }
//                    return entities;
//                });

        BuiltStatement bs = QueryBuilder
                .select().all()
                .from(Schema.KEYSPACE,"users")
                .where(QueryBuilder.in("userid",userIds));

        ResultSetFuture future = manager.getSession().executeAsync(bs);
        Futures.addCallback(future,
                new FutureCallback<ResultSet>() {
                    @Override
                    public void onSuccess(@Nullable ResultSet result) {
                        Result<User> users = userMapper.map(result);
                        users.forEach(user -> builder.addProfiles(user.toUserProfile()));
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting user profile");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(t));

                        responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());
                    }
                },
                MoreExecutors.sameThreadExecutor()
        );
    }
}
