package com.retry.zookeeper;

import com.retry.scheduletask.TaskHandler;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by zbyte on 17-7-28.
 */
public class RetryWatcher implements Watcher {

    private TaskHandler taskHandler;

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        if (Event.EventType.NodeDeleted.equals(watchedEvent.getType())) {
            System.out.println("节点被删除，尝试注册retry服务");
            taskHandler.init();
        }

        // 保持对retry节点的监听
        try {
            taskHandler.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
