package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Standalone XML application context, taking the context definition files from the class path. Mainly useful for test harnesses, but also for application contexts embedded within JARs.
 *
 * <p>Treats resource paths as class path resources, when using
 * ApplicationContext.getResource. Only supports full classpath resource names that include the package path, like "mypackage/myresource.dat".
 *
 * <p>The config location defaults can be overridden via setConfigLocations,
 * respectively via the "contextConfigLocation" parameters of ContextLoader and FrameworkServlet. Config locations can either denote concrete files like "/mypackage/context.xml" or Ant-style patterns like "/mypackage/*-context.xml" (see PathMatcher javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to deliberately override certain bean definitions via an extra XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: ClassPathXmlApplicationContext.java,v 1.12 2004-04-05 07:18:42 jhoeller Exp $
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

    private final String[] configLocations;

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions from the given XML file.
     *
     * @param configLocation file path
     */
    public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
        this.configLocations = new String[]{configLocation};
        refresh();
    }

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions from the given XML files.
     *
     * @param configLocations array of file paths
     */
    public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
        this.configLocations = configLocations;
        refresh();
    }

    /**
     * Create a new ClassPathXmlApplicationContext with the given parent, loading the definitions from the given XML files.
     *
     * @param configLocations array of file paths
     * @param parent          the parent context
     */
    public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent)
        throws BeansException {
        super(parent);
        this.configLocations = configLocations;
        refresh();
    }

    @Override
    protected String[] getConfigLocations() {
        return this.configLocations;
    }

}
