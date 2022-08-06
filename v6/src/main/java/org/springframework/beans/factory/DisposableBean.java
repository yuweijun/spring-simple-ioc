package org.springframework.beans.factory;

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Interface to be implemented by beans that want to release resources on destruction. A BeanFactory is supposed to invoke the destroy method if it disposes a cached singleton. An application context is supposed to dispose all of its singletons on close.
 *
 * <p>An alternative to implementing DisposableBean is specifying a custom
 * destroy-method, for example in an XML bean definition. For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Juergen Hoeller
 * @see RootBeanDefinition#getDestroyMethodName
 * @since 12.08.2003
 */
public interface DisposableBean {

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors. Exceptions will get logged but not rethrown to allow other beans to release their resources too.
     */
    void destroy() throws Exception;

}
