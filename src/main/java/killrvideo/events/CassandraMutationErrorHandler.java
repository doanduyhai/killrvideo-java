package killrvideo.events;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import killrvideo.configuration.KillrVideoProperties;

@Component
public class CassandraMutationErrorHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(CassandraMutationErrorHandler.class);

    @Inject
    KillrVideoProperties properties;

    private PrintWriter errorLogFile;

    @PostConstruct
    public void openErrorLogFile() throws FileNotFoundException {
        this.errorLogFile = new PrintWriter(properties.mutationErrorLog);
    }

    /**
     * Here we just record the original Grpc request so that we can replay
     * them later.
     *
     * An alternative impl can just push the request to a message queue or
     * event bus so that it can be handled by another micro-service
     *
     */
    @Subscribe
    public void handle(CassandraMutationError mutationError) {

        final String errorLog = mutationError.buildErrorLog();
        LOGGER.debug(String.format("Recording mutation error %s", errorLog));

        errorLogFile.append(errorLog).append("\n***********************\n");
        errorLogFile.flush();
    }

    @PreDestroy
    public void closeErrorLogFile() {
        this.errorLogFile.close();
    }

}
