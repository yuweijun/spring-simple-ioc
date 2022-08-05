package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.factory.BeanCreationException;
import com.example.spring.simple.ioc.beans.factory.BeanNotOfRequiredTypeException;
import com.example.spring.simple.ioc.beans.factory.FactoryBean;
import com.example.spring.simple.ioc.beans.factory.ListableBeanFactory;
import com.example.spring.simple.ioc.beans.factory.NoSuchBeanDefinitionException;
import com.example.spring.simple.ioc.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaticListableBeanFactory implements ListableBeanFactory {

    private final Map<String, Object> beans = new HashMap<>();

    @Override
    public Object getBean(String name) throws BeansException {
        Object bean = this.beans.get(name);
        if (bean instanceof FactoryBean) {
            try {
                return ((FactoryBean) bean).getObject();
            } catch (Exception ex) {
                throw new BeanCreationException("FactoryBean threw exception on object creation", ex);
            }
        }
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(name, "defined beans are [" + StringUtils.collectionToCommaDelimitedString(this.beans.keySet()) + "]");
        }
        return bean;
    }

    @Override
    public Object getBean(String name, Class requiredType) throws BeansException {
        Object bean = getBean(name);
        if (!requiredType.isAssignableFrom(bean.getClass())) {
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
        }
        return bean;
    }

    @Override
    public boolean containsBean(String name) {
        return this.beans.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        Object bean = getBean(name);
        // in case of FactoryBean, return singleton status of created object
        if (bean instanceof FactoryBean) {
            return ((FactoryBean) bean).isSingleton();
        } else {
            return true;
        }
    }

    @Override
    public String[] getAliases(String name) {
        final Object bean = this.beans.get(name);
        final Set<Map.Entry<String, Object>> entries = this.beans.entrySet();
        List<String> aliases = new LinkedList<>();
        for (Map.Entry<String, Object> entry : entries) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value != null && value.equals(bean)) {
                aliases.add(key);
            }
        }
        return aliases.toArray(new String[0]);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beans.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beans.keySet().toArray(new String[this.beans.keySet().size()]);
    }

    @Override
    public String[] getBeanDefinitionNames(Class type) {
        List matches = new ArrayList();
        Set keys = this.beans.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            Class clazz = this.beans.get(name).getClass();
            if (type.isAssignableFrom(clazz)) {
                matches.add(name);
            }
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    @Override
    public boolean containsBeanDefinition(String name) {
        return this.beans.containsKey(name);
    }

    @Override
    public Map<String, Object> getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
        Map<String, Object> matches = new HashMap<>();
        Set<String> keys = this.beans.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Object bean = this.beans.get(name);
            if (bean instanceof FactoryBean && includeFactoryBeans) {
                FactoryBean factory = (FactoryBean) bean;
                Class objectType = factory.getObjectType();
                if ((objectType == null && factory.isSingleton()) || ((factory.isSingleton() || includePrototypes) && objectType != null && type.isAssignableFrom(objectType))) {
                    Object createdObject = getBean(name);
                    if (type.isInstance(createdObject)) {
                        matches.put(name, createdObject);
                    }
                }
            } else if (type.isAssignableFrom(bean.getClass())) {
                matches.put(name, bean);
            }
        }
        return matches;
    }

    public void addBean(String name, Object bean) {
        this.beans.put(name, bean);
    }
}
