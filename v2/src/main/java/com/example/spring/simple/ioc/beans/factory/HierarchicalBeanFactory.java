package com.example.spring.simple.ioc.beans.factory;

public interface HierarchicalBeanFactory extends BeanFactory {

    BeanFactory getParentBeanFactory();

}
