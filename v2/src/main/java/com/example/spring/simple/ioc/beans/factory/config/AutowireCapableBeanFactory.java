package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.factory.BeanFactory;

public interface AutowireCapableBeanFactory extends BeanFactory {

    int AUTOWIRE_BY_NAME = 1;

    int AUTOWIRE_BY_TYPE = 2;

    int AUTOWIRE_CONSTRUCTOR = 3;

    int AUTOWIRE_AUTODETECT = 4;

    Object autowire(Class beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException;

    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String name) throws BeansException;

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String name) throws BeansException;

}
