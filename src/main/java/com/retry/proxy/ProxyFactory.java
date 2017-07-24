package com.retry.proxy;

import com.retry.utils.Utils;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Created by zbyte on 17-7-19.
 */
public class ProxyFactory<T> implements FactoryBean<T> {

    private final Class<?> clazz;
    private Object obj;
    private Utils utils;

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public void setUtils(Utils utils) {
        this.utils = utils;
    }

    public ProxyFactory(Class<?> clazz) {
        this.clazz = clazz;
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
