package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;

/**
 * Editor for java.lang.Class, to directly feed a Class property instead of using a String class name property.
 *
 * <p>Also supports "java.lang.String[]"-style array class names,
 * in contrast to the standard Class.forName method.
 *
 * @author Juergen Hoeller
 * @see Class
 * @see Class#forName
 * @since 13.05.2003
 */
public class ClassEditor extends PropertyEditorSupport {

    public static final String ARRAY_SUFFIX = "[]";

    public String getAsText() {
        Class clazz = (Class) getValue();
        return (clazz.isArray() ? clazz.getComponentType().getName() + ARRAY_SUFFIX : clazz.getName());
    }

    public void setAsText(String text) throws IllegalArgumentException {
        Class clazz = null;
        try {
            if (text.endsWith(ARRAY_SUFFIX)) {
                // special handling for array class names
                String elementClassName = text.substring(0, text.length() - ARRAY_SUFFIX.length());
                Class elementClass = Class.forName(elementClassName, true, Thread.currentThread().getContextClassLoader());
                clazz = Array.newInstance(elementClass, 0).getClass();
            } else {
                clazz = Class.forName(text, true, Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Invalid class name: " + ex.getMessage());
        }
        setValue(clazz);
    }

}
