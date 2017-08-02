package com.retry.client.proxy;

import com.kepler.header.HeadersContext;
import com.retry.client.dao.ClientDao;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Created by zbyte on 17-7-19.
 */
public class ProxyFactory<T> implements FactoryBean<T> {

    private final Class<?> clazz;
    private final Object obj;
    private final HeadersContext headersContext;
    private final ClientDao clientDao;

    public ProxyFactory(Class<?> clazz, Object obj, HeadersContext headersContext, ClientDao clientDao) {
        this.clazz = clazz;
        this.obj = obj;
        this.headersContext = headersContext;
        this.clientDao = clientDao;
    }

    public T getObject() throws Exception {
        RetryHandler handler = new RetryHandler(obj, headersContext, clientDao);
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    public Class<?> getObjectType() {
        return clazz;
    }

    public boolean isSingleton() {
        return true;
    }
}
