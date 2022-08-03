package com.example.spring.simple.ioc.beans.factory;

import com.example.spring.simple.ioc.beans.BeansException;

public class BeanNotOfRequiredTypeException extends BeansException {

    private final String name;

    private final Class requiredType;

    private final Object actualInstance;

    public BeanNotOfRequiredTypeException(String name, Class requiredType, Object actualInstance) {
        super("Bean named '" + name + "' must be of type [" + requiredType.getName() +
            "], but was actually of type [" + actualInstance.getClass().getName() + "]", null);
        this.name = name;
        this.requiredType = requiredType;
        this.actualInstance = actualInstance;
    }

    public String getBeanName() {
        return name;
    }

    public Class getRequiredType() {
        return requiredType;
    }

    public Class getActualType() {
        return actualInstance.getClass();
    }

    public Object getActualInstance() {
        return actualInstance;
    }

}


