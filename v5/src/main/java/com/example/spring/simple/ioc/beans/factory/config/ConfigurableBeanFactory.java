package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.factory.BeanFactory;
import com.example.spring.simple.ioc.beans.factory.HierarchicalBeanFactory;

import java.beans.PropertyEditor;

public interface ConfigurableBeanFactory extends HierarchicalBeanFactory {

    void setParentBeanFactory(BeanFactory parentBeanFactory);

    void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor);

    void ignoreDependencyType(Class type);

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    void registerAlias(String beanName, String alias) throws BeansException;

    void registerSingleton(String beanName, Object singletonObject) throws BeansException;

    void destroySingletons();

}
