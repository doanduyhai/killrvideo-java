package killrvideo.grpc;

import static java.lang.String.format;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.xqbase.etcd4j.EtcdClient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import killrvideo.comments.CommentsServiceGrpc;
import killrvideo.configuration.KillrVideoConfiguration;
import killrvideo.events.CassandraMutationErrorHandler;
import killrvideo.ratings.RatingsServiceGrpc;
import killrvideo.search.SearchServiceGrpc;
import killrvideo.service.CommentService;
import killrvideo.service.RatingsService;
import killrvideo.service.SearchService;
import killrvideo.service.StatisticsService;
import killrvideo.service.SuggestedVideosService;
import killrvideo.service.UploadsService;
import killrvideo.service.UserManagementService;
import killrvideo.service.VideoCatalogService;
import killrvideo.statistics.StatisticsServiceGrpc;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc;
import killrvideo.uploads.UploadsServiceGrpc;
import killrvideo.user_management.UserManagementServiceGrpc;
import killrvideo.video_catalog.VideoCatalogServiceGrpc;

/**
 * Startup a GRPC server on expected port and register all services.
 *
 * @author DataStax evangelist team.
 */
@Component
public class GrpcServer {

    /** Some logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    /**
     * All configuration parameters. 
     */
    @Inject
    private KillrVideoConfiguration config;
    
    /**
     * Connectivity to ETCD Service discovery.
     */
    @Inject
    private EtcdClient etcdClient;
    
    /**
     * Communication channel between service, for now GUAVA inmemory messaging.
     */
    @Inject
    private EventBus eventBus;
    
    @Inject
    CommentService commentService;

    @Inject
    RatingsService ratingService;

    @Inject
    SearchService searchService;

    @Inject
    StatisticsService statisticsService;

    @Inject
    UploadsService uploadsService;

    @Inject
    UserManagementService userManagementService;

    @Inject
    VideoCatalogService videoCatalogService;

    @Inject
    SuggestedVideosService suggestedVideosService;

    @Inject
    CassandraMutationErrorHandler cassandraMutationErrorHandler;   

    /**
     * GRPC Server to set up.
     */
    private Server server;
    
    private String applicationUID;
    
    @PostConstruct
    public void start() throws Exception {
        applicationUID = config.getApplicationName()  + ":" + config.getApplicationInstanceId();
        
        LOGGER.info("Starting Grpc Server on port: '{}'", config.getApplicationPort());
        final ServerServiceDefinition commentService = CommentsServiceGrpc.bindService(this.commentService);
        final ServerServiceDefinition ratingService = RatingsServiceGrpc.bindService(this.ratingService);
        final ServerServiceDefinition statisticsService = StatisticsServiceGrpc.bindService(this.statisticsService);
        final ServerServiceDefinition suggestedVideoService = SuggestedVideoServiceGrpc.bindService(this.suggestedVideosService);
        final ServerServiceDefinition uploadsService = UploadsServiceGrpc.bindService(this.uploadsService);
        final ServerServiceDefinition userManagementService = UserManagementServiceGrpc.bindService(this.userManagementService);
        final ServerServiceDefinition videoCatalogService = VideoCatalogServiceGrpc.bindService(this.videoCatalogService);
        final ServerServiceDefinition searchService = SearchServiceGrpc.bindService(this.searchService);
        server = ServerBuilder
                .forPort(config.getApplicationPort())
                .addService(commentService)
                .addService(ratingService)
                .addService(statisticsService)
                .addService(suggestedVideoService)
                .addService(uploadsService)
                .addService(userManagementService)
                .addService(videoCatalogService)
                .addService(searchService)
                .build();
       
        eventBus.register(suggestedVideosService);
        eventBus.register(cassandraMutationErrorHandler);

        registerServicesToEtcd(
                commentService, ratingService, statisticsService,
                suggestedVideoService, uploadsService, userManagementService, 
                videoCatalogService, searchService);
        

        /**
         * Declare a shutdown hook otherwise the JVM
         * cannot be stop since the Grpc server
         * is listening on  a port forever
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.info("Calling shutdown for GrpcServer");
                server.shutdown();
            }
        });

        server.start();
    }

    @PreDestroy
    public void stop() {
        eventBus.unregister(suggestedVideosService);
        eventBus.unregister(cassandraMutationErrorHandler);
        server.shutdown();
    }

    /**
     * Enter information into ETCD.
     *
     * @param serviceDefinitions
     *          services definitions from GRPC
     * @throws IOException
     *          exception when accessing ETCD
     */
    private void registerServicesToEtcd(ServerServiceDefinition... serviceDefinitions) throws IOException {
        final String applicationAdress = format("%s:%d", config.getApplicationHost(), config.getApplicationPort());
        for (ServerServiceDefinition service : serviceDefinitions) {
            final String shortName  = shortenServiceName(service.getServiceDescriptor().getName());
            final String serviceKey = format("/killrvideo/services/%s/%s", shortName, applicationUID);
            LOGGER.info("Registering service : '{}' with '{}'", serviceKey, applicationAdress);
            etcdClient.set(serviceKey, applicationAdress);
        }
    }
    
    /**
     * Remove special caracters.
     */
    private String shortenServiceName(String fullServiceName) {
        return fullServiceName.replaceAll(".*\\.([^.]+)", "$1");
    }
}
