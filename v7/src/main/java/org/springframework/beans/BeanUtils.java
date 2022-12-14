package org.springframework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Static convenience methods for JavaBeans, for instantiating beans, copying bean properties, etc.
 *
 * <p>Mainly for use within the framework, but to some degree also
 * useful for application classes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: BeanUtils.java,v 1.15 2004-03-18 02:46:12 trisberg Exp $
 */
public abstract class BeanUtils {

    /**
     * Convenience method to instantiate a class using its no-arg constructor. As this method doesn't try to load classes by name, it should avoid class-loading issues.
     *
     * @param clazz class to instantiate
     * @return the new instance
     */
    public static Object instantiateClass(Class clazz) throws BeansException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException ex) {
            throw new FatalBeanException("Could not instantiate class [" + clazz.getName() +
                "]; Is it an interface or an abstract class? Does it have a no-arg constructor?", ex);
        } catch (IllegalAccessException ex) {
            throw new FatalBeanException("Could not instantiate class [" + clazz.getName() +
                "]; has class definition changed? Is there a public no-arg constructor?", ex);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new FatalBeanException("Could not instantiate class [" + clazz.getName() +
                "]; Is it an interface or an abstract class? Does it have a no-arg constructor?", e);
        }
    }

    /**
     * Convenience method to instantiate a class using the given constructor. As this method doesn't try to load classes by name, it should avoid class-loading issues.
     *
     * @param constructor constructor to instantiate
     * @return the new instance
     */
    public static Object instantiateClass(Constructor constructor, Object[] arguments) throws BeansException {
        try {
            return constructor.newInstance(arguments);
        } catch (IllegalArgumentException ex) {
            throw new FatalBeanException("Illegal arguments when trying to instantiate constructor: " + constructor, ex);
        } catch (InstantiationException ex) {
            throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() +
                "]; is it an interface or an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() +
                "]; has class definition changed? Is there a public constructor?", ex);
        } catch (InvocationTargetException ex) {
            throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() +
                "]; constructor threw exception", ex.getTargetException());
        }
    }

    /**
     * Determine if the given type is assignable from the given value, assuming setting by reflection. Considers primitive wrapper classes as assignable to the corresponding primitive types.
     * <p>For example used in a bean factory's constructor resolution.
     *
     * @param type  the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
     */
    public static boolean isAssignable(Class type, Object value) {
        return (type.isInstance(value) ||
            (!type.isPrimitive() && value == null) ||
            (type.equals(boolean.class) && value instanceof Boolean) ||
            (type.equals(byte.class) && value instanceof Byte) ||
            (type.equals(char.class) && value instanceof Character) ||
            (type.equals(short.class) && value instanceof Short) ||
            (type.equals(int.class) && value instanceof Integer) ||
            (type.equals(long.class) && value instanceof Long) ||
            (type.equals(float.class) && value instanceof Float) ||
            (type.equals(double.class) && value instanceof Double));
    }

    /**
     * Check if the given class represents a "simple" property, i.e. a primitive, a String, a Class, or a corresponding array. Used to determine properties to check for a "simple" dependency-check.
     */
    public static boolean isSimpleProperty(Class clazz) {
        return clazz.isPrimitive() || isPrimitiveArray(clazz) || isPrimitiveWrapperArray(clazz) ||
            clazz.equals(String.class) || clazz.equals(String[].class) ||
            clazz.equals(Class.class) || clazz.equals(Class[].class);
    }

    /**
     * Check if the given class represents a primitive array, i.e. boolean, byte, char, short, int, long, float, or double.
     */
    public static boolean isPrimitiveArray(Class clazz) {
        return boolean[].class.equals(clazz) || byte[].class.equals(clazz) || char[].class.equals(clazz) ||
            short[].class.equals(clazz) || int[].class.equals(clazz) || long[].class.equals(clazz) ||
            float[].class.equals(clazz) || double[].class.equals(clazz);
    }

    /**
     * Check if the given class represents an array of primitive wrappers, i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     */
    public static boolean isPrimitiveWrapperArray(Class clazz) {
        return Boolean[].class.equals(clazz) || Byte[].class.equals(clazz) || Character[].class.equals(clazz) ||
            Short[].class.equals(clazz) || Integer[].class.equals(clazz) || Long[].class.equals(clazz) ||
            Float[].class.equals(clazz) || Double[].class.equals(clazz);
    }

}
