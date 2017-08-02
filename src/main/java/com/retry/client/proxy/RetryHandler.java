package com.retry.client.proxy;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.annotation.Retryable;
import com.retry.config.PropertiesUtils;
import com.retry.client.dao.ClientDao;
import com.retry.exception.RetryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by zbyte on 17-7-28.
 *
 * 持久化处理器，先正常调用一次，若失败则将该次调用持久化
 */
public class RetryHandler implements InvocationHandler {

    private static final String TABLE = PropertiesUtils.get("db.table", "retry");

    private static final Log LOGGER = LogFactory.getLog(RetryHandler.class);

    public static final String STR_UUID = "UUID";

    private final Object obj;
    private final HeadersContext headersContext;
    private final ClientDao clientDao;

    public RetryHandler(Object obj, HeadersContext headersContext, ClientDao clientDao) {
        this.obj = obj;
        this.headersContext = headersContext;
        this.clientDao = clientDao;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        if (method.getAnnotation(Retryable.class) == null || headersContext.get().get(STR_UUID) != null) {
            try {
                return method.invoke(obj, args);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            String uuid = UUID.randomUUID().toString();
            try {
                // 先执行一次,如果不成功则持久化
                headersContext.set(new LazyHeaders().put(STR_UUID, uuid));
                return method.invoke(obj, args);
            } catch (Exception e) {
                // 持久化
                try {
                    ByteArrayOutputStream byteArgs = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteArgs);
                    out.writeObject(args);
                    out.close();
                    clientDao.insert(TABLE, uuid, obj.getClass().getInterfaces()[0].getName(),
                            method.getName(), byteArgs.toByteArray());
                } catch (IOException e1) {
                    RetryHandler.LOGGER.warn(e1);
                    throw new RetryException(e1.getMessage());
                }
            }
        }

        return null;
    }
}
