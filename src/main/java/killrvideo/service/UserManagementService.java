package killrvideo.service;


import static killrvideo.utils.ExceptionUtils.mergeStackTrace;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import killrvideo.entity.Schema;
import killrvideo.utils.FutureUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

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

    private Session session;
    private String usersTableName;
    private String userCredentialsTableName;
    private PreparedStatement createUser_checkEmailPrepared;
    private PreparedStatement createUser_insertUserPrepared;
    private PreparedStatement getUserProfile_getUsersPrepared;

    @PostConstruct
    public void init(){
        this.session = manager.getSession();

        usersTableName = userMapper.getTableMetadata().getName();
        userCredentialsTableName = userCredentialsMapper.getTableMetadata().getName();

        createUser_checkEmailPrepared = session.prepare(
                QueryBuilder
                        .insertInto(Schema.KEYSPACE, userCredentialsTableName)
                        .value("email", QueryBuilder.bindMarker())
                        .value("password", QueryBuilder.bindMarker())
                        .value("userid", QueryBuilder.bindMarker())
                        .ifNotExists() // use lightweight transaction
        );

        createUser_insertUserPrepared = session.prepare(
                QueryBuilder
                        .insertInto(Schema.KEYSPACE, usersTableName)
                        .value("userid", QueryBuilder.bindMarker())
                        .value("firstname", QueryBuilder.bindMarker())
                        .value("lastname", QueryBuilder.bindMarker())
                        .value("email", QueryBuilder.bindMarker())
                        .value("created_date", QueryBuilder.bindMarker())
                        .ifNotExists() // use lightweight transaction
        );

        getUserProfile_getUsersPrepared = session.prepare(
                QueryBuilder
                        .select()
                        .all()
                        .from(Schema.KEYSPACE, usersTableName)
                        .where(QueryBuilder.in("userid", QueryBuilder.bindMarker()))
        );
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

        LOGGER.debug("-----Start creating user-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        final Date now = new Date();
        final UUID userId = UUID.fromString(request.getUserId().getValue());

        /** Trim the password **/
        final String hashedPassword = HashUtils.hashPassword(request.getPassword().trim());
        final String email = request.getEmail();
        final String exceptionMessage = String.format("Exception creating user because it already exists with email %s", email);

        /**
         * We insert first the credentials since
         * the LWT condition is on the user email
         *
         * Note, the LWT condition is set up at the prepared statement
         */
        final BoundStatement checkEmailQuery = createUser_checkEmailPrepared.bind()
                .setString("email", email)
                .setString("password", hashedPassword)
                .setUUID("userid", userId);

        /**
         * Note that we have multiple executeAsync() calls in the following chain.
         * We check our user_credentials first, if that passes, we move onto inserting
         * the user into the users table.  Both cases use lightweight transactions
         * to ensure we are not duplicating already existing users within the database.
         */
        FutureUtils.buildCompletableFuture(session.executeAsync(checkEmailQuery))
                /**
                 * I use the *Async() version of .handle below because I am
                 * chaining multiple async futures.  In testing we found that chains like
                 * this would cause timeouts possibly from starvation.
                 */
                //TODO: Possibly refactor this section to make it easier to follow/read
                .handleAsync((rs, ex) -> {
                    try {
                        if (rs != null) {
                            /** Check the result of the LWT, if it's false
                             * the email already exists within our user_credentials
                             * table and must not be duplicated.
                             * Note the use of wasApplied(), this is a convenience method
                             * described here ->
                             * http://docs.datastax.com/en/drivers/java/3.2/com/datastax/driver/core/ResultSet.html#wasApplied--
                             * that allows an easy check of a conditional statement.
                             */
                            if (rs.wasApplied()) {
                                /**
                                 * No LWT error, we can proceed further
                                 * Execute our insert statement in an async
                                 * fashion as well and pass the result to the next
                                 * line in the chain
                                 */
                                final BoundStatement insertUser = createUser_insertUserPrepared.bind()
                                        .setUUID("userid", userId)
                                        .setString("firstname", request.getFirstName())
                                        .setString("lastname", request.getLastName())
                                        .setString("email", email)
                                        .setTimestamp("created_date", now);

                                return FutureUtils.buildCompletableFuture(session.executeAsync(insertUser));

                            } else { // throw in case our LWT fails
                                throw new Throwable(exceptionMessage);
                            }
                        } else { // throw in case our result set is null
                            throw new Throwable(ex);
                        }

                    } catch (Throwable t) {
                        final String message = t.getMessage();
                        responseObserver.onError(Status.INVALID_ARGUMENT.augmentDescription(message).asRuntimeException());
                        LOGGER.debug(this.getClass().getName() + ".createUser() " + message);

                        return null;
                    }
                })
                /**
                 * thenAccept in the same thread pool
                 */
                .thenAccept(rs -> {
                    try {
                        if (rs != null) {
                            /** Check to see if userInsert was applied.
                             * userId should be unique, if not, the insert
                             * should fail
                             */
                            if (rs.get().wasApplied()) {
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

                            } else {
                                throw new Throwable("User ID already exists");
                            }

                            LOGGER.debug("End creating user");
                        }

                    } catch (Throwable t) {
                        eventBus.post(new CassandraMutationError(request, t));
                        responseObserver.onError(Status.INTERNAL.withCause(t).asRuntimeException());
                        LOGGER.error(this.getClass().getName() + ".createUser() " + "Exception creating user : " + mergeStackTrace(t));
                    }
                });
    }

    @Override
    public void verifyCredentials(VerifyCredentialsRequest request, StreamObserver<VerifyCredentialsResponse> responseObserver) {

        LOGGER.debug("------Start verifying user credentials------");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        /**
         * Since email is the partitionKey for the UserCredentials
         * entity I can simply pass it to the mapper getAsync() method
         * to get my result
         */
        FutureUtils.buildCompletableFuture(userCredentialsMapper.getAsync(request.getEmail()))
                .handle((credential, ex) -> {
                    if (credential == null || !HashUtils.isPasswordValid(request.getPassword(), credential.getPassword())) {
                        final String errorMessage = "Email address or password are not correct.";

                        LOGGER.error(errorMessage);
                        responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(errorMessage).asRuntimeException());

                    } else {
                        responseObserver.onNext(VerifyCredentialsResponse
                                .newBuilder()
                                .setUserId(TypeConverter.uuidToUuid(credential.getUserid()))
                                .build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End verifying user credentials");
                    }
                    return credential;
                });
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

        /**
         * Instead of firing multiple async SELECT, we can as well use
         * the IN(..) clause to fetch multiple user infos. It is recommended
         * to limit the number of values inside the IN clause to a dozen
         */
        BoundStatement getUsersQuery = getUserProfile_getUsersPrepared.bind()
                .setList(0, Arrays.asList(userIds), UUID.class);

        FutureUtils.buildCompletableFuture(userMapper.mapAsync(session.executeAsync(getUsersQuery)))
                .handle((users, ex) -> {
                    if (users != null) {
                        users.forEach(user -> builder.addProfiles(user.toUserProfile()));
                        responseObserver.onNext(builder.build());
                        responseObserver.onCompleted();

                        LOGGER.debug("End getting user profile");

                    } else if (ex != null) {
                        LOGGER.error("Exception getting user profile : " + mergeStackTrace(ex));
                        responseObserver.onError(Status.INTERNAL.withCause(ex).asRuntimeException());

                    }
                    return users;
                });
    }
}
