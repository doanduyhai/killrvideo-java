package killrvideo.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import killrvideo.events.VideoAddedHandlers;
import killrvideo.service.*;

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
    SuggestedVideosService suggestedVideosService;

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

    private Server server;

    @PostConstruct
    public void start() throws Exception {
        final int port = Integer.parseInt(env.getProperty("killrvideo.server.port"));
        server = ServerBuilder
                .forPort(port)
                .addService(commentService)
                .addService(ratingService)
                .addService(statisticsService)
                .addService(suggestedVideosService)
                .addService(uploadsService)
                .addService(userManagementService)
                .addService(videoCatalogService)
                .build();

        LOGGER.info("Starting Grpc Server on port " + port);

        eventBus.register(videoAddedHandlers);

        server.start();
        server.awaitTermination();
    }

    @PreDestroy
    public void stop() {
        eventBus.unregister(videoAddedHandlers);
        server.shutdown();
    }
}
