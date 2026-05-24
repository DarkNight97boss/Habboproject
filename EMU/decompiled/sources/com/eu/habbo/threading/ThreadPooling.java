package com.eu.habbo.threading;

import com.eu.habbo.Emulator;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/ThreadPooling.class */
public class ThreadPooling {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPooling.class);
    public final int threads;
    private final ScheduledExecutorService scheduledPool;
    private volatile boolean canAdd = true;

    public ThreadPooling(Integer num) {
        this.threads = num.intValue();
        this.scheduledPool = new HabboExecutorService(this.threads, new DefaultThreadFactory("HabExec"));
        LOGGER.info("Thread Pool -> Loaded!");
    }

    public ScheduledFuture run(Runnable runnable) {
        try {
            if (this.canAdd) {
                return run(runnable, 0L);
            }
            if (Emulator.isShuttingDown) {
                runnable.run();
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    public ScheduledFuture run(Runnable runnable, long j) {
        try {
            if (this.canAdd) {
                return this.scheduledPool.schedule(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        LOGGER.error("Caught exception", e);
                    }
                }, j, TimeUnit.MILLISECONDS);
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    public void shutDown() {
        this.canAdd = false;
        this.scheduledPool.shutdownNow();
        LOGGER.info("Threading -> Disposed!");
    }

    public void setCanAdd(boolean z) {
        this.canAdd = z;
    }

    public ScheduledExecutorService getService() {
        return this.scheduledPool;
    }
}
