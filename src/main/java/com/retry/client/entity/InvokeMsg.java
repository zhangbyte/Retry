package com.retry.client.entity;

import java.util.Arrays;

/**
 * Created by zbyte on 17-7-31.
 *
 * 远端调用信息
 */
public class InvokeMsg {

    private String uuid;

    private String interfc;

    private String method;

    private byte[] args;

    private long times;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInterfc() {
        return interfc;
    }

    public void setInterfc(String interfc) {
        this.interfc = interfc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public byte[] getArgs() {
        return args;
    }

    public void setArgs(byte[] args) {
        this.args = args;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "InvokeMsg{" +
                "uuid='" + uuid + '\'' +
                ", interfc='" + interfc + '\'' +
                ", method='" + method + '\'' +
                ", args=" + Arrays.toString(args) +
                ", times=" + times +
                '}';
    }
}
