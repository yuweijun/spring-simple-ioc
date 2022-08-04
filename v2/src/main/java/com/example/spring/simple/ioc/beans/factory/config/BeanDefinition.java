package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.MutablePropertyValues;

public interface BeanDefinition {

	MutablePropertyValues getPropertyValues();

	ConstructorArgumentValues getConstructorArgumentValues();

	String getResourceDescription();

}
