package com.retry.aspect;

import com.kepler.header.Headers;
import com.kepler.header.HeadersContext;
import com.retry.annotation.Retryable;
import com.retry.config.PropertiesUtils;
import com.retry.dao.RetryDao;
import com.retry.proxy.AsyncHandler;
import com.retry.proxy.ProxyFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by zbyte on 17-7-20.
 */
@Component
@Aspect
@Order(1)
public class RetryAspect {

    private static final String TABLE = PropertiesUtils.get("retry.table", "retry");

    @Autowired
    private RetryDao retryDao;
    @Autowired
    private HeadersContext headersContext;

    @Pointcut("@within(com.kepler.annotation.Autowired)")
    public void retry(){}

    @Around("retry()")
    public Object around(ProceedingJoinPoint joinPoint) {
        //判断对象上的接口是否有@retryable注解
        boolean isRetry = false;
        Class<?>[] interfaces = joinPoint.getTarget().getClass().getInterfaces();
        for (Class interfc : interfaces) {
            for (Method method : interfc.getMethods()) {
                if (method.getAnnotation(Retryable.class) != null) {
                    isRetry = true;
                    break;
                }
            }
        }
        if (isRetry) {
            String uuid = headersContext.get().get(AsyncHandler.STR_UUID);
            int resultCount = retryDao.insert(TABLE, uuid);
            if (resultCount > 0) {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        } else {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return true;
    }
}
