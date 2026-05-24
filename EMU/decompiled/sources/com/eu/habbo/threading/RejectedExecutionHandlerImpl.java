package com.eu.habbo.threading;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/RejectedExecutionHandlerImpl.class */
public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RejectedExecutionHandlerImpl.class);

    @Override // java.util.concurrent.RejectedExecutionHandler
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        LOGGER.error(runnable.toString() + " is rejected");
    }
}
