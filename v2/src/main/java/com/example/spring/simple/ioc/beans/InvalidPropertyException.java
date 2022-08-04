package com.example.spring.simple.ioc.beans;

public class InvalidPropertyException extends FatalBeanException {

    private final Class beanClass;

    private final String propertyName;

    public InvalidPropertyException(Class beanClass, String propertyName, String msg) {
        this(beanClass, propertyName, msg, null);
    }

    public InvalidPropertyException(Class beanClass, String propertyName, String msg, Throwable ex) {
        super("Invalid property '" + propertyName + "' of bean class [" + beanClass.getName() + "]: " + msg, ex);
        this.beanClass = beanClass;
        this.propertyName = propertyName;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

}
