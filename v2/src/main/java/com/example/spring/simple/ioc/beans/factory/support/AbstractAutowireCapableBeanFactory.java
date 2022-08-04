package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.beans.BeanUtils;
import com.example.spring.simple.ioc.beans.BeanWrapper;
import com.example.spring.simple.ioc.beans.BeanWrapperImpl;
import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.MutablePropertyValues;
import com.example.spring.simple.ioc.beans.PropertyValue;
import com.example.spring.simple.ioc.beans.PropertyValues;
import com.example.spring.simple.ioc.beans.factory.BeanCreationException;
import com.example.spring.simple.ioc.beans.factory.BeanFactory;
import com.example.spring.simple.ioc.beans.factory.BeanFactoryAware;
import com.example.spring.simple.ioc.beans.factory.BeanNameAware;
import com.example.spring.simple.ioc.beans.factory.DisposableBean;
import com.example.spring.simple.ioc.beans.factory.InitializingBean;
import com.example.spring.simple.ioc.beans.factory.NoSuchBeanDefinitionException;
import com.example.spring.simple.ioc.beans.factory.UnsatisfiedDependencyException;
import com.example.spring.simple.ioc.beans.factory.config.AutowireCapableBeanFactory;
import com.example.spring.simple.ioc.beans.factory.config.BeanDefinition;
import com.example.spring.simple.ioc.beans.factory.config.BeanPostProcessor;
import com.example.spring.simple.ioc.beans.factory.config.ConstructorArgumentValues;
import com.example.spring.simple.ioc.beans.factory.config.DestructionAwareBeanPostProcessor;
import com.example.spring.simple.ioc.beans.factory.config.RuntimeBeanReference;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
    implements AutowireCapableBeanFactory {

    private static final String MANAGED_LINKED_MAP_CLASS_NAME =
        "org.springframework.beans.factory.support.ManagedLinkedMap";

    static {
        // Eagerly load the DisposableBean class to avoid weird classloader
        // issues on EJB shutdown within WebLogic 8.1's EJB container.
        // (Reported by Andreas Senft.)
        DisposableBean.class.getName();
    }

    /**
     * Set that holds all inner beans created by this factory that implement the DisposableBean interface, to be destroyed on destroySingletons.
     *
     * @see #destroySingletons
     */
    private final Set disposableInnerBeans = Collections.synchronizedSet(new HashSet());

    public AbstractAutowireCapableBeanFactory() {
    }

    public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
    }

    //---------------------------------------------------------------------
    // Implementation of AutowireCapableBeanFactory
    //---------------------------------------------------------------------

    public Object autowire(Class beanClass, int autowireMode, boolean dependencyCheck)
        throws BeansException {
        RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
            return autowireConstructor(beanClass.getName(), bd).getWrappedInstance();
        } else {
            Object bean = BeanUtils.instantiateClass(beanClass);
            populateBean(bean.getClass().getName(), bd, new BeanWrapperImpl(bean));
            return bean;
        }
    }

    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
        throws BeansException {
        if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
            throw new IllegalArgumentException("Just constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE allowed");
        }
        RootBeanDefinition bd = new RootBeanDefinition(existingBean.getClass(), autowireMode, dependencyCheck);
        populateBean(existingBean.getClass().getName(), bd, new BeanWrapperImpl(existingBean));
    }

    public Object applyBeanPostProcessorsBeforeInitialization(Object bean, String name) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking BeanPostProcessors before initialization of bean '" + name + "'");
        }
        Object result = bean;
        for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext(); ) {
            BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
            result = beanProcessor.postProcessBeforeInitialization(result, name);
            if (result == null) {
                throw new BeanCreationException("postProcessBeforeInitialization method of BeanPostProcessor [" +
                    beanProcessor + "] returned null for bean [" + result +
                    "] with name [" + name + "]");
            }
        }
        return result;
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object bean, String name) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking BeanPostProcessors after initialization of bean '" + name + "'");
        }
        Object result = bean;
        for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext(); ) {
            BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
            result = beanProcessor.postProcessAfterInitialization(result, name);
            if (result == null) {
                throw new BeanCreationException("postProcessAfter" +
                    "Initialization method of BeanPostProcessor [" +
                    beanProcessor + "] returned null for bean [" + result +
                    "] with name [" + name + "]");
            }
        }
        return result;
    }

    //---------------------------------------------------------------------
    // Implementation of superclass abstract methods
    //---------------------------------------------------------------------

    /**
     * Delegates to full createBean version with allowEagerCaching=true.
     *
     * @see #createBean(String, RootBeanDefinition, boolean).
     */
    protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition)
        throws BeansException {
        return createBean(beanName, mergedBeanDefinition, true);
    }

    /**
     * Create a bean instance for the given bean definition.
     *
     * @param beanName             name of the bean
     * @param mergedBeanDefinition the bean definition for the bean
     * @param allowEagerCaching    whether eager caching of singletons is allowed (typically true for normal beans, but false for inner beans)
     * @return a new instance of the bean
     * @throws BeansException in case of errors
     */
    protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition,
        boolean allowEagerCaching) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating instance of bean '" + beanName +
                "' with merged definition [" + mergedBeanDefinition + "]");
        }

        if (mergedBeanDefinition.getDependsOn() != null) {
            for (int i = 0; i < mergedBeanDefinition.getDependsOn().length; i++) {
                // guarantee initialization of beans that the current one depends on
                getBean(mergedBeanDefinition.getDependsOn()[i]);
            }
        }

        BeanWrapper instanceWrapper = null;
        Object bean = null;
        String errorMessage = null;
        boolean eagerlyCached = false;

        try {
            // instantiate bean
            errorMessage = "Instantiation of bean failed";

            if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
                mergedBeanDefinition.hasConstructorArgumentValues()) {
                instanceWrapper = autowireConstructor(beanName, mergedBeanDefinition);
            } else {
                instanceWrapper = new BeanWrapperImpl(mergedBeanDefinition.getBeanClass());
                initBeanWrapper(instanceWrapper);
            }
            bean = instanceWrapper.getWrappedInstance();

            // Eagerly cache singletons to be able to resolve circular references
            // even when triggered by lifecycle interfaces like BeanFactoryAware.
            if (allowEagerCaching && mergedBeanDefinition.isSingleton()) {
                addSingleton(beanName, bean);
                eagerlyCached = true;
            }

            // initialize bean
            errorMessage = "Initialization of bean failed";

            populateBean(beanName, mergedBeanDefinition, instanceWrapper);

            if (bean instanceof BeanNameAware) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking setBeanName() on BeanNameAware bean '" + beanName + "'");
                }
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            if (bean instanceof BeanFactoryAware) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking setBeanFactory() on BeanFactoryAware bean '" + beanName + "'");
                }
                ((BeanFactoryAware) bean).setBeanFactory(this);
            }

            bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
            invokeInitMethods(beanName, mergedBeanDefinition, bean);
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        } catch (BeanCreationException ex) {
            if (eagerlyCached) {
                removeSingleton(beanName);
            }
            throw ex;
        } catch (Throwable ex) {
            if (eagerlyCached) {
                removeSingleton(beanName);
            }
            throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                errorMessage, ex);
        }

        return bean;
    }

    /**
     * "autowire constructor" (with constructor arguments by type) behavior. Also applied if explicit constructor argument values are specified, matching all remaining arguments with beans from the bean factory.
     * <p>This corresponds to constructor injection: In this mode, a Spring
     * bean factory is able to host components that expect constructor-based dependency resolution.
     *
     * @param beanName             name of the bean to autowire by type
     * @param mergedBeanDefinition bean definition to update through autowiring
     * @return BeanWrapper for the new instance
     */
    protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mergedBeanDefinition)
        throws BeansException {

        ConstructorArgumentValues cargs = mergedBeanDefinition.getConstructorArgumentValues();
        ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();

        int minNrOfArgs = 0;
        if (cargs != null) {
            minNrOfArgs = cargs.getNrOfArguments();
            for (Iterator it = cargs.getIndexedArgumentValues().entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                int index = ((Integer) entry.getKey()).intValue();
                if (index < 0) {
                    throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                        "Invalid constructor argument index: " + index);
                }
                if (index > minNrOfArgs) {
                    minNrOfArgs = index + 1;
                }
                String argName = "constructor argument with index " + index;
                ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) entry.getValue();
                Object resolvedValue = resolveValueIfNecessary(beanName, mergedBeanDefinition, argName, valueHolder.getValue());
                resolvedValues.addIndexedArgumentValue(index, resolvedValue, valueHolder.getType());
            }
            for (Iterator it = cargs.getGenericArgumentValues().iterator(); it.hasNext(); ) {
                ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) it.next();
                String argName = "constructor argument";
                Object resolvedValue = resolveValueIfNecessary(beanName, mergedBeanDefinition, argName, valueHolder.getValue());
                resolvedValues.addGenericArgumentValue(resolvedValue, valueHolder.getType());
            }
        }

        Constructor[] constructors = mergedBeanDefinition.getBeanClass().getConstructors();
        Arrays.sort(constructors, new Comparator() {
            public int compare(Object o1, Object o2) {
                int c1pl = ((Constructor) o1).getParameterTypes().length;
                int c2pl = ((Constructor) o2).getParameterTypes().length;
                return (new Integer(c1pl)).compareTo(new Integer(c2pl)) * -1;
            }
        });

        BeanWrapperImpl bw = new BeanWrapperImpl();
        initBeanWrapper(bw);
        Constructor constructorToUse = null;
        Object[] argsToUse = null;
        int minTypeDiffWeight = Integer.MAX_VALUE;
        for (int i = 0; i < constructors.length; i++) {
            try {
                Constructor constructor = constructors[i];
                if (constructor.getParameterTypes().length < minNrOfArgs) {
                    throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                        minNrOfArgs + " constructor arguments specified but no matching constructor found in bean '" +
                            beanName + "' (hint: specify index arguments for simple parameters to avoid type ambiguities)");
                }
                Class[] argTypes = constructor.getParameterTypes();
                Object[] args = new Object[argTypes.length];
                for (int j = 0; j < argTypes.length; j++) {
                    ConstructorArgumentValues.ValueHolder valueHolder = resolvedValues.getArgumentValue(j, argTypes[j]);
                    if (valueHolder != null) {
                        // Synchronize if custom editors are registered.
                        // Necessary because PropertyEditors are not thread-safe.
                        if (!getCustomEditors().isEmpty()) {
                            synchronized (getCustomEditors()) {
                                args[j] = bw.doTypeConversionIfNecessary(valueHolder.getValue(), argTypes[j]);
                            }
                        } else {
                            args[j] = bw.doTypeConversionIfNecessary(valueHolder.getValue(), argTypes[j]);
                        }
                    } else {
                        if (mergedBeanDefinition.getResolvedAutowireMode() != RootBeanDefinition.AUTOWIRE_CONSTRUCTOR) {
                            throw new UnsatisfiedDependencyException(
                                mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
                                "Did you specify the correct bean references as generic constructor arguments?");
                        }
                        Map matchingBeans = findMatchingBeans(argTypes[j]);
                        if (matchingBeans == null || matchingBeans.size() != 1) {
                            throw new UnsatisfiedDependencyException(
                                mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
                                "There are " + matchingBeans.size() + " beans of type [" + argTypes[j] +
                                    "] for autowiring constructor. There should have been 1 to be able to " +
                                    "autowire constructor of bean '" + beanName + "'.");
                        }
                        args[j] = matchingBeans.values().iterator().next();
                        logger.info("Autowiring by type from bean name '" + beanName +
                            "' via constructor to bean named '" + matchingBeans.keySet().iterator().next() + "'");
                    }
                }
                int typeDiffWeight = getTypeDifferenceWeight(argTypes, args);
                if (typeDiffWeight < minTypeDiffWeight) {
                    constructorToUse = constructor;
                    argsToUse = args;
                    minTypeDiffWeight = typeDiffWeight;
                }
            } catch (BeansException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring constructor [" + constructors[i] + "] of bean '" + beanName +
                        "': could not satisfy dependencies. Detail: " + ex.getMessage());
                }
                if (i == constructors.length - 1 && constructorToUse == null) {
                    // all constructors tried
                    throw ex;
                } else {
                    // swallow and try next constructor
                }
            }
        }

        if (constructorToUse == null) {
            throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                "Could not resolve matching constructor");
        }
        bw.setWrappedInstance(BeanUtils.instantiateClass(constructorToUse, argsToUse));
        if (logger.isInfoEnabled()) {
            logger.info("Bean '" + beanName + "' instantiated via constructor [" + constructorToUse + "]");
        }
        return bw;
    }

    /**
     * Determine a weight that represents the class hierarchy difference between types and arguments. A direct match, i.e. type Integer -> arg of class Integer, does not increase the result - all direct matches means weight 0. A match between type Object and arg of class Integer would increase the
     * weight by 2, due to the superclass 2 steps up in the hierarchy (i.e. Object) being the last one that still matches the required type Object. Type Number and class Integer would increase the weight by 1 accordingly, due to the superclass 1 step up the hierarchy (i.e. Number) still matching the
     * required type Number. Therefore, with an arg of type Integer, a constructor (Integer) would be preferred to a constructor (Number) which would in turn be preferred to a constructor (Object). All argument weights get accumulated.
     *
     * @param argTypes the argument types to match
     * @param args     the arguments to match
     * @return the accumulated weight for all arguments
     */
    private int getTypeDifferenceWeight(Class[] argTypes, Object[] args) {
        int result = 0;
        for (int i = 0; i < argTypes.length; i++) {
            if (!BeanUtils.isAssignable(argTypes[i], args[i])) {
                return Integer.MAX_VALUE;
            }
            if (args[i] != null) {
                Class superClass = args[i].getClass().getSuperclass();
                while (superClass != null) {
                    if (argTypes[i].isAssignableFrom(superClass)) {
                        result++;
                        superClass = superClass.getSuperclass();
                    } else {
                        superClass = null;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values from the bean definition.
     *
     * @param beanName             name of the bean
     * @param mergedBeanDefinition the bean definition for the bean
     * @param bw                   BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
        PropertyValues pvs = mergedBeanDefinition.getPropertyValues();

        if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
            mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            MutablePropertyValues mpvs = new MutablePropertyValues(pvs);

            // add property values based on autowire by name if it's applied
            if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mergedBeanDefinition, bw, mpvs);
            }

            // add property values based on autowire by type if it's applied
            if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mergedBeanDefinition, bw, mpvs);
            }

            pvs = mpvs;
        }

        dependencyCheck(beanName, mergedBeanDefinition, bw, pvs);
        applyPropertyValues(beanName, mergedBeanDefinition, bw, pvs);
    }

    /**
     * Fills in any missing property values with references to other beans in this factory if autowire is set to "byName".
     *
     * @param beanName             name of the bean we're wiring up. Useful for debugging messages; not used functionally.
     * @param mergedBeanDefinition bean definition to update through autowiring
     * @param bw                   BeanWrapper from which we can obtain information about the bean
     * @param pvs                  the PropertyValues to register wired objects with
     */
    protected void autowireByName(String beanName, RootBeanDefinition mergedBeanDefinition,
        BeanWrapper bw, MutablePropertyValues pvs) {
        String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, bw);
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if (containsBean(propertyName)) {
                Object bean = getBean(propertyName);
                pvs.addPropertyValue(propertyName, bean);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added autowiring by name from bean name '" + beanName +
                        "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                        "' by name: no matching bean found");
                }
            }
        }
    }

    /**
     * Abstract method defining "autowire by type" (bean properties by type) behavior.
     * <p>This is like PicoContainer default, in which there must be exactly one bean
     * of the property type in the bean factory. This makes bean factories simple to configure for small namespaces, but doesn't work as well as standard Spring behavior for bigger applications.
     *
     * @param beanName             name of the bean to autowire by type
     * @param mergedBeanDefinition bean definition to update through autowiring
     * @param bw                   BeanWrapper from which we can obtain information about the bean
     * @param pvs                  the PropertyValues to register wired objects with
     */
    protected void autowireByType(String beanName, RootBeanDefinition mergedBeanDefinition,
        BeanWrapper bw, MutablePropertyValues pvs) {
        String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, bw);
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            // look for a matching type
            Class requiredType = bw.getPropertyDescriptor(propertyName).getPropertyType();
            Map matchingBeans = findMatchingBeans(requiredType);
            if (matchingBeans != null && matchingBeans.size() == 1) {
                pvs.addPropertyValue(propertyName, matchingBeans.values().iterator().next());
                if (logger.isDebugEnabled()) {
                    logger.debug("Autowiring by type from bean name '" + beanName +
                        "' via property '" + propertyName + "' to bean named '" +
                        matchingBeans.keySet().iterator().next() + "'");
                }
            } else if (matchingBeans != null && matchingBeans.size() > 1) {
                throw new UnsatisfiedDependencyException(
                    mergedBeanDefinition.getResourceDescription(), beanName, propertyName,
                    "There are " + matchingBeans.size() + " beans of type [" + requiredType + "] for autowire by type. " +
                        "There should have been 1 to be able to autowire property '" + propertyName + "' of bean '" +
                        beanName + "'.");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                        "' by type: no matching bean found");
                }
            }
        }
    }

    /**
     * Perform a dependency check that all properties exposed have been set, if desired. Dependency checks can be objects (collaborating beans), simple (primitives and String), or all (both).
     *
     * @param beanName name of the bean
     */
    protected void dependencyCheck(String beanName, RootBeanDefinition mergedBeanDefinition,
        BeanWrapper bw, PropertyValues pvs) throws UnsatisfiedDependencyException {
        int dependencyCheck = mergedBeanDefinition.getDependencyCheck();
        if (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_NONE)
            return;

        Set ignoreTypes = getIgnoredDependencyTypes();
        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            if (pds[i].getWriteMethod() != null &&
                !ignoreTypes.contains(pds[i].getPropertyType()) &&
                pvs.getPropertyValue(pds[i].getName()) == null) {
                boolean isSimple = BeanUtils.isSimpleProperty(pds[i].getPropertyType());
                boolean unsatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL) ||
                    (isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
                    (!isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
                if (unsatisfied) {
                    throw new UnsatisfiedDependencyException(
                        mergedBeanDefinition.getResourceDescription(), beanName, pds[i].getName(),
                        "Set this property value or disable dependency checking for this bean.");
                }
            }
        }
    }

    /**
     * Return an array of object-type property names that are unsatisfied. These are probably unsatisfied references to other beans in the factory. Does not include simple properties like primitives or Strings.
     *
     * @return an array of object-type property names that are unsatisfied
     * @see BeanUtils#isSimpleProperty
     */
    protected String[] unsatisfiedObjectProperties(RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
        Set result = new TreeSet();
        Set ignoreTypes = getIgnoredDependencyTypes();
        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            String name = pds[i].getName();
            if (pds[i].getWriteMethod() != null &&
                !BeanUtils.isSimpleProperty(pds[i].getPropertyType()) &&
                !ignoreTypes.contains(pds[i].getPropertyType()) &&
                mergedBeanDefinition.getPropertyValues().getPropertyValue(name) == null) {
                result.add(name);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * Apply the given property values, resolving any runtime references to other beans in this bean factory. Must use deep copy, so we don't permanently modify this property
     *
     * @param beanName bean name passed for better exception information
     * @param bw       BeanWrapper wrapping the target object
     * @param pvs      new property values
     */
    protected void applyPropertyValues(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw,
        PropertyValues pvs) throws BeansException {
        if (pvs == null) {
            return;
        }
        MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
        PropertyValue[] pvals = deepCopy.getPropertyValues();
        for (int i = 0; i < pvals.length; i++) {
            Object value = resolveValueIfNecessary(beanName, mergedBeanDefinition,
                pvals[i].getName(), pvals[i].getValue());
            PropertyValue pv = new PropertyValue(pvals[i].getName(), value);
            // update mutable copy
            deepCopy.setPropertyValueAt(pv, i);
        }
        // set our (possibly massaged) deepCopy
        try {
            // synchronize if custom editors are registered
            // necessary because PropertyEditors are not thread-safe
            if (!getCustomEditors().isEmpty()) {
                synchronized (this) {
                    bw.setPropertyValues(deepCopy);
                }
            } else {
                bw.setPropertyValues(deepCopy);
            }
        } catch (BeansException ex) {
            // improve the message by showing the context
            throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                "Error setting property values", ex);
        }
    }

    /**
     * Given a PropertyValue, return a value, resolving any references to other beans in the factory if necessary. The value could be:
     * <li>A BeanDefinition, which leads to the creation of a corresponding
     * new bean instance. Singleton flags and names of such "inner beans" are always ignored: Inner beans are anonymous prototypes.
     * <li>A RuntimeBeanReference, which must be resolved.
     * <li>A ManagedList. This is a special collection that may contain
     * RuntimeBeanReferences or Collections that will need to be resolved.
     * <li>A ManagedSet. May also contain RuntimeBeanReferences or
     * Collections that will need to be resolved.
     * <li>A ManagedMap. In this case the value may be a RuntimeBeanReference
     * or Collection that will need to be resolved.
     * <li>An ordinary object or null, in which case it's left alone.
     */
    protected Object resolveValueIfNecessary(String beanName, RootBeanDefinition mergedBeanDefinition,
        String argName, Object value) throws BeansException {
        // We must check each PropertyValue to see whether it
        // requires a runtime reference to another bean to be resolved.
        // If it does, we'll attempt to instantiate the bean and set the reference.
        if (value instanceof BeanDefinitionHolder) {
            // Resolve BeanDefinitionHolder: contains BeanDefinition with name and aliases.
            BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
            return resolveInnerBeanDefinition(beanName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
        } else if (value instanceof BeanDefinition) {
            // Resolve plain BeanDefinition, without contained name: use dummy name.
            BeanDefinition bd = (BeanDefinition) value;
            return resolveInnerBeanDefinition(beanName, "(inner bean)", bd);
        } else if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            return resolveReference(beanName, mergedBeanDefinition, argName, ref);
        } else if (value instanceof ManagedList) {
            // Convert from managed list. This is a special container that may
            // contain runtime bean references. May need to resolve references.
            return resolveManagedList(beanName, mergedBeanDefinition, argName, (List) value);
        } else if (value instanceof ManagedSet) {
            // Convert from managed set. This is a special container that may
            // contain runtime bean references. May need to resolve references.
            return resolveManagedSet(beanName, mergedBeanDefinition, argName, (Set) value);
        } else if (value instanceof ManagedMap ||
            (value != null && MANAGED_LINKED_MAP_CLASS_NAME.equals(value.getClass().getName()))) {
            // Convert from managed map. This is a special container that may
            // contain runtime bean references. May need to resolve references.
            return resolveManagedMap(beanName, mergedBeanDefinition, argName, (Map) value);
        } else {
            // no need to resolve value
            return value;
        }
    }

    /**
     * Resolve an inner bean definition.
     */
    protected Object resolveInnerBeanDefinition(String beanName, String innerBeanName, BeanDefinition bd)
        throws BeansException {
        RootBeanDefinition mbd = getMergedBeanDefinition(innerBeanName, bd);
        Object bean = createBean(innerBeanName, mbd, false);
        if (bean instanceof DisposableBean) {
            // keep reference to inner bean, to be able to destroy it on factory shutdown
            this.disposableInnerBeans.add(bean);
        }
        return getObjectForSharedInstance(innerBeanName, bean);
    }

    /**
     * Resolve a reference to another bean in the factory.
     */
    protected Object resolveReference(String beanName, RootBeanDefinition mergedBeanDefinition,
        String argName, RuntimeBeanReference ref) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving reference from property '" + argName + "' in bean '" +
                beanName + "' to bean '" + ref.getBeanName() + "'");
        }
        try {
            return getBean(ref.getBeanName());
        } catch (BeansException ex) {
            throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                "Can't resolve reference to bean '" + ref.getBeanName() +
                    "' while setting property '" + argName + "'", ex);
        }
    }

    /**
     * For each element in the ManagedList, resolve reference if necessary.
     */
    protected List resolveManagedList(String beanName, RootBeanDefinition mergedBeanDefinition,
        String argName, List ml) throws BeansException {
        List resolved = new ArrayList(ml.size());
        for (int i = 0; i < ml.size(); i++) {
            resolved.add(
                resolveValueIfNecessary(beanName, mergedBeanDefinition,
                    argName + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
                    ml.get(i)));
        }
        return resolved;
    }

    /**
     * For each element in the ManagedList, resolve reference if necessary.
     */
    protected Set resolveManagedSet(String beanName, RootBeanDefinition mergedBeanDefinition,
        String argName, Set ms) throws BeansException {
        Set resolved = new HashSet(ms.size());
        int i = 0;
        for (Iterator it = ms.iterator(); it.hasNext(); ) {
            resolved.add(
                resolveValueIfNecessary(beanName, mergedBeanDefinition,
                    argName + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
                    it.next()));
            i++;
        }
        return resolved;
    }

    /**
     * For each element in the ManagedMap, resolve reference if necessary.
     */
    protected Map resolveManagedMap(String beanName, RootBeanDefinition mergedBeanDefinition,
        String argName, Map mm) throws BeansException {
        Map resolved = new HashMap(mm.size());
        Iterator keys = mm.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            resolved.put(
                key,
                resolveValueIfNecessary(beanName, mergedBeanDefinition,
                    argName + BeanWrapper.PROPERTY_KEY_PREFIX + key + BeanWrapper.PROPERTY_KEY_SUFFIX,
                    mm.get(key)));
        }
        return resolved;
    }

    /**
     * Give a bean a chance to react now all its properties are set, and a chance to know about its owning bean factory (this object). This means checking whether the bean implements InitializingBean and/or BeanFactoryAware, and invoking the necessary callback(s) if it does.
     *
     * @param bean     new bean instance we may need to initialize
     * @param beanName the bean has in the factory. Used for debug output.
     */
    protected void invokeInitMethods(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean)
        throws Exception {

        if (bean instanceof InitializingBean) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling afterPropertiesSet() on bean with beanName '" + beanName + "'");
            }
            ((InitializingBean) bean).afterPropertiesSet();
        }

        if (mergedBeanDefinition.getInitMethodName() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling custom init method '" + mergedBeanDefinition.getInitMethodName() +
                    "' on bean with beanName '" + beanName + "'");
            }
            try {
                bean.getClass().getMethod(mergedBeanDefinition.getInitMethodName(), null).invoke(bean, null);
            } catch (InvocationTargetException ex) {
                throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                    "Initialization method '" + mergedBeanDefinition.getInitMethodName() +
                        "' threw exception", ex.getTargetException());
            } catch (Exception ex) {
                throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
                    "Invocation of initialization method '" +
                        mergedBeanDefinition.getInitMethodName() + "' failed", ex);
            }
        }
    }

    public void destroySingletons() {
        super.destroySingletons();

        synchronized (this.disposableInnerBeans) {
            for (Iterator it = this.disposableInnerBeans.iterator(); it.hasNext(); ) {
                Object bean = it.next();
                it.remove();
                destroyBean("(inner bean of type " + bean.getClass().getName() + ")", bean);
            }
        }
    }

    protected void destroyBean(String beanName, Object bean) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving depending beans for bean '" + beanName + "'");
        }
        String[] dependingBeans = getDependingBeanNames(beanName);
        if (dependingBeans != null) {
            for (int i = 0; i < dependingBeans.length; i++) {
                destroySingleton(dependingBeans[i]);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Applying DestructionAwareBeanPostProcessors to bean with name '" + beanName + "'");
        }
        for (int i = getBeanPostProcessors().size() - 1; i >= 0; i--) {
            Object beanProcessor = getBeanPostProcessors().get(i);
            if (beanProcessor instanceof DestructionAwareBeanPostProcessor) {
                ((DestructionAwareBeanPostProcessor) beanProcessor).postProcessBeforeDestruction(bean, beanName);
            }
        }

        if (bean instanceof DisposableBean) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling destroy() on bean with name '" + beanName + "'");
            }
            try {
                ((DisposableBean) bean).destroy();
            } catch (Exception ex) {
                logger.error("destroy() on bean with name '" + beanName + "' threw an exception", ex);
            }
        }

        try {
            RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
            if (bd.getDestroyMethodName() != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Calling custom destroy method '" + bd.getDestroyMethodName() +
                        "' on bean with name '" + beanName + "'");
                }
                invokeCustomDestroyMethod(beanName, bean, bd.getDestroyMethodName());
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // ignore, from manually registered singleton
        }
    }

    /**
     * Invoke the specified custom destroy method on the given bean.
     * <p>This implementation invokes a no-arg method if found, else checking
     * for a method with a single boolean argument (passing in "true", assuming a "force" parameter), else logging an error.
     * <p>Can be overridden in subclasses for custom resolution of destroy
     * methods with arguments.
     */
    protected void invokeCustomDestroyMethod(String beanName, Object bean, String destroyMethodName) {
        Method[] methods = bean.getClass().getMethods();
        Method targetMethod = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(destroyMethodName)) {
                if (targetMethod == null ||
                    methods[i].getParameterTypes().length < targetMethod.getParameterTypes().length) {
                    targetMethod = methods[i];
                }
            }
        }
        if (targetMethod == null) {
            logger.error("Couldn't find a method named '" + destroyMethodName +
                "' on bean with name '" + beanName + "'");
        } else {
            Class[] paramTypes = targetMethod.getParameterTypes();
            if (paramTypes.length > 1) {
                logger.error("Method '" + destroyMethodName + "' of bean '" + beanName +
                    "' has more than one parameter - not supported as destroy method");
            } else if (paramTypes.length == 1 && !paramTypes[0].equals(boolean.class)) {
                logger.error("Method '" + destroyMethodName + "' of bean '" + beanName +
                    "' has a non-boolean parameter - not supported as destroy method");
            } else {
                Object[] args = new Object[paramTypes.length];
                if (paramTypes.length == 1) {
                    args[0] = Boolean.TRUE;
                }
                try {
                    targetMethod.invoke(bean, args);
                } catch (InvocationTargetException ex) {
                    logger.error("Couldn't invoke destroy method '" + destroyMethodName +
                        "' of bean with name '" + beanName + "'", ex.getTargetException());
                } catch (Exception ex) {
                    logger.error("Couldn't invoke destroy method '" + destroyMethodName +
                        "' of bean with name '" + beanName + "'", ex);
                }
            }
        }
    }

    //---------------------------------------------------------------------
    // Abstract methods to be implemented by concrete subclasses
    //---------------------------------------------------------------------

    /**
     * Find bean instances that match the required type. Called by autowiring. If a subclass cannot obtain information about bean names by type, a corresponding exception should be thrown.
     *
     * @param requiredType the type of the beans to look up
     * @return a Map of bean names and bean instances that match the required type, or null if none found
     * @throws BeansException in case of errors
     * @see #autowireByType
     * @see #autowireConstructor
     */
    protected abstract Map findMatchingBeans(Class requiredType) throws BeansException;

    /**
     * Return the names of the beans that depend on the given bean. Called by destroyBean, to be able to destroy depending beans first.
     *
     * @param beanName name of the bean to find depending beans for
     * @return array of names of depending beans, or null if none
     * @throws BeansException in case of errors
     * @see #destroyBean
     */
    protected abstract String[] getDependingBeanNames(String beanName) throws BeansException;

    /**
     * Actual creation of a java.util.LinkedHashMap. In separate inner class to avoid runtime dependency on JDK 1.4.
     */
    private static abstract class LinkedHashMapCreator {

        private static Map createLinkedHashMap(int capacity) {
            return new LinkedHashMap(capacity);
        }
    }

}
