package com.retry.aspect;

import com.kepler.header.HeadersContext;
import com.retry.annotation.Retryable;
import com.retry.config.PropertiesUtils;
import com.retry.dao.RetryDao;
import com.retry.proxy.AsyncHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by zbyte on 17-7-20.
 */
@Component
@Aspect
@Order(Integer.MAX_VALUE)
public class RetryAspect {

    private static final String TABLE = PropertiesUtils.get("retry.table", "retry");

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RetryDao retryDao;
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
        boolean isTx = false;
        Class<?>[] interfaces = joinPoint.getTarget().getClass().getInterfaces();
        for (Class interfc : interfaces) {
            try {
                Method method = interfc.getMethod(joinPoint.getSignature().getName(), params);
                if (method.getAnnotation(Retryable.class) != null) {
                    //判断对象方法上是否有tx注解
                    try {
                        Method method1 = joinPoint.getTarget().getClass().getMethod(joinPoint.getSignature().getName(), params);
                        if (method1.getAnnotation(Transactional.class) != null) {
                            isTx = true;
                        }
                    }catch (NoSuchMethodException e) {
                    }
                    isRetry = true;
                    break;
                }
            } catch (NoSuchMethodException e) {
            }
        }
        if (isRetry) {
            if (isTx) {
                String uuid = headersContext.get().get(AsyncHandler.STR_UUID);
                int resultCount = retryDao.insert(TABLE, uuid);
                if (resultCount > 0) {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable.getMessage());
                    }
                }
                return true;
            } else {
                return transactionTemplate.execute(new TransactionCallback() {
                    @Override
                    public Object doInTransaction(TransactionStatus transactionStatus) {
                        String uuid = headersContext.get().get(AsyncHandler.STR_UUID);
                        int resultCount = retryDao.insert(TABLE, uuid);
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
