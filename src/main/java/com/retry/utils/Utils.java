package com.retry.utils;

import com.kepler.header.HeadersContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * Created by zbyte on 17-7-21.
 */
public class Utils {

    private final HeadersContext headersContext;
    private final ExecutorService executor;

    public Utils(HeadersContext headersContext, ExecutorService executor) {
        this.headersContext = headersContext;
        this.executor = executor;
    }

    public HeadersContext getHeadersContext() {
        return headersContext;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
