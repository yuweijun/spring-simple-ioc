package com.example.spring.simple.ioc.beans.factory;

import com.example.spring.simple.ioc.beans.FatalBeanException;

public class BeanCreationException extends FatalBeanException {

    public BeanCreationException(String msg) {
        super(msg);
    }

    public BeanCreationException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public BeanCreationException(String beanName, String msg) {
        this(beanName, msg, (Throwable) null);
    }

    public BeanCreationException(String beanName, String msg, Throwable ex) {
        super("Error creating bean with name '" + beanName + "': " + msg, ex);
    }

    public BeanCreationException(String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, null);
    }

    public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable ex) {
        super("Error creating bean with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, ex);
    }

}
