package com.example.spring.simple.ioc.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.File;

/**
 * Editor for java.io.File, to directly feed a File property instead of using a String file name property.
 *
 * @author Juergen Hoeller
 * @see File
 * @since 09.12.2003
 */
public class FileEditor extends PropertyEditorSupport {

    public String getAsText() {
        return ((File) getValue()).getAbsolutePath();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new File(text));
    }

}
