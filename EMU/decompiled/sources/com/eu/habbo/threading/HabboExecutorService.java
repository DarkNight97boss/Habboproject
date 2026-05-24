package com.eu.habbo.threading;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/HabboExecutorService.class */
public class HabboExecutorService extends ScheduledThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HabboExecutorService.class);

    public HabboExecutorService(int i, ThreadFactory threadFactory) {
        super(i, threadFactory);
    }

    @Override // java.util.concurrent.ThreadPoolExecutor
    protected void afterExecute(Runnable runnable, Throwable th) {
        super.afterExecute(runnable, th);
        if (th == null || (th instanceof IOException)) {
            return;
        }
        LOGGER.error("Error in HabboExecutorService", th);
    }
}
