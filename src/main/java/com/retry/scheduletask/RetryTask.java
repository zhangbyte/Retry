package com.retry.scheduletask;

import com.kepler.header.HeadersContext;
import com.kepler.header.impl.LazyHeaders;
import com.retry.config.PropertiesUtils;
import com.retry.dao.ClientDao;
import com.retry.entity.InvokeMsg;
import com.retry.proxy.RetryHandler;
import com.retry.utils.SpringContextUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zbyte on 17-7-28.
 */
public class RetryTask implements Runnable{

    private static final String TABLE = PropertiesUtils.get("client.db.table", "retry");

    private ClientDao clientDao;
    private HeadersContext headersContext;

    public void setClientDao(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setHeadersContext(HeadersContext headersContext) {
        this.headersContext = headersContext;
    }

    @Override
    public void run() {
        // 从数据库中查询出需要retry的数据
        List<InvokeMsg> invokeMsgs = clientDao.selectAll(TABLE);
        for (InvokeMsg i : invokeMsgs) {
            invoke(i);
        }
    }

    public Object invoke(InvokeMsg i) {
        try {
            // 反序列化得到参数
            ByteArrayInputStream bais = new ByteArrayInputStream(i.getArgs());
            ObjectInputStream in = new ObjectInputStream(bais);
            Object[] args = (Object[]) in.readObject();
            // 获取参数类型数组
            Class[] params = new Class[args.length];
            for (int j=0; j<args.length; j++) {
                params[j] = args[j].getClass();
            }
            // 获取对象
            Class<?> cls = Class.forName(i.getInterfc());
            Object obj = SpringContextUtil.getApplicationContext().getBean(cls);
            // 获取方法
            Method method = cls.getMethod(i.getMethod(), params);
            // 往headers中放入uuid
            headersContext.set(new LazyHeaders().put(RetryHandler.STR_UUID, i.getUuid()));
            // 执行方法
            Object res = method.invoke(obj, args);
            // 删除记录
            clientDao.deleteById(TABLE, i.getUuid());

            return res;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("执行失败！！！！！！！！！！！");
        }

        return null;
    }
}
