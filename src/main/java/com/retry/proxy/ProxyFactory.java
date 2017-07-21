package com.retry.proxy;

import com.retry.utils.Utils;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Created by zbyte on 17-7-19.
 */
public class ProxyFactory<T> implements FactoryBean<T> {

    private final Class<?> clazz;
    private final Object obj;
    private final Utils utils;

    public ProxyFactory(Class<?> clazz, Object obj, Utils utils) {
        this.clazz = clazz;
        this.obj = obj;
        this.utils = utils;
    }

    public T getObject() throws Exception {
        AsyncHandler handler = new AsyncHandler(obj, utils.getHeadersContext(), utils.getExecutor());
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    public Class<?> getObjectType() {
        return clazz;
    }

    public boolean isSingleton() {
        return true;
    }
}
