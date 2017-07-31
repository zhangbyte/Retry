package com.retry.zookeeper;

import com.retry.scheduletask.RetryExecutor;
import com.retry.scheduletask.TaskHandler;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by zbyte on 17-7-31.
 */
public class RootWatcher implements Watcher {

    private final RetryExecutor executor;

    private TaskHandler taskHandler;

    public RootWatcher(RetryExecutor executor) {
        this.executor = executor;
    }

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.Disconnected.equals(watchedEvent.getState())) {
            System.out.println("与zk断开连接，停止retry job");
            executor.shutdown();
        }

        if (Event.KeeperState.SyncConnected.equals(watchedEvent.getState())
                && Event.EventType.None.equals(watchedEvent.getType())) {
            System.out.println("与zk连接成功");
            taskHandler.init();
        }

    }
}
