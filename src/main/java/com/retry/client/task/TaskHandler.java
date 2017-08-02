package com.retry.client.task;

import com.kepler.zookeeper.ZkClient;
import com.retry.client.zookeeper.RetryWatcher;
import com.retry.client.zookeeper.RootWatcher;
import com.retry.client.zookeeper.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;


/**
 * Created by zbyte on 17-7-28.
 */
public class TaskHandler {

    private final static String PATH = "/retry";

    private final static Log LOGGER = LogFactory.getLog(TaskHandler.class);

    private final ZkClient zkClient;
    private final RetryExecutor executor;
    private final Session session;
    private final RetryTask retryTask;

    private RetryWatcher retryWatcher;

    public TaskHandler(ZkClient zkClient, RetryExecutor executor, Session session, RetryTask retryTask) {
        this.zkClient = zkClient;
        this.executor = executor;
        this.session = session;
        this.retryTask = retryTask;
        init();
    }

    public void setRetryWatcher(RetryWatcher retryWatcher) {
        this.retryWatcher = retryWatcher;
    }

    public void setRootWatcher(RootWatcher rootWatcher) {
        zkClient.zoo().register(rootWatcher);
    }

    /**
     * 初始化，尝试注册节点并启动定时任务
     */
    public void init() {
        if (zkClient.zoo().getSessionId() == session.getSessionId()) {
            // 断线重连
            executor.execute(retryTask);
            return;
        }
        Stat stat = null;
        try {
            stat = exists();
            if (stat == null) {
                zkClient.create(PATH, PATH.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                session.setSessionId(zkClient.zoo().getSessionId());
                executor.execute(retryTask);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * 保持监听
     * @return
     * @throws Exception
     */
    public Stat exists(){
        try {
            return zkClient.exists(PATH, retryWatcher);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }
}
