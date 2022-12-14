package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

/**
 * Editor for byte arrays. Strings will simply be converted to their corresponding byte representations.
 *
 * <p>This property editor is automatically registered by BeanWrapperImpl.
 *
 * @author Juergen Hoeller
 * @since 02.04.2004
 */
public class ByteArrayPropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        byte[] value = (byte[]) getValue();
        return (value != null ? new String(value) : "null");
    }

    public void setAsText(String text) {
        setValue(text.getBytes());
    }

}
