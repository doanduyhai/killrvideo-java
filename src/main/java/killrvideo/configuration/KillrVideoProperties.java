package killrvideo.configuration;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.Optional;

public class KillrVideoProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(KillrVideoProperties.class);

    public static final String THREADPOOL_MIN_THREADS = "killrvideo.threadpool.min.threads";
    public static final String THREADPOOL_MAX_THREADS = "killrvideo.threadpool.max.threads";
    public static final String THREADPOOL_TTL_SECONDS = "killrvideo.thread.ttl.seconds";
    public static final String THREADPOOL_QUEUE_SIZE = "killrvideo.thread.queue.size";

    public static final String APPLICATION_NAME = "killrvideo.application.name";
    public static final String APPLICATION_INSTANCE_ID = "killrvideo.application.instance.id";
    public static final String APPLICATION_PORT = "killrvideo.server.port";
    public static final String ETCD_PORT = "killrvideo.etcd.port";
    public static final String CASSANDRA_MUTATION_ERROR_LOG = "killrvideo.cassandra.mutation.error.log";
    public static final String KILLRVIDEO_DOCKER_IP = "KILLRVIDEO_DOCKER_IP";
    public static final String KILLRVIDEO_HOST_IP = "KILLRVIDEO_HOST_IP";
    public static final String KILLRVIDEO_DSE_USERNAME = "KILLRVIDEO_DSE_USERNAME";
    public static final String KILLRVIDEO_DSE_PASSWORD = "KILLRVIDEO_DSE_PASSWORD";

    public final int minThreads;
    public final int maxThreads;
    public final int threadsTTLSeconds;
    public final int threadPoolQueuSize;
    public final String applicationName;
    public final String applicationInstanceId;
    public final int applicationPort;
    public final int etcdPort;
    public final String mutationErrorLog;
    public final String dockerIp;
    public final String serverIp;
    public final String dseUsername;
    public final String dsePassword;

    public KillrVideoProperties(Environment env) {
        this.minThreads = parseInt(env.getProperty(THREADPOOL_MIN_THREADS, "5"));
        this.maxThreads = parseInt(env.getProperty(THREADPOOL_MAX_THREADS, "10"));
        this.threadsTTLSeconds = parseInt(env.getProperty(THREADPOOL_TTL_SECONDS, "60"));
        this.threadPoolQueuSize = parseInt(env.getProperty(THREADPOOL_QUEUE_SIZE, "1000"));
        this.applicationName = env.getProperty(APPLICATION_NAME, "KillrVideo");
        this.applicationInstanceId = env.getProperty(APPLICATION_INSTANCE_ID, "0");
        this.applicationPort = parseInt(env.getProperty(APPLICATION_PORT, "8899"));
        this.etcdPort = parseInt(env.getProperty(ETCD_PORT, "2379"));
        this.mutationErrorLog = env.getProperty(CASSANDRA_MUTATION_ERROR_LOG, "/tmp/killrvideo-mutation-errors.log");

        /**
         * Need to set env variable KILLRVIDEO_DOCKER_IP and KILLRVIDEO_SERVER_IP before launching application
         */
        final Optional<String> dockerIp = Optional.ofNullable(System.getenv(KILLRVIDEO_DOCKER_IP));
        final Optional<String> serverIp = Optional.ofNullable(System.getenv(KILLRVIDEO_HOST_IP));

        if (!dockerIp.isPresent()) {
            final String errorMessage = format("Cannot find environment variable %s. " +
                    "Please set it before launching KillrVideoServer", KILLRVIDEO_DOCKER_IP);
            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage);

        } else {
            LOGGER.info("Setting docker ip to : " + dockerIp.get());
            this.dockerIp = dockerIp.get();
        }

        if (!serverIp.isPresent()) {
            final String errorMessage = format("Cannot find environment variable %s. " +
                    "Please set it before launching KillrVideoServer", KILLRVIDEO_HOST_IP);

            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage);

        } else {
            LOGGER.info("Setting server ip to : " + serverIp.get());
            this.serverIp = serverIp.get();
        }

        /**
         * Grab the DSE username and password from the environment as well if they exist
         */
        final Optional<String> dseUsername = Optional.ofNullable(System.getenv(KILLRVIDEO_DSE_USERNAME));
        final Optional<String> dsePassword = Optional.ofNullable(System.getenv(KILLRVIDEO_DSE_PASSWORD));
        this.dseUsername = dseUsername.orElse(null);
        this.dsePassword = dsePassword.orElse(null);
    }
}
