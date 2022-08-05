package com.example.spring.simple.ioc.beans.factory;


import com.example.spring.simple.ioc.beans.TestBean;

import java.util.HashMap;
import java.util.Map;

public class DummyBeanFactory implements BeanFactory {

    public Map<String, Object> map = new HashMap<>();

    {
        map.put("test", new TestBean());
        map.put("s", "");
    }

    public Object getBean(String name) {
        Object bean = map.get(name);
        if (bean == null)
            throw new NoSuchBeanDefinitionException(name, "no message");
        return bean;
    }

    public Object getBean(String name, Class requiredType) {
        return getBean(name);
    }

    public boolean containsBean(String name) {
        return map.containsKey(name);
    }

    public boolean isSingleton(String name) {
        return true;
    }

    public String[] getAliases(String name) {
        throw new UnsupportedOperationException("getAliases");
    }
}
