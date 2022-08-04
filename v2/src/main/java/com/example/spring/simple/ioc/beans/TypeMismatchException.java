package com.example.spring.simple.ioc.beans;

import java.beans.PropertyChangeEvent;

public class TypeMismatchException extends PropertyAccessException {

    private final Class requiredType;

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType) {
        this(propertyChangeEvent, requiredType, null);
    }

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType, Throwable ex) {
        super(propertyChangeEvent,
            "Failed to convert property value of type [" +
                (propertyChangeEvent.getNewValue() != null ?
                    propertyChangeEvent.getNewValue().getClass().getName() : null) +
                "] to required type [" + requiredType.getName() + "]" +
                (propertyChangeEvent.getPropertyName() != null ?
                    " for property '" + propertyChangeEvent.getPropertyName() + "'" : ""),
            ex);
        this.requiredType = requiredType;
    }

    public Class getRequiredType() {
        return requiredType;
    }

    public String getErrorCode() {
        return "typeMismatch";
    }

}
