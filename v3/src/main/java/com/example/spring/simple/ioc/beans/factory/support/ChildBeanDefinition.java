package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.beans.MutablePropertyValues;

public class ChildBeanDefinition extends AbstractBeanDefinition {

    private final String parentName;

    public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
        super(pvs);
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
    }

    public void validate() throws BeanDefinitionValidationException {
        super.validate();
        if (this.parentName == null) {
            throw new BeanDefinitionValidationException("parentName must be set in ChildBeanDefinition");
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Child bean with parent '");
        sb.append(getParentName()).append("'");
        if (getResourceDescription() != null) {
            sb.append(" defined in ").append(getResourceDescription());
        }
        return sb.toString();
    }

}
