package com.example.spring.simple.ioc.beans.factory;

public class FactoryBeanCircularReferenceException extends BeanCreationException {

	public FactoryBeanCircularReferenceException(String beanName, String msg) {
		super(beanName, msg);
	}

}
