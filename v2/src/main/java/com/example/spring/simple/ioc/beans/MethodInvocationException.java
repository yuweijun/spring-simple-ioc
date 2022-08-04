package com.example.spring.simple.ioc.beans;

import java.beans.PropertyChangeEvent;

public class MethodInvocationException extends PropertyAccessException {

	public MethodInvocationException(PropertyChangeEvent propertyChangeEvent, Throwable ex) {
		super(propertyChangeEvent, "Property '" + propertyChangeEvent.getPropertyName() + "' threw exception", ex);
	}

	public String getErrorCode() {
		return "methodInvocation";
	}

}
