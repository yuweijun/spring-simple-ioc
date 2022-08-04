package com.example.spring.simple.ioc.beans.factory;

import com.example.spring.simple.ioc.beans.BeansException;

public interface BeanFactoryAware {

	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
