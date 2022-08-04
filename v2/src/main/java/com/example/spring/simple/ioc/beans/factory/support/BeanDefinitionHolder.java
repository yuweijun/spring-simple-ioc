package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.beans.factory.config.BeanDefinition;

/**
 * Holder for a BeanDefinition with name and aliases.
 *
 * <p>Recognized by AbstractAutowireCapableBeanFactory for inner
 * bean definitions. Registered by DefaultXmlBeanDefinitionParser, which also uses it as general holder for a parsed bean definition.
 *
 * <p>Can also be used for programmatic registration of inner bean
 * definitions. If you don't care about BeanNameAware and the like, registering RootBeanDefinition or ChildBeanDefinition is good enough.
 *
 * @author Juergen Hoeller
 * @see AbstractAutowireCapableBeanFactory#resolveValueIfNecessary
 * @since 29.04.2004
 */
public class BeanDefinitionHolder {

    private final BeanDefinition beanDefinition;

    private final String beanName;

    private final String[] aliases;

    /**
     * Create a new BeanDefinitionHolder.
     *
     * @param beanDefinition the BeanDefinition
     * @param beanName       the name of the bean
     */
    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
        this(beanDefinition, beanName, null);
    }

    /**
     * Create a new BeanDefinitionHolder.
     *
     * @param beanDefinition the BeanDefinition
     * @param beanName       the name of the bean
     * @param aliases        alias names of the bean, or null if none
     */
    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    public String[] getAliases() {
        return aliases;
    }

}
