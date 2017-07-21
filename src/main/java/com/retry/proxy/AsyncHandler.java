package com.retry.proxy;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.annotation.Retryable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by zbyte on 17-7-21.
 */
public class AsyncHandler implements InvocationHandler {

    public static final String STR_UUID = "UUID";

    private static final int MAX_TIMES = 5;

    private final Object obj;
    private final HeadersContext headersContext;
    private final ExecutorService executor;

    public AsyncHandler(Object obj, HeadersContext headersContext, ExecutorService executor) {
        this.obj = obj;
        this.headersContext = headersContext;
        this.executor = executor;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Retryable.class) != null) {
            headersContext.set(new LazyHeaders().put(STR_UUID, UUID.randomUUID().toString()));
            retry(method, args);
        } else {
            method.invoke(obj, args);
        }
        return true;
    }

    private void retry(final Method method, final Object[] args) {
        Future<Boolean> result = null;
        Boolean isSuccess = false;
        int times = 0;
        int timeout = 1000;

        while(!isSuccess && times < MAX_TIMES) {
            System.out.println("向B发送一个请求，当前等待响应延迟："+timeout+"ms");
            result = executor.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    try {
                        // 模拟网络延迟
                        Thread.sleep(4000);
                        return (Boolean) method.invoke(obj, args);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return false;
                    }
                }
            });
            try {
                isSuccess = result.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                // 超时
                timeout += 1000;
            }
            times++;
        }
        executor.shutdown();
        if (!isSuccess) {
            throw new RuntimeException("TaskB error");
        }
    }
}
