package com.retry.client.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zbyte on 17-7-31.
 */
public class RetryExecutor implements Executor{

    private final static long INITIAL_DELAY = 0;
    private final static long PERIOD = 10;

    private ScheduledExecutorService executor = null;

    @Override
    public void execute(Runnable command) {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(command, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
