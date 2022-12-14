package org.springframework.beans;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class BeanWrapperImpl implements BeanWrapper {

    /**
     * We'll create a lot of these objects, so we don't want a new logger every time
     */
    private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------
    /**
     * Registry for default PropertyEditors
     */
    private final Map defaultEditors;
    /**
     * The wrapped object
     */
    private Object object;
    /**
     * The nested path of the object
     */
    private String nestedPath = "";
    /**
     * Map with custom PropertyEditor instances
     */
    private Map customEditors;

    /**
     * Cached introspections results for this object, to prevent encountering the cost of JavaBeans introspection every time.
     */
    private CachedIntrospectionResults cachedIntrospectionResults;

    /* Map with cached nested BeanWrappers */
    private Map nestedBeanWrappers;

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------

    /**
     * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
     *
     * @see #setWrappedInstance
     */
    public BeanWrapperImpl() {
        // Register default editors in this class, for restricted environments.
        // We're not using the JRE's PropertyEditorManager to avoid potential
        // SecurityExceptions when running in a SecurityManager.
        this.defaultEditors = new HashMap(16);

        // Simple editors, without parameterization capabilities.
        this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
        this.defaultEditors.put(Class.class, new ClassEditor());
        this.defaultEditors.put(File.class, new FileEditor());
        this.defaultEditors.put(InputStream.class, new InputStreamEditor());
        this.defaultEditors.put(Locale.class, new LocaleEditor());
        this.defaultEditors.put(Properties.class, new PropertiesEditor());
        this.defaultEditors.put(String[].class, new StringArrayPropertyEditor());
        this.defaultEditors.put(URL.class, new URLEditor());

        // Default instances of parameterizable editors.
        // Can be overridden by registering custom instances of those as custom editors.
        this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(false));
        this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, false));
        this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, false));
        this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, false));
        this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, false));
        this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, false));
        this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, false));
        this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, false));
    }

    /**
     * Create new BeanWrapperImpl for the given object.
     *
     * @param object object wrapped by this BeanWrapper
     */
    public BeanWrapperImpl(Object object) {
        this();
        setWrappedInstance(object);
    }

    /**
     * Create new BeanWrapperImpl, wrapping a new instance of the specified class.
     *
     * @param clazz class to instantiate and wrap
     */
    public BeanWrapperImpl(Class clazz) {
        this();
        setWrappedInstance(BeanUtils.instantiateClass(clazz));
    }

    /**
     * Create new BeanWrapperImpl for the given object, registering a nested path that the object is in.
     *
     * @param object     object wrapped by this BeanWrapper.
     * @param nestedPath the nested path of the object
     */
    public BeanWrapperImpl(Object object, String nestedPath) {
        this();
        setWrappedInstance(object, nestedPath);
    }

    /**
     * Create new BeanWrapperImpl for the given object, registering a nested path that the object is in.
     *
     * @param object     object wrapped by this BeanWrapper.
     * @param nestedPath the nested path of the object
     * @param superBw    the containing BeanWrapper (must not be null)
     */
    private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl superBw) {
        this.defaultEditors = superBw.defaultEditors;
        setWrappedInstance(object, nestedPath);
    }

    //---------------------------------------------------------------------
    // Implementation of BeanWrapper
    //---------------------------------------------------------------------

    /**
     * Switch the target object, replacing the cached introspection results only if the class of the new object is different to that of the replaced object.
     *
     * @param object     new target
     * @param nestedPath the nested path of the object
     */
    public void setWrappedInstance(Object object, String nestedPath) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot set BeanWrapperImpl target to a null object");
        }
        this.object = object;
        this.nestedPath = nestedPath;
        this.nestedBeanWrappers = null;
        if (this.cachedIntrospectionResults == null ||
            !this.cachedIntrospectionResults.getBeanClass().equals(object.getClass())) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(object.getClass());
        }
    }

    public Object getWrappedInstance() {
        return this.object;
    }

    /**
     * Switch the target object, replacing the cached introspection results only if the class of the new object is different to that of the replaced object.
     *
     * @param object new target
     */
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "");
    }

    public Class getWrappedClass() {
        return this.object.getClass();
    }

    public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
        registerCustomEditor(requiredType, null, propertyEditor);
    }

    public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
        if (propertyPath != null) {
            List bws = getBeanWrappersForPropertyPath(propertyPath);
            for (Iterator it = bws.iterator(); it.hasNext(); ) {
                BeanWrapperImpl bw = (BeanWrapperImpl) it.next();
                bw.doRegisterCustomEditor(requiredType, getFinalPath(bw, propertyPath), propertyEditor);
            }
        } else {
            doRegisterCustomEditor(requiredType, propertyPath, propertyEditor);
        }
    }

    private void doRegisterCustomEditor(Class requiredType, String propertyName, PropertyEditor propertyEditor) {
        if (this.customEditors == null) {
            this.customEditors = new HashMap();
        }
        if (propertyName != null) {
            this.customEditors.put(propertyName, propertyEditor);
        } else {
            if (requiredType == null) {
                throw new IllegalArgumentException("No propertyName and no requiredType specified");
            }
            this.customEditors.put(requiredType, propertyEditor);
        }
    }

    public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
        if (propertyPath != null) {
            BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyPath);
            return nestedBw.doFindCustomEditor(requiredType, getFinalPath(nestedBw, propertyPath));
        } else {
            return doFindCustomEditor(requiredType, propertyPath);
        }
    }

    private PropertyEditor doFindCustomEditor(Class requiredType, String propertyName) {
        if (this.customEditors == null) {
            return null;
        }
        if (propertyName != null) {
            // check property-specific editor first
            try {
                PropertyEditor editor = (PropertyEditor) this.customEditors.get(propertyName);
                if (editor == null) {
                    int keyIndex = propertyName.indexOf('[');
                    if (keyIndex != -1) {
                        editor = (PropertyEditor) this.customEditors.get(propertyName.substring(0, keyIndex));
                    }
                }
                if (editor != null) {
                    return editor;
                } else {
                    if (requiredType == null) {
                        // try property type
                        requiredType = getPropertyDescriptor(propertyName).getPropertyType();
                    }
                }
            } catch (InvalidPropertyException ex) {
                // probably an indexed or mapped property
                // we need to retrieve the value to determine the type
                Object value = getPropertyValue(propertyName);
                if (value == null) {
                    return null;
                }
                requiredType = value.getClass();
            }
        }
        // no property-specific editor -> check type-specific editor
        return (PropertyEditor) this.customEditors.get(requiredType);
    }

    /**
     * Get the last component of the path. Also works if not nested.
     *
     * @param bw         BeanWrapper to work on
     * @param nestedPath property path we know is nested
     * @return last component of the path (the property on the target bean)
     */
    private String getFinalPath(BeanWrapper bw, String nestedPath) {
        if (bw == this) {
            return nestedPath;
        }
        return nestedPath.substring(nestedPath.lastIndexOf(NESTED_PROPERTY_SEPARATOR) + 1);
    }

    /**
     * Recursively navigate to return a BeanWrapper for the nested property path.
     *
     * @param propertyPath property property path, which may be nested
     * @return a BeanWrapper for the target bean
     */
    private BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) throws BeansException {
        int pos = propertyPath.indexOf(NESTED_PROPERTY_SEPARATOR);
        // handle nested properties recursively
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
            return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
        } else {
            return this;
        }
    }

    /**
     * Recursively navigate to return a BeanWrapper for the nested property path. In case of an indexed or mapped property, all BeanWrappers that apply will be returned.
     *
     * @param propertyPath property property path, which may be nested
     * @return a BeanWrapper for the target bean
     */
    private List getBeanWrappersForPropertyPath(String propertyPath) throws BeansException {
        List beanWrappers = new ArrayList();
        int pos = propertyPath.indexOf(NESTED_PROPERTY_SEPARATOR);
        // handle nested properties recursively
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            if (nestedProperty.indexOf('[') == -1) {
                Class propertyType = getPropertyDescriptor(nestedProperty).getPropertyType();
                if (propertyType.isArray()) {
                    Object[] array = (Object[]) getPropertyValue(nestedProperty);
                    for (int i = 0; i < array.length; i++) {
                        beanWrappers.addAll(
                            getBeanWrappersForNestedProperty(nestedProperty + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
                                nestedPath));
                    }
                    return beanWrappers;
                } else if (List.class.isAssignableFrom(propertyType)) {
                    List list = (List) getPropertyValue(nestedProperty);
                    for (int i = 0; i < list.size(); i++) {
                        beanWrappers.addAll(
                            getBeanWrappersForNestedProperty(nestedProperty + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
                                nestedPath));
                    }
                    return beanWrappers;
                } else if (Map.class.isAssignableFrom(propertyType)) {
                    Map map = (Map) getPropertyValue(nestedProperty);
                    for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
                        beanWrappers.addAll(
                            getBeanWrappersForNestedProperty(nestedProperty + PROPERTY_KEY_PREFIX + it.next() + PROPERTY_KEY_SUFFIX,
                                nestedPath));
                    }
                    return beanWrappers;
                }
            }
            beanWrappers.addAll(getBeanWrappersForNestedProperty(nestedProperty, nestedPath));
            return beanWrappers;
        } else {
            beanWrappers.add(this);
            return beanWrappers;
        }
    }

    private List getBeanWrappersForNestedProperty(String nestedProperty, String nestedPath) throws BeansException {
        BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
        return nestedBw.getBeanWrappersForPropertyPath(nestedPath);
    }

    /**
     * Retrieve a BeanWrapper for the given nested property. Create a new one if not found in the cache.
     * <p>Note: Caching nested BeanWrappers is necessary now,
     * to keep registered custom editors for nested properties.
     *
     * @param nestedProperty property to create the BeanWrapper for
     * @return the BeanWrapper instance, either cached or newly created
     */
    private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) throws BeansException {
        if (this.nestedBeanWrappers == null) {
            this.nestedBeanWrappers = new HashMap();
        }
        // get value of bean property
        String[] tokens = getPropertyNameTokens(nestedProperty);
        Object propertyValue = getPropertyValue(tokens[0], tokens[1], tokens[2]);
        String canonicalName = tokens[0];
        if (propertyValue == null) {
            throw new NullValueInNestedPathException(getWrappedClass(), this.nestedPath + canonicalName);
        }

        // lookup cached sub-BeanWrapper, create new one if not found
        BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(canonicalName);
        if (nestedBw == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
            }
            nestedBw = new BeanWrapperImpl(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR,
                this);
            // inherit all type-specific PropertyEditors
            if (this.customEditors != null) {
                for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext(); ) {
                    Object key = it.next();
                    if (key instanceof Class) {
                        Class requiredType = (Class) key;
                        PropertyEditor propertyEditor = (PropertyEditor) this.customEditors.get(key);
                        nestedBw.registerCustomEditor(requiredType, propertyEditor);
                    }
                }
            }
            this.nestedBeanWrappers.put(canonicalName, nestedBw);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
            }
        }
        return nestedBw;
    }

    private String[] getPropertyNameTokens(String propertyName) {
        String actualName = propertyName;
        String key = null;
        int keyStart = propertyName.indexOf('[');
        if (keyStart != -1 && propertyName.endsWith("]")) {
            actualName = propertyName.substring(0, keyStart);
            key = propertyName.substring(keyStart + 1, propertyName.length() - 1);
            if (key.startsWith("'") && key.endsWith("'")) {
                key = key.substring(1, key.length() - 1);
            } else if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
        }
        String canonicalName = actualName;
        if (key != null) {
            canonicalName += PROPERTY_KEY_PREFIX + key + PROPERTY_KEY_SUFFIX;
        }
        return new String[]{canonicalName, actualName, key};
    }

    public Object getPropertyValue(String propertyName) throws BeansException {
        BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
        String[] tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        return nestedBw.getPropertyValue(tokens[0], tokens[1], tokens[2]);
    }

    private Object getPropertyValue(String propertyName, String actualName, String key) throws BeansException {
        PropertyDescriptor pd = getPropertyDescriptorInternal(actualName);
        if (pd == null || pd.getReadMethod() == null) {
            throw new NotReadablePropertyException(getWrappedClass(), this.nestedPath + propertyName);
        }
        if (logger.isDebugEnabled())
            logger.debug("About to invoke read method [" + pd.getReadMethod() +
                "] on object of class [" + this.object.getClass().getName() + "]");
        try {
            Object value = pd.getReadMethod().invoke(this.object, null);
            if (key != null) {
                if (value == null) {
                    throw new NullValueInNestedPathException(getWrappedClass(), this.nestedPath + propertyName,
                        "Cannot access indexed value of property referenced in indexed " +
                            "property path '" + propertyName + "': returned null");
                } else if (value.getClass().isArray()) {
                    return Array.get(value, Integer.parseInt(key));
                } else if (value instanceof List) {
                    List list = (List) value;
                    return list.get(Integer.parseInt(key));
                } else if (value instanceof Set) {
                    // apply index to Iterator in case of a Set
                    Set set = (Set) value;
                    int index = Integer.parseInt(key);
                    Iterator it = set.iterator();
                    for (int i = 0; it.hasNext(); i++) {
                        Object elem = it.next();
                        if (i == index) {
                            return elem;
                        }
                    }
                    throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                        "Cannot get element with index " + index + " from Set of size " +
                            set.size() + ", accessed using property path '" + propertyName + "'");
                } else if (value instanceof Map) {
                    Map map = (Map) value;
                    return map.get(key);
                } else {
                    throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                        "Property referenced in indexed property path '" + propertyName +
                            "' is neither an array nor a List nor a Map; returned value was [" +
                            value + "]");
                }
            } else {
                return value;
            }
        } catch (InvocationTargetException ex) {
            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "Getter for property '" + actualName + "' threw exception", ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "Illegal attempt to get property '" + actualName + "' threw exception", ex);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "Index of out of bounds in property path '" + propertyName + "'", ex);
        } catch (NumberFormatException ex) {
            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "Invalid index in property path '" + propertyName + "'", ex);
        }
    }

    public void setPropertyValue(String propertyName, Object value) throws BeansException {
        BeanWrapperImpl nestedBw = null;
        try {
            nestedBw = getBeanWrapperForPropertyPath(propertyName);
        } catch (NotReadablePropertyException ex) {
            throw new NotWritablePropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "Nested property in path '" + propertyName + "' does not exist",
                ex);
        }
        String[] tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        nestedBw.setPropertyValue(tokens[0], tokens[1], tokens[2], value);
    }

    private void setPropertyValue(String propertyName, String actualName, String key, Object value)
        throws BeansException {
        if (key != null) {
            Object propValue = null;
            try {
                propValue = getPropertyValue(actualName);
            } catch (NotReadablePropertyException ex) {
                throw new NotWritablePropertyException(getWrappedClass(), this.nestedPath + propertyName,
                    "Cannot access indexed value in property referenced " +
                        "in indexed property path '" + propertyName + "'",
                    ex);
            }
            if (propValue == null) {
                throw new NullValueInNestedPathException(getWrappedClass(), this.nestedPath + propertyName,
                    "Cannot access indexed value in property referenced " +
                        "in indexed property path '" + propertyName + "': returned null");
            } else if (propValue.getClass().isArray()) {
                Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value,
                    propValue.getClass().getComponentType());
                Array.set(propValue, Integer.parseInt(key), newValue);
            } else if (propValue instanceof List) {
                Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, null);
                List list = (List) propValue;
                int index = Integer.parseInt(key);
                if (index < list.size()) {
                    list.set(index, newValue);
                } else if (index >= list.size()) {
                    for (int i = list.size(); i < index; i++) {
                        try {
                            list.add(null);
                        } catch (NullPointerException ex) {
                            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                                "Cannot set element with index " + index + " in List of size " +
                                    list.size() + ", accessed using property path '" + propertyName +
                                    "': List does not support filling up gaps with null elements");
                        }
                    }
                    list.add(newValue);
                }
            } else if (propValue instanceof Map) {
                Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, null);
                Map map = (Map) propValue;
                map.put(key, newValue);
            } else {
                throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                    "Property referenced in indexed property path '" + propertyName +
                        "' is neither an array nor a List nor a Map; returned value was [" +
                        value + "]");
            }
        } else {
            if (!isWritableProperty(propertyName)) {
                throw new NotWritablePropertyException(getWrappedClass(), this.nestedPath + propertyName);
            }
            PropertyDescriptor pd = getPropertyDescriptor(propertyName);
            Method writeMethod = pd.getWriteMethod();
            Object newValue = null;
            try {
                // old value may still be null
                newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, pd.getPropertyType());

                if (pd.getPropertyType().isPrimitive() &&
                    (newValue == null || "".equals(newValue))) {
                    throw new IllegalArgumentException("Invalid value [" + value + "] for property '" +
                        pd.getName() + "' of primitive type [" + pd.getPropertyType() + "]");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("About to invoke write method [" + writeMethod +
                        "] on object of class [" + object.getClass().getName() + "]");
                }
                writeMethod.invoke(this.object, newValue);
                if (logger.isDebugEnabled()) {
                    String msg = "Invoked write method [" + writeMethod + "] with value ";
                    // only cause toString invocation of new value in case of simple property
                    if (newValue == null || BeanUtils.isSimpleProperty(pd.getPropertyType())) {
                        logger.debug(msg + PROPERTY_KEY_PREFIX + newValue + PROPERTY_KEY_SUFFIX);
                    } else {
                        logger.debug(msg + "of type [" + pd.getPropertyType().getName() + "]");
                    }
                }
            } catch (InvocationTargetException ex) {
                PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.object, this.nestedPath + propertyName,
                    null, newValue);
                if (ex.getTargetException() instanceof ClassCastException) {
                    throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
                } else {
                    throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
                }
            } catch (IllegalArgumentException ex) {
                PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.object, this.nestedPath + propertyName,
                    null, newValue);
                throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
            } catch (IllegalAccessException ex) {
                PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.object, this.nestedPath + propertyName,
                    null, newValue);
                throw new MethodInvocationException(propertyChangeEvent, ex);
            }
        }
    }

    public void setPropertyValue(PropertyValue pv) throws BeansException {
        setPropertyValue(pv.getName(), pv.getValue());
    }

    /**
     * Bulk update from a Map. Bulk updates from PropertyValues are more powerful: this method is provided for convenience.
     *
     * @param map map containing properties to set, as name-value pairs. The map may include nested properties.
     * @throws BeansException if there's a fatal, low-level exception
     */
    public void setPropertyValues(Map map) throws BeansException {
        setPropertyValues(new MutablePropertyValues(map));
    }

    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        setPropertyValues(pvs, false);
    }

    public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown) throws BeansException {
        List propertyAccessExceptions = new ArrayList();
        PropertyValue[] pvs = propertyValues.getPropertyValues();
        for (int i = 0; i < pvs.length; i++) {
            try {
                // This method may throw any BeansException, which won't be caught
                // here, if there is a critical failure such as no matching field.
                // We can attempt to deal only with less serious exceptions.
                setPropertyValue(pvs[i]);
            } catch (NotWritablePropertyException ex) {
                if (!ignoreUnknown) {
                    throw ex;
                }
                // otherwise, just ignore it and continue...
            } catch (TypeMismatchException ex) {
                propertyAccessExceptions.add(ex);
            } catch (MethodInvocationException ex) {
                propertyAccessExceptions.add(ex);
            }
        }

        // If we encountered individual exceptions, throw the composite exception.
        if (!propertyAccessExceptions.isEmpty()) {
            Object[] paeArray = propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
            throw new PropertyAccessExceptionsException(this, (PropertyAccessException[]) paeArray);
        }
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName, Object oldValue, Object newValue)
        throws BeansException {
        return new PropertyChangeEvent((this.object != null ? this.object : "constructor"),
            (propertyName != null ? this.nestedPath + propertyName : null),
            oldValue, newValue);
    }

    /**
     * Convert the value to the required type (if necessary from a String). Conversions from String to any type use the setAsText() method of the PropertyEditor class. Note that a PropertyEditor must be registered for this class for this to work. This is a standard Java Beans API. A number of
     * property editors are automatically registered by this class.
     *
     * @param newValue     proposed change value.
     * @param requiredType type we must convert to
     * @return new value, possibly the result of type convertion
     * @throws BeansException if there is an internal error
     */
    public Object doTypeConversionIfNecessary(Object newValue, Class requiredType) throws BeansException {
        return doTypeConversionIfNecessary(null, null, null, newValue, requiredType);
    }

    /**
     * Convert the value to the required type (if necessary from a String), for the specified property.
     *
     * @param propertyName name of the property
     * @param oldValue     previous value, if available (may be null)
     * @param newValue     proposed change value.
     * @param requiredType type we must convert to
     * @return new value, possibly the result of type convertion
     * @throws BeansException if there is an internal error
     */
    protected Object doTypeConversionIfNecessary(String propertyName, String fullPropertyName,
        Object oldValue, Object newValue,
        Class requiredType) throws BeansException {
        if (newValue != null) {

            if (requiredType != null && requiredType.isArray()) {
                // convert individual elements to array elements
                Class componentType = requiredType.getComponentType();
                if (newValue instanceof List) {
                    List list = (List) newValue;
                    Object result = Array.newInstance(componentType, list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Object value = doTypeConversionIfNecessary(propertyName,
                            propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
                            null, list.get(i), componentType);
                        Array.set(result, i, value);
                    }
                    return result;
                } else if (newValue instanceof Object[]) {
                    Object[] array = (Object[]) newValue;
                    Object result = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Object value = doTypeConversionIfNecessary(propertyName,
                            propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
                            null, array[i], componentType);
                        Array.set(result, i, value);
                    }
                    return result;
                }
            }

            // custom editor for this type?
            PropertyEditor pe = findCustomEditor(requiredType, fullPropertyName);

            // value not of required type?
            if (pe != null || (requiredType != null && !requiredType.isAssignableFrom(newValue.getClass()))) {

                if (pe == null && requiredType != null) {
                    // no custom editor -> check BeanWrapperImpl's default editors
                    pe = (PropertyEditor) this.defaultEditors.get(requiredType);
                    if (pe == null) {
                        // no BeanWrapper default editor -> check standard JavaBean editors
                        pe = PropertyEditorManager.findEditor(requiredType);
                    }
                }

                if (newValue instanceof String[]) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Converting String array to comma-delimited String [" + newValue + "]");
                    }
                    newValue = StringUtils.arrayToCommaDelimitedString((String[]) newValue);
                }

                if (newValue instanceof String) {
                    if (pe != null) {
                        // use PropertyEditor's setAsText in case of a String value
                        if (logger.isDebugEnabled()) {
                            logger.debug("Converting String to [" + requiredType + "] using property editor [" + pe + "]");
                        }
                        try {
                            pe.setAsText((String) newValue);
                            newValue = pe.getValue();
                        } catch (IllegalArgumentException ex) {
                            throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue),
                                requiredType, ex);
                        }
                    } else {
                        throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue),
                            requiredType);
                    }
                } else if (pe != null) {
                    // Not a String -> use PropertyEditor's setValue.
                    // With standard PropertyEditors, this will return the very same object;
                    // we just want to allow special PropertyEditors to override setValue
                    // for type conversion from non-String values to the required type.
                    try {
                        pe.setValue(newValue);
                        newValue = pe.getValue();
                    } catch (IllegalArgumentException ex) {
                        throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue),
                            requiredType, ex);
                    }
                }
            }

            if (requiredType != null && requiredType.isArray() && !newValue.getClass().isArray()) {
                Class componentType = requiredType.getComponentType();
                Object result = Array.newInstance(componentType, 1);
                Object val = doTypeConversionIfNecessary(propertyName, propertyName + "[0]",
                    null, newValue, componentType);
                Array.set(result, 0, val);
                return result;
            }
        }

        return newValue;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
    }

    public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find property descriptor for null property");
        }
        PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
        if (pd != null) {
            return pd;
        } else {
            throw new InvalidPropertyException(getWrappedClass(), this.nestedPath + propertyName,
                "No property '" + propertyName + "' found");
        }
    }

    /**
     * Internal version of getPropertyDescriptor: Returns null if not found rather than throwing an exception.
     */
    protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
        BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
        return nestedBw.cachedIntrospectionResults.getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
    }

    public Class getPropertyType(String propertyName) throws BeansException {
        Class type = null;
        try {
            type = getPropertyDescriptor(propertyName).getPropertyType();
        } catch (InvalidPropertyException ex) {
            // probably an indexed or mapped element
            Object value = getPropertyValue(propertyName);
            if (value != null) {
                type = value.getClass();
            }
        }
        return type;
    }

    public boolean isReadableProperty(String propertyName) {
        // This is a programming error, although asking for a property
        // that doesn't exist is not.
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find readability status for null property");
        }
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            return (pd != null && pd.getReadMethod() != null);
        } catch (InvalidPropertyException ex) {
            // doesn't exist, so can't be readable
            return false;
        }
    }

    public boolean isWritableProperty(String propertyName) {
        // This is a programming error, although asking for a property
        // that doesn't exist is not.
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find writability status for null property");
        }
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            return (pd != null && pd.getWriteMethod() != null);
        } catch (InvalidPropertyException ex) {
            // doesn't exist, so can't be writable
            return false;
        }
    }

    //---------------------------------------------------------------------
    // Diagnostics
    //---------------------------------------------------------------------

    /**
     * This method is expensive! Only call for diagnostics and debugging reasons, not in production.
     *
     * @return a string describing the state of this object
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("BeanWrapperImpl:"
                + " wrapping class [" + getWrappedInstance().getClass().getName() + "]; ");
            PropertyDescriptor[] pds = getPropertyDescriptors();
            if (pds != null) {
                for (int i = 0; i < pds.length; i++) {
                    Object val = getPropertyValue(pds[i].getName());
                    String valStr = (val != null) ? val.toString() : "null";
                    sb.append(pds[i].getName() + "={" + valStr + "}");
                }
            }
        } catch (Exception ex) {
            sb.append("exception encountered: " + ex);
        }
        return sb.toString();
    }

}
