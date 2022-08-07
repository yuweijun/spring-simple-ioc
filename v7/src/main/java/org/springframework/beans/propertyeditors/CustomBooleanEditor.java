package org.springframework.beans.propertyeditors;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for Boolean properties.
 *
 * <p>This is not meant to be used as system PropertyEditor but rather as
 * locale-specific Boolean editor within custom controller code, to parse UI-caused boolean strings into Boolean properties of beans, and evaluate them in the UI form.
 *
 * <p>In web MVC code, this editor will typically be registered with
 * binder.registerCustomEditor calls in an implementation of BaseCommandController's initBinder method.
 *
 * @author Juergen Hoeller
 * @since 10.06.2003
 */
public class CustomBooleanEditor extends PropertyEditorSupport {

    public static final String VALUE_TRUE = "true";

    public static final String VALUE_FALSE = "false";

    private final boolean allowEmpty;

    /**
     * Create a new CustomBooleanEditor instance.
     * <p>The allowEmpty parameter states if an empty String should
     * be allowed for parsing, i.e. get interpreted as null value. Else, an IllegalArgumentException gets thrown in that case.
     *
     * @param allowEmpty if empty strings should be allowed
     */
    public CustomBooleanEditor(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    public String getAsText() {
        if (getValue() != null) {
            return getValue().toString();
        } else {
            return (this.allowEmpty ? "" : "null");
        }
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            setValue(null);
        } else if (text.equalsIgnoreCase(VALUE_TRUE)) {
            setValue(Boolean.TRUE);
        } else if (text.equalsIgnoreCase(VALUE_FALSE)) {
            setValue(Boolean.FALSE);
        } else
            throw new IllegalArgumentException("Invalid Boolean value [" + text + "]");
    }

}
