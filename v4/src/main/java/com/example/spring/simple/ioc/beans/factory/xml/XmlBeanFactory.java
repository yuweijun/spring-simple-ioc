package com.example.spring.simple.ioc.beans.factory.xml;

import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.factory.BeanFactory;
import com.example.spring.simple.ioc.beans.factory.support.DefaultListableBeanFactory;
import com.example.spring.simple.ioc.core.io.InputStreamResource;
import com.example.spring.simple.ioc.core.io.Resource;

import java.io.InputStream;

/**
 * Convenience extension of DefaultListableBeanFactory that reads bean definitions from an XML document. Delegates to DefaultXmlBeanDefinitionReader underneath; effectively equivalent to using a DefaultXmlBeanDefinitionReader for a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary to produce this format). "beans" doesn't need to be the root element of the XML document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the DefaultListableBeanFactory
 * superclass, and relies on the latter's implementation of the BeanFactory interface. It supports singletons, prototypes and references to either of these kinds of bean.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: XmlBeanFactory.java,v 1.25 2004-03-18 02:46:12 trisberg Exp $
 * @since 15 April 2001
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {

    private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    /**
     * Create a new XmlBeanFactory with the given resource, which must be parsable using DOM.
     *
     * @param resource XML resource to load bean definitions from
     * @throws BeansException in case of loading or parsing errors
     */
    public XmlBeanFactory(Resource resource) throws BeansException {
        this(resource, null);
    }

    /**
     * Create a new XmlBeanFactory with the given InputStream, which must be parsable using DOM.
     * <p>It's preferable to use a Resource argument instead of an
     * InputStream, to retain location information. This constructor is mainly kept for backward compatibility.
     *
     * @param is XML InputStream to load bean definitions from
     * @throws BeansException in case of loading or parsing errors
     * @see #XmlBeanFactory(Resource)
     */
    public XmlBeanFactory(InputStream is) throws BeansException {
        this(new InputStreamResource(is, "(no description)"), null);
    }

    /**
     * Create a new XmlBeanFactory with the given input stream, which must be parsable using DOM.
     *
     * @param resource          XML resource to load bean definitions from
     * @param parentBeanFactory parent bean factory
     * @throws BeansException in case of loading or parsing errors
     */
    public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
        super(parentBeanFactory);
        this.reader.loadBeanDefinitions(resource);
    }

}
