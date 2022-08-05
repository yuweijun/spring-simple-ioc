package com.example.spring.simple.ioc.beans.propertyeditors;

import com.example.spring.simple.ioc.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Editor for String arrays. Strings must be in CSV format.
 *
 * <p>This property editor is automatically registered by BeanWrapperImpl.
 *
 * @author Rod Johnson
 * @see StringUtils#commaDelimitedListToStringArray
 * @see com.example.spring.simple.ioc.beans.BeanWrapperImpl
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        String[] array = (String[]) this.getValue();
        return StringUtils.arrayToCommaDelimitedString(array);
    }

    public void setAsText(String s) throws IllegalArgumentException {
        String[] sa = StringUtils.commaDelimitedListToStringArray(s);
        setValue(sa);
    }

}
