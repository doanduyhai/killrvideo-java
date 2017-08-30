package killrvideo.configuration;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.datastax.driver.dse.graph.GraphProtocol;
import com.datastax.dse.graph.api.DseGraph;
import killrvideo.graph.KillrVideoTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseCluster.Builder;
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.xqbase.etcd4j.EtcdClient;

import com.datastax.driver.mapping.MappingManager;
import killrvideo.utils.ExceptionUtils;


@Configuration
public class DSEConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSEConfiguration.class);

    private static final String CLUSTER_NAME = "killrvideo";
    private static final String RECOMMENDATION_GRAPH_NAME = "killrvideo_video_recommendations";
    private static final Integer GRAPH_TIMEOUT_DEFAULT = 30000;

    @Inject
    Environment env;

    @Inject
    EtcdClient etcdClient;

    @Inject
    private KillrVideoProperties properties;

    @Bean
    public DseSession initializeDSE() {
        LOGGER.info("Initializing connection to Cassandra");
        LOGGER.info("ETCD Client is: " + etcdClient.toString());

        try {
            List<String> cassandraHostsAndPorts = etcdClient.listDir("/killrvideo/services/cassandra")
                    .stream()
                    .map(node -> node.value)
                    .collect(toList());

            final String cassandraHosts = cassandraHostsAndPorts
                    .stream()
                    .map(x -> x.split(":")[0])
                    .collect(Collectors.joining(","));

            final int cassandraPort = Integer.parseInt(
                    cassandraHostsAndPorts
                            .get(0)
                            .split(":")[1]);

            LOGGER.info(String.format("Retrieving cassandra hosts %s and port %s from etcd", cassandraHosts, cassandraPort));

            Builder clusterConfig = new Builder();
            clusterConfig
                    .addContactPoints(cassandraHosts)
                    .withPort(cassandraPort)
                    .withClusterName(CLUSTER_NAME);

            /**
             * Check to see if we have username and password from the environment
             * This is here because we have a dual use scenario.  One for developers and others
             * who download KillrVideo and run within a local Docker container and the other
             * who might need (like us for example) to connect KillrVideo up to an external
             * cluster that requires authentication.
             */
            String dseUsername = properties.dseUsername;
            String dsePassword = properties.dsePassword;
            if (dseUsername != null && dsePassword != null) {
                Integer passwordLength = dsePassword.length();
                LOGGER.info("Using supplied DSE username: \"" + dseUsername + "\" and password: \"***" +
                        dsePassword.substring(passwordLength - 4, passwordLength) + "\" from environment variables");

                clusterConfig
                        .withAuthProvider(new DsePlainTextAuthProvider(dseUsername, dsePassword));

            } else {
                LOGGER.info("No detected username/password combination was passed in. DSE cluster authentication method was NOT executed.");

            }

            /**
             * Add option for the graph based recommendation engine
             */
            clusterConfig.withGraphOptions(new GraphOptions()
                    .setGraphName(RECOMMENDATION_GRAPH_NAME)
                    .setReadTimeoutMillis(GRAPH_TIMEOUT_DEFAULT)
                    .setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0)
            );

            DseCluster dseCluster = clusterConfig.build();

            return dseCluster.connect();

        } catch (Throwable e) {
            LOGGER.error("Exception : " + e.getMessage());
            LOGGER.error(ExceptionUtils.mergeStackTrace(e));

            throw new IllegalStateException("Cannot find 'killrvideo/services/cassandra' from etcd");
        }

    }

    @Bean
    public Void cassandraNativeClusterProduction() {
        // Initialize DSE
        final DseSession dseSession = initializeDSE();

        final MappingManager manager = getMappingManager(dseSession);
        LOGGER.info(String.format("Creating mapping manager %s", manager));

        final KillrVideoTraversalSource killr = getKillrVideoTraversalSource(dseSession);
        LOGGER.info(String.format("Creating graph traversal killrvideosource %s", killr));

        return null;
    }

    @Bean
    public MappingManager getMappingManager(DseSession session) {
        return new MappingManager(session);
    }

    @Bean
    public KillrVideoTraversalSource getKillrVideoTraversalSource(DseSession session) {
        return DseGraph.traversal(session, KillrVideoTraversalSource.class);
    }
}
