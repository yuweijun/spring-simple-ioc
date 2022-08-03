package org.springframework.context.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

    private final DefaultListableBeanFactory beanFactory;

    @Nullable
    private ResourceLoader resourceLoader;

    private boolean customClassLoader = false;

    private final AtomicBoolean refreshed = new AtomicBoolean();


    /**
     * Create a new GenericApplicationContext.
     * @see #registerBeanDefinition
     * @see #refresh
     */
    public GenericApplicationContext() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    /**
     * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
     * @param beanFactory the DefaultListableBeanFactory instance to use for this context
     * @see #registerBeanDefinition
     * @see #refresh
     */
    public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
    }

    /**
     * Create a new GenericApplicationContext with the given parent.
     * @param parent the parent application context
     * @see #registerBeanDefinition
     * @see #refresh
     */
    public GenericApplicationContext(@Nullable ApplicationContext parent) {
        this();
        setParent(parent);
    }

    /**
     * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
     * @param beanFactory the DefaultListableBeanFactory instance to use for this context
     * @param parent the parent application context
     * @see #registerBeanDefinition
     * @see #refresh
     */
    public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
        this(beanFactory);
        setParent(parent);
    }


    /**
     * Set the parent of this application context, also setting
     * the parent of the internal BeanFactory accordingly.
     * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
     */
    @Override
    public void setParent(@Nullable ApplicationContext parent) {
        super.setParent(parent);
        this.beanFactory.setParentBeanFactory(getInternalParentBeanFactory());
    }

    /**
     * Set whether it should be allowed to override bean definitions by registering
     * a different definition with the same name, automatically replacing the former.
     * If not, an exception will be thrown. Default is "true".
     * @since 3.0
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
     */
    public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
        this.beanFactory.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
    }

    /**
     * Set a ResourceLoader to use for this context. If set, the context will
     * delegate all {@code getResource} calls to the given ResourceLoader.
     * If not set, default resource loading will apply.
     * <p>The main reason to specify a custom ResourceLoader is to resolve
     * resource paths (without URL prefix) in a specific fashion.
     * The default behavior is to resolve such paths as class path locations.
     * To resolve resource paths as file system locations, specify a
     * FileSystemResourceLoader here.
     * <p>You can also pass in a full ResourcePatternResolver, which will
     * be autodetected by the context and used for {@code getResources}
     * calls as well. Else, default resource pattern matching will apply.
     * @see #getResource
     * @see org.springframework.core.io.DefaultResourceLoader
     * @see org.springframework.core.io.support.ResourcePatternResolver
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    //---------------------------------------------------------------------
    // ResourceLoader / ResourcePatternResolver override if necessary
    //---------------------------------------------------------------------

    /**
     * This implementation delegates to this context's ResourceLoader if set,
     * falling back to the default superclass behavior else.
     * @see #setResourceLoader
     */
    @Override
    public Resource getResource(String location) {
        if (this.resourceLoader != null) {
            return this.resourceLoader.getResource(location);
        }
        return super.getResource(location);
    }

    //---------------------------------------------------------------------
    // Implementations of AbstractApplicationContext's template methods
    //---------------------------------------------------------------------

    /**
     * Do nothing: We hold a single internal BeanFactory and rely on callers
     * to register beans through our public methods (or the BeanFactory's).
     * @see #registerBeanDefinition
     */
    @Override
    protected final void refreshBeanFactory() throws IllegalStateException {
        if (!this.refreshed.compareAndSet(false, true)) {
            throw new IllegalStateException(
                "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
        }
    }

    /**
     * Return the single internal BeanFactory held by this context
     * (as ConfigurableListableBeanFactory).
     */
    @Override
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * Return the underlying bean factory of this context,
     * available for registering bean definitions.
     * <p><b>NOTE:</b> You need to call {@link #refresh()} to initialize the
     * bean factory and its contained beans with application context semantics
     * (autodetecting BeanFactoryPostProcessors, etc).
     * @return the internal bean factory (as DefaultListableBeanFactory)
     */
    public final DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return this.beanFactory;
    }

    //---------------------------------------------------------------------
    // Implementation of BeanDefinitionRegistry
    //---------------------------------------------------------------------

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
        throws BeanDefinitionStoreException {

        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return this.beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public void registerAlias(String beanName, String alias) {
        this.beanFactory.registerAlias(beanName, alias);
    }

    //---------------------------------------------------------------------
    // Convenient methods for registering individual beans
    //---------------------------------------------------------------------

    /**
     * Register a bean from the given bean class, optionally providing explicit
     * constructor arguments for consideration in the autowiring process.
     * @param beanClass the class of the bean
     * @param constructorArgs custom argument values to be fed into Spring's
     * constructor resolution algorithm, resolving either all arguments or just
     * specific ones, with the rest to be resolved through regular autowiring
     * (may be {@code null} or empty)
     * @since 5.2 (since 5.0 on the AnnotationConfigApplicationContext subclass)
     */
    public <T> void registerBean(Class<T> beanClass, Object... constructorArgs) {
        registerBean(null, beanClass, constructorArgs);
    }

    /**
     * Register a bean from the given bean class, optionally providing explicit
     * constructor arguments for consideration in the autowiring process.
     * @param beanName the name of the bean (may be {@code null})
     * @param beanClass the class of the bean
     * @param constructorArgs custom argument values to be fed into Spring's
     * constructor resolution algorithm, resolving either all arguments or just
     * specific ones, with the rest to be resolved through regular autowiring
     * (may be {@code null} or empty)
     * @since 5.2 (since 5.0 on the AnnotationConfigApplicationContext subclass)
     */
    public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, Object... constructorArgs) {
        registerBean(beanName, beanClass, (Supplier<T>) null,
            bd -> {
                for (Object arg : constructorArgs) {
                    bd.getConstructorArgumentValues().addGenericArgumentValue(arg);
                }
            });
    }

    /**
     * Register a bean from the given bean class, optionally customizing its
     * bean definition metadata (typically declared as a lambda expression).
     * @param beanClass the class of the bean (resolving a public constructor
     * to be autowired, possibly simply the default constructor)
     * @param customizers one or more callbacks for customizing the factory's
     * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
     * @since 5.0
     * @see #registerBean(String, Class, Supplier, BeanDefinitionCustomizer...)
     */
    public final <T> void registerBean(Class<T> beanClass, BeanDefinitionCustomizer... customizers) {
        registerBean(null, beanClass, null, customizers);
    }

    /**
     * Register a bean from the given bean class, optionally customizing its
     * bean definition metadata (typically declared as a lambda expression).
     * @param beanName the name of the bean (may be {@code null})
     * @param beanClass the class of the bean (resolving a public constructor
     * to be autowired, possibly simply the default constructor)
     * @param customizers one or more callbacks for customizing the factory's
     * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
     * @since 5.0
     * @see #registerBean(String, Class, Supplier, BeanDefinitionCustomizer...)
     */
    public final <T> void registerBean(
        @Nullable String beanName, Class<T> beanClass, BeanDefinitionCustomizer... customizers) {

        registerBean(beanName, beanClass, null, customizers);
    }

    /**
     * Register a bean from the given bean class, using the given supplier for
     * obtaining a new instance (typically declared as a lambda expression or
     * method reference), optionally customizing its bean definition metadata
     * (again typically declared as a lambda expression).
     * @param beanClass the class of the bean
     * @param supplier a callback for creating an instance of the bean
     * @param customizers one or more callbacks for customizing the factory's
     * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
     * @since 5.0
     */
    public final <T> void registerBean(
        Class<T> beanClass, Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

        registerBean(null, beanClass, supplier, customizers);
    }

    /**
     * Register a bean from the given bean class, using the given supplier for
     * obtaining a new instance (typically declared as a lambda expression or
     * method reference), optionally customizing its bean definition metadata
     * (again typically declared as a lambda expression).
     * <p>This method can be overridden to adapt the registration mechanism for
     * all {@code registerBean} methods (since they all delegate to this one).
     * @param beanName the name of the bean (may be {@code null})
     * @param beanClass the class of the bean
     * @param supplier a callback for creating an instance of the bean (in case
     * of {@code null}, resolving a public constructor to be autowired instead)
     * @param customizers one or more callbacks for customizing the factory's
     * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
     * @since 5.0
     */
    public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
        @Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

        ClassDerivedBeanDefinition beanDefinition = new ClassDerivedBeanDefinition(beanClass);
        if (supplier != null) {
            beanDefinition.setInstanceSupplier(supplier);
        }
        for (BeanDefinitionCustomizer customizer : customizers) {
            customizer.customize(beanDefinition);
        }

        String nameToUse = (beanName != null ? beanName : beanClass.getName());
        registerBeanDefinition(nameToUse, beanDefinition);
    }


    /**
     * {@link RootBeanDefinition} marker subclass for {@code #registerBean} based
     * registrations with flexible autowiring for public constructors.
     */
    @SuppressWarnings("serial")
    private static class ClassDerivedBeanDefinition extends RootBeanDefinition {
        private Supplier<?> instanceSupplier;

        public ClassDerivedBeanDefinition(Class<?> beanClass) {
            super(beanClass);
        }

        public ClassDerivedBeanDefinition(ClassDerivedBeanDefinition original) {
            super(original);
        }

        @Nullable
        public Constructor<?>[] getPreferredConstructors() {
            Class<?> clazz = getBeanClass();
            Constructor<?> primaryCtor = null;
            Constructor<?>[] publicCtors = clazz.getConstructors();
            if (publicCtors.length > 0) {
                return publicCtors;
            }
            return null;
        }

        public RootBeanDefinition cloneBeanDefinition() {
            return new ClassDerivedBeanDefinition(this);
        }

        public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
            this.instanceSupplier = instanceSupplier;
        }
    }

}
