package com.retry.client.zookeeper;

import com.retry.client.task.RetryExecutor;
import com.retry.client.task.TaskHandler;
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
            // 与zk断开连接，停止retry job
            executor.shutdown();
        }

        if (Event.KeeperState.SyncConnected.equals(watchedEvent.getState())
                && Event.EventType.None.equals(watchedEvent.getType())) {
            // 与zk连接成功,初始化定时任务处理器
            taskHandler.init();
        }

    }
}
