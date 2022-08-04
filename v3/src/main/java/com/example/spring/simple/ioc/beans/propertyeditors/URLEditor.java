package com.example.spring.simple.ioc.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Editor for java.net.URL, to directly feed a URL property instead of using a String property.
 *
 * @author Juergen Hoeller
 * @see URL
 * @since 15.12.2003
 */
public class URLEditor extends PropertyEditorSupport {

    public String getAsText() {
        return ((URL) getValue()).toExternalForm();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(new URL(text));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Malformed URL: " + ex.getMessage());
        }
    }

}
