package org.springframework.context.annotation;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;
import java.util.function.Supplier;

public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {
    //
    // private final AnnotatedBeanDefinitionReader reader;
    //
    // private final ClassPathBeanDefinitionScanner scanner;
    //
    // /**
    //  * Create a new AnnotationConfigApplicationContext that needs to be populated
    //  * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
    //  */
    // public AnnotationConfigApplicationContext() {
    //     StartupStep createAnnotatedBeanDefReader = this.getApplicationStartup().start("spring.context.annotated-bean-reader.create");
    //     this.reader = new AnnotatedBeanDefinitionReader(this);
    //     createAnnotatedBeanDefReader.end();
    //     this.scanner = new ClassPathBeanDefinitionScanner(this);
    // }
    //
    // /**
    //  * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
    //  * @param beanFactory the DefaultListableBeanFactory instance to use for this context
    //  */
    // public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
    //     super(beanFactory);
    //     this.reader = new AnnotatedBeanDefinitionReader(this);
    //     this.scanner = new ClassPathBeanDefinitionScanner(this);
    // }
    //
    // /**
    //  * Create a new AnnotationConfigApplicationContext, deriving bean definitions
    //  * from the given component classes and automatically refreshing the context.
    //  * @param componentClasses one or more component classes &mdash; for example,
    //  * {@link Configuration @Configuration} classes
    //  */
    // public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
    //     this();
    //     register(componentClasses);
    //     refresh();
    // }
    //
    // /**
    //  * Create a new AnnotationConfigApplicationContext, scanning for components
    //  * in the given packages, registering bean definitions for those components,
    //  * and automatically refreshing the context.
    //  * @param basePackages the packages to scan for component classes
    //  */
    // public AnnotationConfigApplicationContext(String... basePackages) {
    //     this();
    //     scan(basePackages);
    //     refresh();
    // }
    //
    //
    // /**
    //  * Propagate the given custom {@code Environment} to the underlying
    //  * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
    //  */
    // @Override
    // public void setEnvironment(ConfigurableEnvironment environment) {
    //     super.setEnvironment(environment);
    //     this.reader.setEnvironment(environment);
    //     this.scanner.setEnvironment(environment);
    // }
    //
    // /**
    //  * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
    //  * and/or {@link ClassPathBeanDefinitionScanner}, if any.
    //  * <p>Default is {@link AnnotationBeanNameGenerator}.
    //  * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
    //  * and/or {@link #scan(String...)}.
    //  * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
    //  * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
    //  * @see AnnotationBeanNameGenerator
    //  * @see FullyQualifiedAnnotationBeanNameGenerator
    //  */
    // public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
    //     this.reader.setBeanNameGenerator(beanNameGenerator);
    //     this.scanner.setBeanNameGenerator(beanNameGenerator);
    //     getBeanFactory().registerSingleton(
    //         AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
    // }
    //
    // /**
    //  * Set the {@link ScopeMetadataResolver} to use for registered component classes.
    //  * <p>The default is an {@link AnnotationScopeMetadataResolver}.
    //  * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
    //  * and/or {@link #scan(String...)}.
    //  */
    // public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
    //     this.reader.setScopeMetadataResolver(scopeMetadataResolver);
    //     this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
    // }
    //
    //
    // //---------------------------------------------------------------------
    // // Implementation of AnnotationConfigRegistry
    // //---------------------------------------------------------------------

    /**
     * Register one or more component classes to be processed.
     * <p>Note that {@link #refresh()} must be called in order for the context
     * to fully process the new classes.
     * @param componentClasses one or more component classes &mdash; for example,
     * {@link Configuration @Configuration} classes
     * @see #scan(String...)
     * @see #refresh()
     */
    @Override
    public void register(Class<?>... componentClasses) {
        // Assert.notEmpty(componentClasses, "At least one component class must be specified");
        // StartupStep registerComponentClass = this.getApplicationStartup().start("spring.context.component-classes.register")
        //                                          .tag("classes", () -> Arrays.toString(componentClasses));
        // this.reader.register(componentClasses);
        // registerComponentClass.end();
    }

    /**
     * Perform a scan within the specified base packages.
     * <p>Note that {@link #refresh()} must be called in order for the context
     * to fully process the new classes.
     * @param basePackages the packages to scan for component classes
     * @see #register(Class...)
     * @see #refresh()
     */
    @Override
    public void scan(String... basePackages) {
        // Assert.notEmpty(basePackages, "At least one base package must be specified");
        // StartupStep scanPackages = this.getApplicationStartup().start("spring.context.base-packages.scan")
        //                                .tag("packages", () -> Arrays.toString(basePackages));
        // this.scanner.scan(basePackages);
        // scanPackages.end();
    }


    //---------------------------------------------------------------------
    // Adapt superclass registerBean calls to AnnotatedBeanDefinitionReader
    //---------------------------------------------------------------------

    // @Override
    // public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
    //     @Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {
    //
    //     this.reader.registerBean(beanClass, beanName, supplier, customizers);
    // }

}
