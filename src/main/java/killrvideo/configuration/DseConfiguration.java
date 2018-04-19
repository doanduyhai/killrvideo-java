package killrvideo.configuration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.dse.DseCluster.Builder;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphProtocol;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.dse.graph.api.DseGraph;
import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;

import killrvideo.dao.EtcdDao;
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
   
    @Value("${killrvideo.cassandra.maxNumberOfTries: 10}")
    private int maxNumberOfTries;
    
    @Value("${killrvideo.cassandra.delayBetweenTries: 2}")
    private int delayBetweenTries;
  
    @Autowired
    private EtcdDao etcdDao;
    
    @Bean
    public DseSession initializeDSE() {
         long top = System.currentTimeMillis();
         LOGGER.info("Initializing connection to DSE");
         Builder clusterConfig = new Builder();
         clusterConfig.withClusterName(dseClusterName);
         populateContactPoints(clusterConfig);
         populateAuthentication(clusterConfig);
         populateGraphOptions(clusterConfig);
         
         final AtomicInteger atomicCount = new AtomicInteger(1);
         Callable<DseSession> connectionToDse = () -> {
             return clusterConfig.build().connect();
         };
         
         RetryConfig config = new RetryConfigBuilder()
                 .retryOnAnyException()
                 .withMaxNumberOfTries(maxNumberOfTries)
                 .withDelayBetweenTries(delayBetweenTries, ChronoUnit.SECONDS)
                 .withFixedBackoff()
                 .build();
         return new CallExecutor<DseSession>(config)
                 .afterFailedTry(s -> { 
                     LOGGER.info("Attempt #{}/{} failed.. trying in {} seconds.", atomicCount.getAndIncrement(),
                             maxNumberOfTries,  delayBetweenTries); })
                 .onFailure(s -> {
                     LOGGER.error("Cannot connection to DSE after {} attempts, exiting", maxNumberOfTries);
                     System.err.println("Can not connect to DSE after " + maxNumberOfTries + " attempts, exiting now.");
                     System.exit(500);
                  })
                 .onSuccess(s -> {   
                     long timeElapsed = System.currentTimeMillis() - top;
                     LOGGER.info("Connection etablished to DSE Cluster in {} millis.", timeElapsed);})
                 .execute(connectionToDse).getResult();
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
        LOGGER.info(" + Reading node addresses from ETCD.");
        List<InetSocketAddress> clusterNodeAdresses = 
                    etcdDao.read("/killrvideo/services/cassandra", true).stream()
                    .map(this::asSocketInetAdress)
                    .filter(node -> node.isPresent())
                    .map(node -> node.get())
                    .collect(Collectors.toList());            
        clusterConfig.withPort(clusterNodeAdresses.get(0).getPort());
        clusterNodeAdresses.stream()
                           .map(adress -> adress.getHostName())
                           .forEach(clusterConfig::addContactPoint);
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
            LOGGER.info(" + Using supplied DSE username: '%s' and password: '%s' from environment variables", 
                        dseUsername.get(), obfuscatedPassword);
        } else {
            LOGGER.info(" + Connection is not authenticated (no username/password)");
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
                    LOGGER.info(" + Adding node '{}' to the Cassandra cluster definition", contactPoint);
                    return Optional.of(new InetSocketAddress(InetAddress.getByName(chunks[0]), Integer.parseInt(chunks[1])));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.warn(" + Cannot read contactPoint - "
                    + "Invalid Port Numer, entry '" + contactPoint + "' will be ignored", e);
        } catch (UnknownHostException e) {
            LOGGER.warn(" + Cannot read contactPoint - "
                    + "Invalid Hostname, entry '" + contactPoint + "' will be ignored", e);
        }
        return target;
    }
   
}
