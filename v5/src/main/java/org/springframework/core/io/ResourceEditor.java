package org.springframework.core.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyEditorSupport;

/**
 * Editor for Resource descriptors, to convert String locations to Resource properties automatically instead of using a String location property.
 *
 * <p>The path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * <p>Delegates to a ResourceLoader, by default DefaultResourceLoader.
 *
 * @author Juergen Hoeller
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see System#getProperty(String)
 * @since 28.12.2003
 */
public class ResourceEditor extends PropertyEditorSupport {

    public static final String PLACEHOLDER_PREFIX = "${";
    public static final String PLACEHOLDER_SUFFIX = "}";
    protected static final Log logger = LogFactory.getLog(ResourceEditor.class);
    private final ResourceLoader resourceLoader;

    /**
     * Create new ResourceEditor with DefaultResourceLoader.
     *
     * @see DefaultResourceLoader
     */
    public ResourceEditor() {
        this.resourceLoader = new DefaultResourceLoader();
    }

    /**
     * Create new ResourceEditor with given ResourceLoader.
     *
     * @param resourceLoader the ResourceLoader to use
     */
    public ResourceEditor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setAsText(String text) {
        setValue(this.resourceLoader.getResource(resolvePath(text)));
    }

    /**
     * Resolve the given path, replacing placeholders with corresponding system property values if necessary.
     *
     * @param path the original file path
     * @return the resolved file path
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    protected String resolvePath(String path) {
        int startIndex = path.indexOf(PLACEHOLDER_PREFIX);
        if (startIndex != -1) {
            int endIndex = path.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = path.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                String propVal = System.getProperty(placeholder);
                if (propVal != null) {
                    return path.substring(0, startIndex) + propVal + path.substring(endIndex + 1);
                } else {
                    logger.warn("Could not resolve placeholder '" + placeholder +
                        "' in file path [" + path + "] as system property");
                }
            }
        }
        return path;
    }

}
