package com.retry.aspect;

import com.kepler.header.HeadersContext;
import com.retry.annotation.Retryable;
import com.retry.config.PropertiesUtils;
import com.retry.dao.ServerDao;
import com.retry.proxy.RetryHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;

/**
 * Created by zbyte on 17-7-20.
 *
 * 服务端切面，对kepler的@Autowired进行拦截，并织入幂等判断逻辑
 */
@Component
@Aspect
@Order(Integer.MAX_VALUE)
public class KeplerAspect {

    private static final String TABLE = PropertiesUtils.get("db.table", "retry");

    @Autowired
    @Qualifier("retry_transactionTemplate")
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ServerDao serverDao;
    @Autowired
    private HeadersContext headersContext;

    @Pointcut("@within(com.kepler.annotation.Autowired)")
    public void retry(){}

    @Around("retry()")
    public Object around(final ProceedingJoinPoint joinPoint) {
        Object[] objs = joinPoint.getArgs();
        Class[] params = new Class[objs.length];
        for (int i=0; i<objs.length; i++) {
            params[i] = objs[i].getClass();
        }
        //判断对象的接口方法是否有@retryable注解
        boolean isRetry = false;
        Class<?>[] interfaces = joinPoint.getTarget().getClass().getInterfaces();
        for (Class interfc : interfaces) {
            try {
                Method method = interfc.getMethod(joinPoint.getSignature().getName(), params);
                if (method.getAnnotation(Retryable.class) != null) {
                    isRetry = true;
                    break;
                }
            } catch (NoSuchMethodException e) {
            }
        }
        if (isRetry) {
            return transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus transactionStatus) {
                    String uuid = headersContext.get().get(RetryHandler.STR_UUID);
                    int resultCount = serverDao.insert(TABLE, uuid);
                    if (resultCount > 0) {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable.getMessage());
                        }
                    }
                    return true;
                }
            });
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
