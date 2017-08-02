package com.retry.client.zookeeper;

import com.kepler.config.PropertiesUtils;
import com.kepler.zookeeper.ZkFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by zbyte on 17-8-2.
 */
public class ZkClient {

    private static final Log LOGGER = LogFactory.getLog(ZkClient.class);

    public static final String HOST = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".host", "");

    private static final int TIMEOUT_SESSION = PropertiesUtils.get(ZkFactory.class.getName().toLowerCase() + ".timeout_session", 30000);

    private ZooKeeper zoo;

    private long sessionId;

    public ZkClient(Watcher watcher) {
        try {
            this.zoo = new ZooKeeper(HOST, TIMEOUT_SESSION, watcher);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public ZooKeeper zoo() {
        return zoo;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
}
