package com.retry.scheduletask;

import com.kepler.zookeeper.ZkClient;
import com.retry.zookeeper.RetryWatcher;
import com.retry.zookeeper.RootWatcher;
import com.retry.zookeeper.Session;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;


/**
 * Created by zbyte on 17-7-28.
 */
public class TaskHandler {

    private final static String PATH = "/retry";

    private final ZkClient zkClient;
    private final RetryExecutor executor;
    private final Session session;

    private RetryWatcher retryWatcher;

    public TaskHandler(ZkClient zkClient, RetryExecutor executor, Session session) {
        this.zkClient = zkClient;
        this.executor = executor;
        this.session = session;
        init();
    }

    public void setRetryWatcher(RetryWatcher retryWatcher) {
        this.retryWatcher = retryWatcher;
    }

    public void setRootWatcher(RootWatcher rootWatcher) {
        zkClient.zoo().register(rootWatcher);
    }

    public void init() {

        if (zkClient.zoo().getSessionId() == session.getSessionId()) {
            // 断线重连
            executor.execute(new RetryTask());
            return;
        }

        System.out.println("zkClient: "+zkClient);
        Stat stat = null;
        try {
            stat = exists();
            System.out.println("stat: "+stat);
            if (stat == null) {
                zkClient.create(PATH, PATH.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                session.setSessionId(zkClient.zoo().getSessionId());
                executor.execute(new RetryTask());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stat exists() throws Exception {
        return zkClient.exists(PATH, retryWatcher);
    }
}
