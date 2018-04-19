package killrvideo.dao;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.xqbase.etcd4j.EtcdClient;
import com.xqbase.etcd4j.EtcdClientException;
import com.xqbase.etcd4j.EtcdNode;

/**
 * Hanle operation arount ETCD (connection, read, write).
 * 
 * @author DataStax Evangelist Team
 */
@Component
public class EtcdDao {

    /** Initialize dedicated connection to ETCD system. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdDao.class);
    
    /**
     *  Expected 'KILLRVIDEO_DOCKER_IP' env variable
     *  Then, if not present put 10.0.75.1 as a default value (dockerNAT assigns 10.0.75.1 on MAC)
     */
    @Value("#{environment.KILLRVIDEO_DOCKER_IP ?: '10.0.75.1'}")
    private String etcdServerHost;
    
    /** 
     * Retrieve expected from application.properties/application .yaml files
     * Then, if not find use default value 2379
     */
    @Value("${killrvideo.etcd.port: 2379}")
    private int etcdServerPort;
    
    @Value("${killrvideo.etcd.maxNumberOfTries: 10}")
    private int maxNumberOfTriesEtcd;
    
    @Value("${killrvideo.etcd.delayBetweenTries: 2}")
    private int delayBetweenTriesEtcd;
    
    /** Native client. */
    private EtcdClient etcdClient;
   
    @PostConstruct
    public void connect() {
        final String etcdUrl = String.format("http://%s:%d", etcdServerHost, etcdServerPort);
        LOGGER.info("Initializing connection to ETCD Server");
        LOGGER.info(" + Address '{}'", etcdUrl);
        etcdClient = new EtcdClient(URI.create(etcdUrl));
        LOGGER.info(" + Client created");
        // Enforcing read event if value is empty is only ways to ensure ETCD is stared and available.
        read("/", false);
        LOGGER.info("Connection etablished to ETCD Server");
    }
    
    /**
     * Read from ETCD using a retry mecanism.
     *
     * @param key
     *      current key to look in ETCD.
     * @param required
     *      key is required if not returning empty list
     * @return
     */
    public List<String> read(String key, boolean required) {
        
        final AtomicInteger atomicCount = new AtomicInteger(1);
        
        Callable<List <EtcdNode>> getKeyFromEtcd = () -> {
            try {
                List <EtcdNode> nodes = etcdClient.listDir(key);
                if (nodes.isEmpty() && required) {
                    throw new IllegalStateException("Key '" + key + "' is required in ETCD but not yet present");
                }
                return nodes;
            } catch (EtcdClientException e) {
                throw new IllegalStateException("Cannot Access ETCD Server : " + e.getMessage());
            }
        };
        
        RetryConfig etcdRetryConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(maxNumberOfTriesEtcd)
                .withDelayBetweenTries(delayBetweenTriesEtcd, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();
        return new CallExecutor<List <EtcdNode>>(etcdRetryConfig)
                .afterFailedTry(s -> { 
                    LOGGER.info("Attempt #{}/{} : ETCD is not ready or require key '{}' is not present yet (retry in {}s)", 
                             atomicCount.getAndIncrement(), maxNumberOfTriesEtcd,  key, delayBetweenTriesEtcd); })
                .onFailure(s -> {
                    LOGGER.error("ETCD is not ready after {} attempts, exiting", maxNumberOfTriesEtcd);
                    System.err.println("ETCD is not ready after " + maxNumberOfTriesEtcd + " attempts, exiting now.");
                    System.exit(500);
                 })
                .execute(getKeyFromEtcd).getResult()
                .stream().map(node -> node.value)
                .collect(Collectors.toList());
    }
    
    public void register(String key, String value) {
        try {
            etcdClient.set(key, value);
        } catch (EtcdClientException e) {
            throw new IllegalStateException("Cannot register services into ETCD", e);
        }
    }
   
}
