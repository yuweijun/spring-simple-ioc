package org.springframework.beans.factory.support;

import org.springframework.beans.TestBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StaticListableBeanFactoryTest {

    StaticListableBeanFactory beanFactory;

    @BeforeEach
    public void before() {
        beanFactory = new StaticListableBeanFactory();
        final TestBean bean = new TestBean();
        bean.setName("bean name");
        beanFactory.addBean("testBean1", bean);
        beanFactory.addBean("testBean2", bean);
        beanFactory.addBean("strBean", new String());
    }

    @Test
    public void testGetBean() {
        final Object bean = beanFactory.getBean("testBean1");
        assertNotNull(bean);
        System.out.println(bean);
    }

    @Test
    public void testContainsBean() {
        final boolean contains = beanFactory.containsBean("strBean");
        System.out.println("strBean contains : " + contains);
    }

    @Test
    public void testIsSingleton() {
        final boolean singleton = beanFactory.isSingleton("testBean1");
        System.out.println(singleton);
    }

    @Test
    public void testGetAliases() {
        final String[] aliases = beanFactory.getAliases("testBean1");
        System.out.println(Arrays.toString(aliases));
    }
}