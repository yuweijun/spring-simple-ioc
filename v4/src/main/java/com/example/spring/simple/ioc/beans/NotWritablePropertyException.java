package com.example.spring.simple.ioc.beans;

/**
 * Exception thrown on an attempt to set the value of a property that isn't writable, because there's no setter method.
 *
 * @author Rod Johnson
 */
public class NotWritablePropertyException extends InvalidPropertyException {

    /**
     * Create a new NotWritablePropertyException.
     *
     * @param beanClass    the offending bean class
     * @param propertyName the offending property
     */
    public NotWritablePropertyException(Class beanClass, String propertyName) {
        super(beanClass, propertyName, "Property '" + propertyName + "' is not writable");
    }

    /**
     * Create a new NotWritablePropertyException.
     *
     * @param beanClass    the offending bean class
     * @param propertyName the offending property
     * @param msg          the detail message
     * @param ex           the root cause
     */
    public NotWritablePropertyException(Class beanClass, String propertyName, String msg, Throwable ex) {
        super(beanClass, propertyName, msg, ex);
    }

}
