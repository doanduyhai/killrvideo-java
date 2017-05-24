package killrvideo.service;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.xqbase.etcd4j.EtcdClient;
import killrvideo.configuration.EtcdConfiguration;
import killrvideo.grpc.GrpcServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.Row;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;

//import info.archinnov.achilles.generated.ManagerFactory;
//import info.archinnov.achilles.generated.ManagerFactoryBuilder;
//import info.archinnov.achilles.junit.AchillesTestResource;
//import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import info.archinnov.achilles.script.ScriptExecutor;
import io.grpc.stub.StreamObserver;
import killrvideo.entity.User;
import killrvideo.entity.UserCredentials;
import killrvideo.suggested_videos.*;
import killrvideo.suggested_videos.SuggestedVideosService;
import killrvideo.user_management.UserManagementServiceOuterClass;
import killrvideo.user_management.UserManagementServiceOuterClass.*;
import killrvideo.user_management.events.UserManagementEvents;
import killrvideo.user_management.events.UserManagementEvents.UserCreated;
import killrvideo.utils.HashUtils;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.configuration.CassandraConfiguration;
import killrvideo.configuration.EtcdConfiguration.*;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementServiceTest {

    @Inject
    MappingManager manager;

    @Inject
    Mapper<UserCredentials> userCredentialsMapper;

    @Inject
    Mapper<User> userMapper;

    @Inject
    EtcdClient etcdClient;

//    @Rule
//    public AchillesTestResource<ManagerFactory> resource =  AchillesTestResourceBuilder
//            .forJunit()
//            .entityClassesToTruncate(User.class, UserCredentials.class)
//            .truncateBeforeAndAfterTest()
//            .createAndUseKeyspace("killrvideo")
//            .build((cluster, statementsCache) -> ManagerFactoryBuilder
//                    .builder(cluster)
//                    .doForceSchemaCreation(true)
//                    .withStatementsCache(statementsCache)
//                    .withBeanValidation(true)
//                    .withPostLoadBeanValidation(true)
//                    .build()
//            );

    private UserManagementService userManagementService;
    //final private ScriptExecutor scriptExecutor = new ScriptExecutor(manager.getSession());

    @Mock
    EventBus eventBus;

    @Captor
    ArgumentCaptor<UserCreated> captor = ArgumentCaptor.forClass(UserCreated.class);

    @Before
    public void setUp() {
        EtcdConfiguration etcdConfig = new EtcdConfiguration();
        EtcdClient etcdClient = etcdConfig.connectToEtcd();
        CassandraConfiguration config = new CassandraConfiguration();
        MappingManager manager = config.cassandraNativeClusterProduction();

        userManagementService = new UserManagementService();
        //userManagementService.userManager = resource.getManagerFactory().forUser();
        //userManagementService.userCredentialsManager = resource.getManagerFactory().forUserCredentials();
        userManagementService.manager = manager;
        userManagementService.userMapper = userMapper;
        userManagementService.userCredentialsMapper = userCredentialsMapper;
        userManagementService.validator = new KillrVideoInputValidator();
        userManagementService.eventBus = eventBus;
    }

    @Test
    public void should_create_user() throws Exception {
        //Given
        UUID userId = UUID.randomUUID();

        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail("test@gmail.com")
                .setFirstName("John")
                .setLastName("DOE")
                .setUserId(TypeConverter.uuidToUuid(userId))
                .setPassword("password")
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicReference<CreateUserResponse> response = new AtomicReference<>(null);

        StreamObserver<CreateUserResponse> streamObserver = new StreamObserver<CreateUserResponse>() {
            @Override
            public void onNext(CreateUserResponse value) {
                response.getAndSet(value);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                latch.countDown();
                completed.getAndSet(true);
            }
        };
        //When
        userManagementService.createUser(request, streamObserver);
        latch.await();

        //Then
        assertThat(completed.get()).isTrue();
        final CreateUserResponse createUserResponse = response.get();
        assertThat(createUserResponse).isNotNull();

        verify(eventBus).post(captor.capture());

        final UserCreated userCreated = captor.getValue();
        assertThat(userCreated).isNotNull();

        assertThat(userCreated.getUserId().getValue()).isEqualTo(userId.toString());
        assertThat(userCreated.getFirstName()).isEqualTo("John");
        assertThat(userCreated.getLastName()).isEqualTo("DOE");
        assertThat(userCreated.getEmail()).isEqualTo("test@gmail.com");
        assertThat(userCreated.getTimestamp()).isNotNull();
    }
//
//    @Test
//    public void should_fail_creating_existing_user() throws Exception {
//        //Given
//        //resource.getScriptExecutor().executeScript("userManagementService/insertExistingUserCredentials.cql");
//        scriptExecutor.executeScript("userManagementService/insertExistingUserCredentials.cql");
//
//        UUID userId = UUID.fromString("00000000-1111-0000-0000-000000000000");
//        CreateUserRequest request = CreateUserRequest.newBuilder()
//                .setEmail("existing_user@gmail.com")
//                .setFirstName("John")
//                .setLastName("DOE")
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setPassword("xxx")
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<Throwable> response = new AtomicReference<>(null);
//
//        StreamObserver<CreateUserResponse> streamObserver = new StreamObserver<CreateUserResponse>() {
//            @Override
//            public void onNext(CreateUserResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                response.getAndSet(t);
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//
//            @Override
//            public void onCompleted() {
//
//            }
//        };
//        //When
//        userManagementService.createUser(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        assertThat(response.get()).isNotNull();
//        assertThat(response.get().getMessage()).contains("Exception creating user because it already exists with email existing_user@gmail.com");
//
//        verify(eventBus, never()).post(any(UserCreated.class));
//
//        //final List<Row> rows = resource.getNativeSession().execute("SELECT * FROM killrvideo.users").all();
//        final List<Row> rows = manager.getSession().execute("SELECT * FROM killrvideo.users").all();
//        assertThat(rows).isEmpty();
//    }
//
//    @Test
//    public void should_validate_create_user_request() throws Exception {
//        //Given
//        CreateUserRequest request = CreateUserRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<CreateUserResponse> streamObserver = new StreamObserver<CreateUserResponse>() {
//            @Override
//            public void onNext(CreateUserResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        userManagementService.createUser(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("user id");
//        assertThat(error.get().getMessage()).contains("email");
//        assertThat(error.get().getMessage()).contains("password");
//    }
//
//    @Test
//    public void should_verify_user_credentials() throws Exception {
//        //Given
//        final String hashPassword = HashUtils.hashPassword("password");
//        //resource.getScriptExecutor().executeScriptTemplate("userManagementService/insertExistingCredentials.cql", ImmutableMap.of("hash", "'" + hashPassword + "'"));
//        scriptExecutor.executeScriptTemplate("userManagementService/insertExistingCredentials.cql", ImmutableMap.of("hash", "'" + hashPassword + "'"));
//
//        VerifyCredentialsRequest request = VerifyCredentialsRequest.newBuilder()
//                .setEmail("to_verify@gmail.com")
//                .setPassword("password")
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<VerifyCredentialsResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<VerifyCredentialsResponse> streamObserver = new StreamObserver<VerifyCredentialsResponse>() {
//            @Override
//            public void onNext(VerifyCredentialsResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//        //When
//        userManagementService.verifyCredentials(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final VerifyCredentialsResponse verifyCredentialsResponse = response.get();
//        assertThat(verifyCredentialsResponse).isNotNull();
//        assertThat(verifyCredentialsResponse.getUserId().getValue()).isEqualTo("00000000-1111-0000-0000-000000000000");
//    }
//
//    @Test
//    public void should_error_if_password_mismatch() throws Exception {
//        //Given
//        final String hashPassword = HashUtils.hashPassword("password");
//        //resource.getScriptExecutor().executeScriptTemplate("userManagementService/insertExistingCredentials.cql", ImmutableMap.of("hash", "'" + hashPassword + "'"));
//        scriptExecutor.executeScriptTemplate("userManagementService/insertExistingCredentials.cql", ImmutableMap.of("hash", "'" + hashPassword + "'"));
//
//        VerifyCredentialsRequest request = VerifyCredentialsRequest.newBuilder()
//                .setEmail("to_verify@gmail.com")
//                .setPassword("wrong")
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<Throwable> response = new AtomicReference<>(null);
//
//        StreamObserver<VerifyCredentialsResponse> streamObserver = new StreamObserver<VerifyCredentialsResponse>() {
//            @Override
//            public void onNext(VerifyCredentialsResponse value) {
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                response.getAndSet(t);
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//
//            @Override
//            public void onCompleted() {
//
//            }
//        };
//        //When
//        userManagementService.verifyCredentials(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//        final Throwable error = response.get();
//        assertThat(error).isNotNull();
//        assertThat(error.getMessage()).contains("Email address or password are not correct.");
//    }
//
//    @Test
//    public void should_validate_verify_credentials_request() throws Exception {
//        //Given
//        VerifyCredentialsRequest request = VerifyCredentialsRequest
//                .newBuilder()
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<VerifyCredentialsResponse> streamObserver = new StreamObserver<VerifyCredentialsResponse>() {
//            @Override
//            public void onNext(VerifyCredentialsResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        userManagementService.verifyCredentials(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("email");
//        assertThat(error.get().getMessage()).contains("password");
//    }
//
//
//    @Test
//    public void should_get_user_profiles() throws Exception {
//        //Given
//        Map<String, Object> params = new HashMap<>();
//
//        UUID userId1 = UUID.randomUUID();
//        UUID userId2 = UUID.randomUUID();
//        UUID userId3 = UUID.randomUUID();
//        UUID userId4 = UUID.randomUUID();
//        UUID userId5 = UUID.randomUUID();
//
//        params.put("userId1", userId1);
//        params.put("userId2", userId2);
//        params.put("userId3", userId3);
//        params.put("userId4", userId4);
//        params.put("userId5", userId5);
//
//        //resource.getScriptExecutor().executeScriptTemplate("userManagementService/insert5Users.cql", params);
//        scriptExecutor.executeScriptTemplate("userManagementService/insert5Users.cql", params);
//
//        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
//                .addAllUserIds(params.values().stream().map(x -> (UUID)x).map(TypeConverter::uuidToUuid).collect(toList()))
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<GetUserProfileResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<GetUserProfileResponse> streamObserver = new StreamObserver<GetUserProfileResponse>() {
//
//            @Override
//            public void onNext(GetUserProfileResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//        //When
//        userManagementService.getUserProfile(request, streamObserver);
//        latch.await();
//
//        //Then
//        assertThat(completed.get()).isTrue();
//
//        final GetUserProfileResponse getUserProfileResponse = response.get();
//        assertThat(getUserProfileResponse).isNotNull();
//
//        final List<UserProfile> profilesList = getUserProfileResponse.getProfilesList();
//        assertThat(profilesList).isNotEmpty().hasSize(5);
//        assertThat(profilesList.stream().map(UserProfile::getEmail).collect(toList()))
//                .containsAll(Arrays.asList("email1@google.com","email2@google.com","email3@google.com",
//                        "email4@google.com","email5@google.com"));
//    }
//
//    @Test
//    public void should_validate_get_user_profiles_request() throws Exception {
//        //Given
//        GetUserProfileRequest request = GetUserProfileRequest
//                .newBuilder()
//                .addAllUserIds(IntStream.range(0, 22).<UUID>mapToObj(index -> UUID.randomUUID()).map(TypeConverter::uuidToUuid).collect(toList()))
//                .build();
//
//        final AtomicReference<Throwable> error = new AtomicReference<>(null);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//
//        StreamObserver<GetUserProfileResponse> streamObserver = new StreamObserver<GetUserProfileResponse>() {
//            @Override
//            public void onNext(GetUserProfileResponse value) {
//
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                error.getAndSet(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                completed.getAndSet(true);
//            }
//        };
//
//        //When
//        userManagementService.getUserProfile(request, streamObserver);
//
//        //Then
//        assertThat(error.get()).isNotNull();
//        assertThat(error.get().getMessage()).contains("cannot get more than 20 user profiles at once");
//    }
//
//    @Test
//    public void should_create_user_and_verify_credentials() throws Exception {
//        //Given
//        UUID userId = UUID.randomUUID();
//
//        CreateUserRequest request = CreateUserRequest.newBuilder()
//                .setEmail("jdoe@gmail.com")
//                .setFirstName("John")
//                .setLastName("DOE")
//                .setUserId(TypeConverter.uuidToUuid(userId))
//                .setPassword("aaa")
//                .build();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        final AtomicBoolean completed = new AtomicBoolean(false);
//        final AtomicReference<CreateUserResponse> response = new AtomicReference<>(null);
//
//        StreamObserver<CreateUserResponse> streamObserver = new StreamObserver<CreateUserResponse>() {
//            @Override
//            public void onNext(CreateUserResponse value) {
//                response.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//                completed.getAndSet(true);
//            }
//        };
//        //When
//        userManagementService.createUser(request, streamObserver);
//        latch.await();
//
//        VerifyCredentialsRequest verifyCredentialsRequest = VerifyCredentialsRequest
//                .newBuilder()
//                .setEmail("jdoe@gmail.com")
//                .setPassword("aaa")
//                .build();
//
//
//        final CountDownLatch newLatch = new CountDownLatch(1);
//        final AtomicBoolean newCompleted = new AtomicBoolean(false);
//        final AtomicReference<VerifyCredentialsResponse> newResponse = new AtomicReference<>(null);
//
//        StreamObserver<VerifyCredentialsResponse> newStreamObserver = new StreamObserver<VerifyCredentialsResponse>() {
//            @Override
//            public void onNext(VerifyCredentialsResponse value) {
//                newResponse.getAndSet(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                newLatch.countDown();
//            }
//
//            @Override
//            public void onCompleted() {
//                newLatch.countDown();
//                newCompleted.getAndSet(true);
//            }
//        };
//
//        userManagementService.verifyCredentials(verifyCredentialsRequest, newStreamObserver);
//        newLatch.await();
//
//        final VerifyCredentialsResponse verifyCredentialsResponse = newResponse.get();
//
//        assertThat(verifyCredentialsResponse).isNotNull();
//
//    }

}