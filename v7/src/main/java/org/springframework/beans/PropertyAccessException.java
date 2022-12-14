package org.springframework.beans;

import org.springframework.core.ErrorCoded;

import java.beans.PropertyChangeEvent;

public abstract class PropertyAccessException extends BeansException implements ErrorCoded {

    private final PropertyChangeEvent propertyChangeEvent;

    public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg) {
        super(msg);
        this.propertyChangeEvent = propertyChangeEvent;
    }

    public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, Throwable ex) {
        super(msg, ex);
        this.propertyChangeEvent = propertyChangeEvent;
    }

    public PropertyChangeEvent getPropertyChangeEvent() {
        return propertyChangeEvent;
    }

}
