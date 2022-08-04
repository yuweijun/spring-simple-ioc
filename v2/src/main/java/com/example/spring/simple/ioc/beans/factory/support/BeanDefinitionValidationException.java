package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.beans.FatalBeanException;

public class BeanDefinitionValidationException extends FatalBeanException {

	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}

	public BeanDefinitionValidationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
