package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.BeansException;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	void postProcessBeforeDestruction(Object bean, String name) throws BeansException;

}
