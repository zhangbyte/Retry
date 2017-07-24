package com.retry.proxy;

import com.retry.utils.SpringContextUtil;
import com.retry.utils.XmlParser;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

/**
 * Created by zbyte on 17-7-24.
 */
public class AutoProxy {

    private DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

    public AutoProxy() {
        List<XmlParser.Item> list = null;
        try {
            list = XmlParser.parse(new ClassPathResource("kepler-client.xml").getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null) {
            for (XmlParser.Item item : list) {
                RootBeanDefinition beanDefinition = new RootBeanDefinition(ProxyFactory.class);
                ConstructorArgumentValues args = new ConstructorArgumentValues();
                args.addGenericArgumentValue(item.getInterfc());
                beanDefinition.setConstructorArgumentValues(args);
                beanDefinition.getPropertyValues().addPropertyValue("obj", new RuntimeBeanReference(item.getBeanId()));
                beanDefinition.getPropertyValues().addPropertyValue("utils", new RuntimeBeanReference("retry.utils"));
                beanDefinition.setPrimary(true);
                beanFactory.registerBeanDefinition(item.getBeanId()+"_proxy", beanDefinition);
            }
        }
    }
}
