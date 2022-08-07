package org.springframework.beans;

import java.io.PrintStream;
import java.io.PrintWriter;

public class PropertyAccessExceptionsException extends BeansException {

    private final BeanWrapper beanWrapper;

    private final PropertyAccessException[] propertyAccessExceptions;

    public PropertyAccessExceptionsException(BeanWrapper beanWrapper,
        PropertyAccessException[] propertyAccessExceptions) {
        super("");
        this.beanWrapper = beanWrapper;
        this.propertyAccessExceptions = propertyAccessExceptions;
    }

    public BeanWrapper getBeanWrapper() {
        return beanWrapper;
    }

    public Object getBindObject() {
        return this.beanWrapper.getWrappedInstance();
    }

    public int getExceptionCount() {
        return this.propertyAccessExceptions.length;
    }

    public PropertyAccessException[] getPropertyAccessExceptions() {
        return this.propertyAccessExceptions;
    }

    public PropertyAccessException getPropertyAccessException(String propertyName) {
        for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
            PropertyAccessException pae = this.propertyAccessExceptions[i];
            if (propertyName.equals(pae.getPropertyChangeEvent().getPropertyName())) {
                return pae;
            }
        }
        return null;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(this);
        sb.append("; nested propertyAccessExceptions are: ");
        for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
            PropertyAccessException pae = this.propertyAccessExceptions[i];
            sb.append("[");
            sb.append(pae.getClass().getName());
            sb.append(": ");
            sb.append(pae.getMessage());
            sb.append(']');
            if (i < this.propertyAccessExceptions.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public void printStackTrace(PrintStream ps) {
        ps.println(this);
        for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
            PropertyAccessException pae = this.propertyAccessExceptions[i];
            pae.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        pw.println(this);
        for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
            PropertyAccessException pae = this.propertyAccessExceptions[i];
            pae.printStackTrace(pw);
        }
    }

    public String toString() {
        return "PropertyAccessExceptionsException (" + getExceptionCount() + " errors)";
    }

}
