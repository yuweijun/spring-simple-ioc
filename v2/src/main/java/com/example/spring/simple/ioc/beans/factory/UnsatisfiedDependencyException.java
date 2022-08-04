package com.example.spring.simple.ioc.beans.factory;

public class UnsatisfiedDependencyException extends BeanCreationException {

	public UnsatisfiedDependencyException(String resourceDescription, String beanName, int ctorArgIndex,
																				Class ctorArgType, String msg) {
		super(resourceDescription, beanName,
					"Unsatisfied dependency expressed through constructor argument with index " +
					ctorArgIndex + " of type [" + ctorArgType.getName() + "]" +
					(msg != null ? ": " + msg : ""));
	}

	public UnsatisfiedDependencyException(String resourceDescription, String beanName, String propertyName,
																				String msg) {
		super(resourceDescription, beanName,
					"Unsatisfied dependency expressed through bean property '" + propertyName + "'" +
					(msg != null ? ": " + msg : ""));
	}

}
