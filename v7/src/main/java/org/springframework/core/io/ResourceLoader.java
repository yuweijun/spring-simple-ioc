package org.springframework.core.io;

/**
 * Interface to be implemented by objects that can load resources. An ApplicationContext is required to provide this functionality.
 *
 * <p>DefaultResourceLoader is a standalone implementation that is
 * usable outside an ApplicationContext, also used by ResourceEditor.
 *
 * @author Juergen Hoeller
 * @see DefaultResourceLoader
 * @see ResourceEditor
 * @since 10.03.2004
 */
public interface ResourceLoader {

    /**
     * Pseudo URL prefix for loading from the class path
     */
    String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * Return a Resource handle for the specified resource. The handle should always be a reusable resource descriptor, allowing for multiple getInputStream calls.
     * <p><ul>
     * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
     * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
     * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
     * (This will be implementation-specific, typically provided by an ApplicationContext implementation.)
     * </ul>
     * <p>Note that a Resource handle does not imply an existing resource;
     * you need to invoke Resource's "exists" to check for existence.
     *
     * @param location resource location
     * @return Resource handle
     * @see #CLASSPATH_URL_PREFIX
     * @see Resource#exists
     * @see Resource#getInputStream
     */
    Resource getResource(String location);

}
