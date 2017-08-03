package com.retry.client.zookeeper;

import com.kepler.config.PropertiesUtils;
import com.kepler.zookeeper.ZkFactory;
import com.retry.client.task.RetryExecutor;
import com.retry.client.task.TaskHandler;
import com.retry.exception.RetryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import static org.apache.zookeeper.Watcher.Event.EventType.None;

/**
 * Created by zbyte on 17-8-2.
 */
public class ZkClient {

    private static final int RETRY_TIMES = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".retry_times", Integer.MAX_VALUE);

    private static final int RETRY_INTERVAL = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".retry_interval", 20000);

    private static final int TIMEOUT_SESSION = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".timeout_session", 30000);

    private static final int TIMEOUT_CONNECT = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".timeout_connect", 120000);

    private static final String SCHEME = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".scheme", "digest");

    private static final String AUTH = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".auth", "");




    public static final String HOST = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".host", "");

    private static final Log LOGGER = LogFactory.getLog(ZkClient.class);

    private final RetryExecutor executor;

    private TaskHandler taskHandler;

    private ZooKeeper zoo;

    private final ZkConnection connection = new ZkConnection();

    private long sessionId;

    public ZkClient(RetryExecutor executor) {
        this.executor = executor;
        try {
            init();
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }

    public void init() throws Exception {
        this.connection.reset();
        this.zoo = new ZooKeeper(HOST, TIMEOUT_SESSION, new ConnectionWatcher());
        this.zoo.addAuthInfo(ZkClient.SCHEME, ZkClient.AUTH.getBytes());
        // 阻塞直到连接成功
        this.connection.await();
    }

    public ZooKeeper zoo() {
        return zoo;
    }

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
        // 将ZkClient手动注入到TaskHandler中,并尝试建立节点
        this.taskHandler.setZkClient(this);
        this.taskHandler.init();
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void reset() {
        for (int times = 0; times < ZkClient.RETRY_TIMES; times++) {
            try {
                this.reset(times);
            } catch (Exception e) {
                ZkClient.LOGGER.error(e.getMessage(), e);
            }
            return;
        }
    }

    private void reset(int times) throws Exception {
        Thread.sleep(ZkClient.RETRY_INTERVAL);
        ZkClient.LOGGER.info("Retry-Zookeeper reset " + times + " times");
        this.zoo.close();
        this.init();
    }

    private class ConnectionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent watchedEvent) {

            switch (watchedEvent.getState()) {
                case SyncConnected:
                    ZkClient.this.connection.activate();
                    if (watchedEvent.getType().equals(None)) {
                        if (ZkClient.this.taskHandler != null) {
                            // 与zk连接成功,初始化定时任务处理器
                            ZkClient.this.taskHandler.init();
                        }
                    }
                    return;
                case Expired:
                    ZkClient.LOGGER.fatal("Retry-Zookeeper Expired: " + watchedEvent + " ...");
                    ZkClient.this.reset();
                    return;
                case Disconnected:
                    // 与zk断开连接，停止retry job，并开始尝试重连
                    ZkClient.LOGGER.fatal("Retry-Zookeeper Disconnected: " + watchedEvent + " ...");
                    ZkClient.this.executor.shutdown();
                    ZkClient.this.reset();
                    return;
            }
        }
    }

    private class ZkConnection {

        private long start;

        volatile private boolean valid;

        public void reset() {
            this.start = System.currentTimeMillis();
            this.valid = false;
        }

        public void await() throws Exception {
            if (!this.valid) {
                this.doWait();
            }
        }

        public void activate() {
            synchronized (this) {
                this.valid = true;
                this.notifyAll();
            }
        }

        public void doWait() throws Exception {
            synchronized (this) {
                while (!this.valid) {
                    this.wait();
                }
            }
            this.timeout();
        }

        private void timeout() {
            if ((System.currentTimeMillis() - this.start) > ZkClient.TIMEOUT_CONNECT) {
                throw new RetryException("Retry-Zookeeper connect timeout ...");
            }
        }
    }
}
