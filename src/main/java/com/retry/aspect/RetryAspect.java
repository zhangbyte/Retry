package com.retry.aspect;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.config.PropertiesUtils;
import com.retry.dao.ClientDao;
import com.retry.proxy.RetryHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * Created by zbyte on 17-8-1.
 */
@Component
@Aspect
public class RetryAspect {

    private static final String TABLE = PropertiesUtils.get("client.db.table", "retry");

    @Autowired
    private ClientDao clientDao;
    @Autowired
    private HeadersContext headersContext;


    @Pointcut("@annotation(com.retry.annotation.Retryable)")
    public void retry(){}

    @Around("retry()")
    public void around(ProceedingJoinPoint joinPoint) {

        if (headersContext.get().get(RetryHandler.STR_UUID) != null) {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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
                    clientDao.insert(TABLE, uuid, interfc, joinPoint.getSignature().getName(), byteArgs.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
