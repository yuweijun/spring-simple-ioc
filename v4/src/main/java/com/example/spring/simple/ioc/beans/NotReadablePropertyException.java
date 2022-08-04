package com.example.spring.simple.ioc.beans;

/**
 * Exception thrown on an attempt to get the value of a property that isn't readable, because there's no getter method.
 *
 * @author Juergen Hoeller
 * @since 01.06.2004
 */
public class NotReadablePropertyException extends InvalidPropertyException {

    /**
     * Create a new NotReadablePropertyException.
     *
     * @param beanClass    the offending bean class
     * @param propertyName the offending property
     */
    public NotReadablePropertyException(Class beanClass, String propertyName) {
        super(beanClass, propertyName, "Property '" + propertyName + "' is not readable");
    }

}
