package com.example.spring.simple.ioc.beans.factory;

public interface FactoryBean {

    Object getObject() throws Exception;

    Class getObjectType();

    boolean isSingleton();

}
