package com.example.spring.simple.ioc.beans;

import com.example.spring.simple.ioc.core.NestedRuntimeException;

public abstract class BeansException extends NestedRuntimeException {

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
