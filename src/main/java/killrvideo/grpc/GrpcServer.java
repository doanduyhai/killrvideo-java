package killrvideo.grpc;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.xqbase.etcd4j.EtcdClient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import killrvideo.comments.CommentsServiceGrpc;
import killrvideo.configuration.KillrVideoProperties;
import killrvideo.events.CassandraMutationErrorHandler;
import killrvideo.events.VideoAddedHandlers;
import killrvideo.ratings.RatingsServiceGrpc;
import killrvideo.search.SearchServiceGrpc;
import killrvideo.service.*;
import killrvideo.statistics.StatisticsServiceGrpc;
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc;
import killrvideo.uploads.UploadsServiceGrpc;
import killrvideo.user_management.UserManagementServiceGrpc;
import killrvideo.video_catalog.VideoCatalogServiceGrpc;

@Component
public class GrpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    @Inject
    Environment env;

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
    EventBus eventBus;

    @Inject
    VideoAddedHandlers videoAddedHandlers;

    @Inject
    SuggestedVideosService suggestedVideosService;

    @Inject
    CassandraMutationErrorHandler cassandraMutationErrorHandler;

    @Inject
    EtcdClient etcdClient;

    @Inject
    KillrVideoProperties properties;

    private Server server;

    @PostConstruct
    public void start() throws Exception {

        LOGGER.info("Try starting Grpc Server ");

        final int port = Integer.parseInt(env.getProperty("killrvideo.server.port"));
        final ServerServiceDefinition commentService = CommentsServiceGrpc.bindService(this.commentService);
        final ServerServiceDefinition ratingService = RatingsServiceGrpc.bindService(this.ratingService);
        final ServerServiceDefinition statisticsService = StatisticsServiceGrpc.bindService(this.statisticsService);
        final ServerServiceDefinition suggestedVideoService = SuggestedVideoServiceGrpc.bindService(this.suggestedVideosService);
        final ServerServiceDefinition uploadsService = UploadsServiceGrpc.bindService(this.uploadsService);
        final ServerServiceDefinition userManagementService = UserManagementServiceGrpc.bindService(this.userManagementService);
        final ServerServiceDefinition videoCatalogService = VideoCatalogServiceGrpc.bindService(this.videoCatalogService);
        final ServerServiceDefinition searchService = SearchServiceGrpc.bindService(this.searchService);

        server = ServerBuilder
                .forPort(port)
                .addService(commentService)
                .addService(ratingService)
                .addService(statisticsService)
                .addService(suggestedVideoService)
                .addService(uploadsService)
                .addService(userManagementService)
                .addService(videoCatalogService)
                .addService(searchService)
                .build();

        LOGGER.info("Starting Grpc Server on port " + port);

        eventBus.register(videoAddedHandlers);
        eventBus.register(suggestedVideosService);
        eventBus.register(cassandraMutationErrorHandler);

        registerServicesToEtcd(Lists.newArrayList(commentService, ratingService, statisticsService,
                suggestedVideoService, uploadsService, userManagementService, videoCatalogService,
                searchService));

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
        eventBus.unregister(videoAddedHandlers);
        eventBus.unregister(suggestedVideosService);
        eventBus.unregister(cassandraMutationErrorHandler);
        server.shutdown();
    }

    private void registerServicesToEtcd(List<ServerServiceDefinition> serviceDefinitions) throws IOException {
        final String uniqueId = env.getProperty("killrvideo.application.name") + ":" + env.getProperty("killrvideo.application.instance.id");

        final String port = env.getProperty("killrvideo.server.port");

        LOGGER.info("Registering Grpc services to etcd");

        for (ServerServiceDefinition service : serviceDefinitions) {

            final String serviceUrl = format("/killrvideo/services/%s/%s", shortenServiceName(service.getServiceDescriptor().getName()), uniqueId);

            LOGGER.info(format("Registering service : %s", serviceUrl));

            etcdClient.set(serviceUrl, format("%s:%s", properties.serverIp, port));
        }
    }

    private String shortenServiceName(String fullServiceName) {
        return fullServiceName.replaceAll(".*\\.([^.]+)", "$1");
    }
}
