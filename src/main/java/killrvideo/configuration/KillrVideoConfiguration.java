package killrvideo.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import killrvideo.async.KillrVideoThreadFactory;

@Configuration
public class KillrVideoConfiguration {

    private static Logger LOGGER = LoggerFactory.getLogger(KillrVideoConfiguration.class);

    @Inject
    private Environment env;

    @Bean
    public KillrVideoProperties getApplicationProperties() {
        return new KillrVideoProperties(env);
    }

    @Bean
    public EventBus createEventBus() {
        return new EventBus("killrvideo_event_bus");
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService threadPool() {
        final KillrVideoProperties properties = this.getApplicationProperties();
        return new ThreadPoolExecutor(properties.minThreads, properties.maxThreads, properties.threadsTTLSeconds, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(properties.threadPoolQueuSize), new KillrVideoThreadFactory());
    }

    @Bean
    public Validator getBeanValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

}
