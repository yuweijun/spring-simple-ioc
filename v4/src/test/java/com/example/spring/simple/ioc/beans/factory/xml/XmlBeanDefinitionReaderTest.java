package com.example.spring.simple.ioc.beans.factory.xml;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.LifecycleBean;
import com.example.spring.simple.ioc.beans.MutablePropertyValues;
import com.example.spring.simple.ioc.beans.TestBean;
import com.example.spring.simple.ioc.beans.factory.BeanFactory;
import com.example.spring.simple.ioc.beans.factory.DummyFactory;
import com.example.spring.simple.ioc.beans.factory.config.BeanPostProcessor;
import com.example.spring.simple.ioc.beans.factory.support.AssertionUtil;
import com.example.spring.simple.ioc.beans.factory.support.DefaultListableBeanFactory;
import com.example.spring.simple.ioc.beans.factory.support.RootBeanDefinition;
import com.example.spring.simple.ioc.core.io.ClassPathResource;
import com.example.spring.simple.ioc.core.io.DefaultResourceLoader;
import com.example.spring.simple.ioc.core.io.Resource;
import com.example.spring.simple.ioc.core.io.ResourceLoader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class XmlBeanDefinitionReaderTest  {

    @Test
    public void testLoadBeanDefinitions() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Map m = new HashMap();
        m.put("name", "Albert");

        lbf.registerBeanDefinition("father", new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource("classpath:com/example/spring/simple/ioc/beans/factory/xml/test.xml");
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(lbf);
        reader.loadBeanDefinitions(resource);
        TestBean tb = (TestBean) lbf.getBean("singletonFactory");
        DummyFactory dummyFactory = (DummyFactory) lbf.getBean("&singletonFactory");
        TestBean tb2 = (TestBean) lbf.getBean("singletonFactory");
        final BeanFactory beanFactory = dummyFactory.getBeanFactory();
        System.out.println(beanFactory);
    }

    @Test
    public void test() {
        DefaultListableBeanFactory parent = new DefaultListableBeanFactory();
        Map m = new HashMap();
        m.put("name", "Albert");
        parent.registerBeanDefinition("father", new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));
        m = new HashMap();
        m.put("name", "Roderick");
        parent.registerBeanDefinition("rod", new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));

        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("test.xml", getClass()), parent);
        factory.addBeanPostProcessor(new BeanPostProcessor() {
            public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
                if (bean instanceof com.example.spring.simple.ioc.beans.TestBean) {
                    ((com.example.spring.simple.ioc.beans.TestBean) bean).setPostProcessed(true);
                }
                if (bean instanceof DummyFactory) {
                    ((DummyFactory) bean).setPostProcessed(true);
                }
                return bean;
            }

            public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
                return bean;
            }
        });
        factory.addBeanPostProcessor(new LifecycleBean.PostProcessor());
        factory.preInstantiateSingletons();

        TestBean tb = (TestBean) factory.getBean("singletonFactory");
        DummyFactory dummyFactory = (DummyFactory) factory.getBean("&singletonFactory");
        com.example.spring.simple.ioc.beans.TestBean tb2 = (TestBean) factory.getBean("singletonFactory");
        AssertionUtil.assertTrue("Singleton references ==", tb == tb2);
        AssertionUtil.assertTrue("FactoryBean is BeanFactoryAware", dummyFactory.getBeanFactory() != null);
    }
}