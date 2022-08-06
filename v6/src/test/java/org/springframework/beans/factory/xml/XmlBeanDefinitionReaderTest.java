package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.LifecycleBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AssertionUtil;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
        final Resource resource = resourceLoader.getResource("classpath:org/springframework/beans/factory/xml/test.xml");
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
                if (bean instanceof TestBean) {
                    ((TestBean) bean).setPostProcessed(true);
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
        TestBean tb2 = (TestBean) factory.getBean("singletonFactory");
        AssertionUtil.assertTrue("Singleton references ==", tb == tb2);
        AssertionUtil.assertTrue("FactoryBean is BeanFactoryAware", dummyFactory.getBeanFactory() != null);
    }
}