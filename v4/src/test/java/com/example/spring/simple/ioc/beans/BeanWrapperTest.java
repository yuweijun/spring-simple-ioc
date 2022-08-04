package com.example.spring.simple.ioc.beans;

import com.example.spring.simple.ioc.beans.factory.support.DependenciesBean;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanWrapperTest {

    @Test
    public void testGetPropertyValue() {
        TestBean testBean = new TestBean();
        testBean.setName("test");
        testBean.setAge(32);
        final BeanWrapper beanWrapper = new BeanWrapperImpl(testBean);
        final PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            System.out.println(propertyDescriptor);
        }

        final Class type = beanWrapper.getPropertyType("name");
        System.out.println(type);
        final Object name = beanWrapper.getPropertyValue("name");
        System.out.println(name);
        assertEquals("test", name);
    }

    @Test
    public void testSetNestedPropertyValue() {
        TestBean testBean = new TestBean();
        final BeanWrapper beanWrapper = new BeanWrapperImpl(testBean);
        PropertyValue ageValue = new PropertyValue("age", 23);
        beanWrapper.setPropertyValue(ageValue);
        System.out.println(testBean.getAge());
        assertEquals(23, testBean.getAge());

        DependenciesBean dependenciesBean = new DependenciesBean();
        PropertyValue tb = new PropertyValue("spouse", testBean);
        PropertyValue tba = new PropertyValue("spouse.age", 33);
        final BeanWrapperImpl beanWrapper1 = new BeanWrapperImpl(dependenciesBean);
        beanWrapper1.setPropertyValue(tb);
        beanWrapper1.setPropertyValue(tba);

        assertEquals(33, testBean.getAge());
    }
}