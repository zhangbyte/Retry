package com.retry.proxy;

import com.kepler.service.imported.ImportedServiceFactory;
import com.retry.utils.SpringContextUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Created by zbyte on 17-7-24.
 *
 * 自动生成kepler代理的代理
 */
public class AutoProxy {

    private DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

    public AutoProxy() {
        // 获取容器中 kepler proxy 的注册信息
        // <bean id="serviceB" class="com.kepler.service.imported.ImportedServiceFactory" parent="kepler.service.imported.abstract">
        //    <constructor-arg index="0" value="com.retrykepler.client.service.ServiceB" />
        // </bean>

        // 获取所有id
        String[] beanNames = beanFactory.getBeanNamesForType(ImportedServiceFactory.class);

        for (String name : beanNames) {
            // &beanId -> beanId
            String beanId = name.substring(1);
            BeanDefinition kepler_beanDefinition = beanFactory.getBeanDefinition(beanId);
            TypedStringValue value = (TypedStringValue) kepler_beanDefinition.getConstructorArgumentValues().getIndexedArgumentValue(0, String.class).getValue();
            // 获取构造参数
            String interfc = value.getValue();

            // 构造 retry proxy
            RootBeanDefinition beanDefinition = new RootBeanDefinition(ProxyFactory.class);
            ConstructorArgumentValues args = new ConstructorArgumentValues();
            args.addIndexedArgumentValue(0, interfc);
            args.addIndexedArgumentValue(1, new RuntimeBeanReference(beanId));
            args.addIndexedArgumentValue(2, new RuntimeBeanReference("kepler.header.context"));
            args.addIndexedArgumentValue(3, new RuntimeBeanReference("clientDao"));
            beanDefinition.setConstructorArgumentValues(args);
//            beanDefinition.getPropertyValues().addPropertyValue("obj", new RuntimeBeanReference(beanId));
//            beanDefinition.getPropertyValues().addPropertyValue("utils", new RuntimeBeanReference("retry.utils"));
            // 设置优先注入
            beanDefinition.setPrimary(true);
            beanFactory.registerBeanDefinition(beanId+"_proxy", beanDefinition);
        }
    }
}
