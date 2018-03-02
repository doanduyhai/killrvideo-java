package killrvideo.configuration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseCluster.Builder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphProtocol;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.dse.graph.api.DseGraph;
import com.xqbase.etcd4j.EtcdClient;
import com.xqbase.etcd4j.EtcdClientException;

import killrvideo.graph.KillrVideoTraversalSource;

/**
 * Connectivity to DSE (cassandra, graph, search, analytics).
 *
 * @author DataStax evangelist team.
 */
@Configuration
public class DseConfiguration {

	/** Internal logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DseConfiguration.class);
    
    @Value("${killrvideo.cassandra.clustername: 'killrvideo'}")
    public String dseClusterName;
    
    @Value("${killrvideo.graph.timeout: 30000}")
    public Integer graphTimeout;
    
    @Value("${killrvideo.graph.recommendation.name: 'killrvideo_video_recommendations'}")
    public String graphRecommendationName;
    
    @Value("#{environment.KILLRVIDEO_DSE_USERNAME}")
    public Optional < String > dseUsername;
   
    @Value("#{environment.KILLRVIDEO_DSE_PASSWORD}")
    public Optional < String > dsePassword;
    
    @Value("#{environment.KILLRVIDEO_DOCKER_IP}")
    public Optional < String > dockerIp;
    
    @Value("#{environment.KILLRVIDEO_HOST_IP}")
    public Optional < String > serverIp;
    
    @Inject
    private EtcdClient etcdClient;
    
    @Bean
    public DseSession initializeDSE() {
         LOGGER.info("Initializing connection to DSE Cluster...");
         long top = System.currentTimeMillis();
         Builder clusterConfig = new Builder();
         populateContactPoints(clusterConfig);
         populateAuthentication(clusterConfig);
         populateGraphOptions(clusterConfig);
         
         DseCluster dseCluster = clusterConfig.build();
         DseSession dseSession = dseCluster.connect();
         long timeElapsed = System.currentTimeMillis() - top;
         LOGGER.info("Connection etablished to DSE Cluster in {} millis.", timeElapsed);
         return dseSession;
    }
    
    @Bean
    public MappingManager initializeMappingManager(DseSession session) {
        return new MappingManager(session);
    }

    @Bean
    public KillrVideoTraversalSource initialGraphTraversalSource(DseSession session) {
        return DseGraph.traversal(session, KillrVideoTraversalSource.class);
    }
    
    /**
     * Retrieve server name from ETCD and update the contact points.
     *
     * @param clusterConfig
     *      current configuration
     */
    private void populateContactPoints(Builder clusterConfig)  {
        try {
            etcdClient.listDir("/killrvideo/services/cassandra").stream()
                      .forEach(node -> asSocketInetAdress(node.value)
                              .ifPresent(clusterConfig::addContactPointsWithPorts));
            clusterConfig.withClusterName(dseClusterName);
        } catch (EtcdClientException e) {
            /**
             * If ETCD is not setup yet we must retry.
             */
            throw new IllegalArgumentException("Cannot retrieve cassandra cluster information from ETCD", e);
        }
    }
    
    /**
     * Check to see if we have username and password from the environment
     * This is here because we have a dual use scenario.  One for developers and others
     * who download KillrVideo and run within a local Docker container and the other
     * who might need (like us for example) to connect KillrVideo up to an external
     * cluster that requires authentication.
     */
    private void populateAuthentication(Builder clusterConfig) {
        if (dseUsername.isPresent() && dsePassword.isPresent() 
                                    && dseUsername.get().length() > 0) {
            AuthProvider cassandraAuthProvider = new DsePlainTextAuthProvider(dseUsername.get(), dsePassword.get());
            clusterConfig.withAuthProvider(cassandraAuthProvider);
            String obfuscatedPassword = new String(new char[dsePassword.get().length()]).replace("\0", "*");
            LOGGER.info("Using supplied DSE username: '%s' and password: '%s' from environment variables", 
                        dseUsername.get(), obfuscatedPassword);
        } else {
            LOGGER.info("DSE cluster authentication method was NOT executed (no username/password provided)");
        }
    }
    
    private void populateGraphOptions(Builder clusterConfig) {
        GraphOptions go = new GraphOptions();
        go.setGraphName(graphRecommendationName);
        go.setReadTimeoutMillis(graphTimeout);
        go.setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0);
        clusterConfig.withGraphOptions(go);
    }
    
    /**
     * Convert information in ETCD as real adress {@link InetSocketAddress} if possible.
     *
     * @param contactPoint
     *      network node adress information like hostname:port
     * @return
     *      java formatted inet adress
     */
    private Optional<InetSocketAddress> asSocketInetAdress(String contactPoint) {
        Optional<InetSocketAddress> target = Optional.empty();
        try {
            if (contactPoint != null && contactPoint.length() > 0) {
                String[] chunks = contactPoint.split(":");
                if (chunks.length == 2) {
                    LOGGER.info("Adding node '{}' to the Cassandra cluster definition", contactPoint);
                    return Optional.of(new InetSocketAddress(InetAddress.getByName(chunks[0]), Integer.parseInt(chunks[1])));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Cannot read contactPoint - "
                    + "Invalid Port Numer, entry '" + contactPoint + "' will be ignored", e);
        } catch (UnknownHostException e) {
            LOGGER.warn("Cannot read contactPoint - "
                    + "Invalid Hostname, entry '" + contactPoint + "' will be ignored", e);
        }
        return target;
    }
   
}
