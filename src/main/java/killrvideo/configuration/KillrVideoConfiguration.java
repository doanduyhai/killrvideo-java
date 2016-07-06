package killrvideo.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import killrvideo.async.KillrVideoThreadFactory;

@Configuration
public class KillrVideoConfiguration {

    @Inject
    private Environment env;

    @Bean
    public EventBus createEventBus() {
        return new EventBus("killrvideo_event_bus");
    }

    @Bean(destroyMethod = "shutDownNow")
    public ExecutorService threadPool() {
        final int minThreads = Integer.parseInt(env.getProperty("threadpool.min.threads", "10"));
        final int maxThreads = Integer.parseInt(env.getProperty("threadpool.max.threads", "10"));
        final long threadKeepAlive = Long.parseLong(env.getProperty("threadpool.thread.ttl", "60"));
        int queueSize = Integer.parseInt(env.getProperty("threadpool.queue.size", "1000"));
        return new ThreadPoolExecutor(minThreads, maxThreads, threadKeepAlive, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueSize), new KillrVideoThreadFactory());
    }
}
