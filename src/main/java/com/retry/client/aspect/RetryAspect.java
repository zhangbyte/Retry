package com.retry.client.aspect;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.config.PropertiesUtils;
import com.retry.client.dao.ClientDao;
import com.retry.client.proxy.RetryHandler;
import com.retry.exception.RetryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by zbyte on 17-8-1.
 *
 * 客户端切面，对含有@Retryable的实现类方法拦截并织入retry逻辑
 */
@Component
@Aspect
public class RetryAspect {

    private static final String TABLE = PropertiesUtils.get("db.table", "retry");

    private static final Log LOGGER = LogFactory.getLog(RetryAspect.class);

    @Autowired
    private ClientDao clientDao;
    @Autowired
    private HeadersContext headersContext;

    @Pointcut("@annotation(com.retry.annotation.Retryable)")
    public void retry(){}

    @Around("retry()")
    public void around(ProceedingJoinPoint joinPoint) {
        if (headersContext.get().get(RetryHandler.STR_UUID) != null) {
            // headers已经含有uuid，则处于retry嵌套内，直接忽略该层的retry逻辑
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable.getMessage());
            }
        } else {
            String uuid = UUID.randomUUID().toString();
            try {
                // 先执行一次,如果不成功则持久化
                headersContext.set(new LazyHeaders().put(RetryHandler.STR_UUID, uuid));
                joinPoint.proceed();
            } catch (Throwable throwable) {
                // 持久化
                ByteArrayOutputStream byteArgs = new ByteArrayOutputStream();
                ObjectOutputStream out = null;
                try {
                    out = new ObjectOutputStream(byteArgs);
                    out.writeObject(joinPoint.getArgs());
                    out.close();
                    String interfc = joinPoint.getTarget().getClass().getInterfaces()[0].getName();
                    clientDao.insert(TABLE, uuid, interfc, joinPoint.getSignature().getName(),
                            byteArgs.toByteArray(), Arrays.toString(joinPoint.getArgs()));
                } catch (IOException e) {
                    RetryAspect.LOGGER.warn(e);
                    throw new RetryException(e.getMessage());
                }
            } finally {
                // 删除使用过的headers
                headersContext.get().delete(RetryHandler.STR_UUID);
            }
        }
    }
}
