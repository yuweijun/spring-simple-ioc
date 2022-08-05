package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.factory.ListableBeanFactory;

/**
 * SPI interface to be implemented by most if not all listable bean factories. In addition to ConfigurableBeanFactory, provides a way to pre-instantiate singletons.
 *
 * <p>Allows for framework-internal plug'n'play, e.g. in AbstractApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 */
public interface ConfigurableListableBeanFactory
    extends ListableBeanFactory, ConfigurableBeanFactory, AutowireCapableBeanFactory {

    /**
     * Ensure that all non-lazy-init singletons are instantiated, also considering FactoryBeans. Typically invoked at the end of factory setup, if desired.
     * <p>As this is a startup method, it should destroy already created singletons
     * if it fails, to avoid dangling resources. In other words, after invocation of that method, either all or no singletons at all should be instantiated.
     *
     * @throws BeansException if one of the singleton beans could not be created
     */
    void preInstantiateSingletons() throws BeansException;

}
