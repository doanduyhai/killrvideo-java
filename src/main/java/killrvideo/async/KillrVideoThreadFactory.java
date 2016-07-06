package killrvideo.async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillrVideoThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger("killrvideo-default-executor");
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
        LOGGER.error("Uncaught asynchronous exception : " + e.getMessage(), e);
    };

    public KillrVideoThreadFactory() {
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("killrvideo-default-executor-" + this.threadNumber.incrementAndGet());
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        return thread;
    }
    }
