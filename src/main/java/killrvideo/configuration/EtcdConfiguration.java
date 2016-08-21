package killrvideo.configuration;

import java.net.URI;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xqbase.etcd4j.EtcdClient;

@Configuration
public class EtcdConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdConfiguration.class);

    @Inject
    private KillrVideoProperties properties;

    @Bean
    public EtcdClient connectToEtcd() {

        final String etcdUrl = "http://" + properties.dockerIp + ":" + properties.etcdPort;

        LOGGER.info(String.format("Creating connection to etcd %s", etcdUrl));

        return new EtcdClient(URI.create(etcdUrl));
    }
}
