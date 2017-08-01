package com.retry.proxy;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.annotation.Retryable;
import com.retry.config.PropertiesUtils;
import com.retry.dao.ClientDao;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by zbyte on 17-7-28.
 *
 * 持久化处理器，先正常调用一次，若失败则将该次调用持久化
 */
public class RetryHandler implements InvocationHandler {

    private static final String TABLE = PropertiesUtils.get("client.db.table", "retry");

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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Retryable.class) == null || headersContext.get().get(STR_UUID) != null) {
            return method.invoke(obj, args);
        } else {
            String uuid = UUID.randomUUID().toString();
            try {
                // 先执行一次,如果不成功则持久化
                headersContext.set(new LazyHeaders().put(STR_UUID, uuid));
                return method.invoke(obj, args);
            } catch (Exception e) {
                // 持久化
                ByteArrayOutputStream byteArgs = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteArgs);
                out.writeObject(args);
                out.close();
                clientDao.insert(TABLE, uuid, obj.getClass().getInterfaces()[0].getName(),
                        method.getName(), byteArgs.toByteArray());
            }
        }

        return null;
    }
}
