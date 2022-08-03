package org.springframework.context.annotation;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

    // private final AnnotatedBeanDefinitionReader reader;
    // private final ClassPathBeanDefinitionScanner scanner;

    public AnnotationConfigApplicationContext() {
        // StartupStep createAnnotatedBeanDefReader = this.getApplicationStartup().start("spring.context.annotated-bean-reader.create");
        // this.reader = new AnnotatedBeanDefinitionReader(this);
        // createAnnotatedBeanDefReader.end();
        // this.scanner = new ClassPathBeanDefinitionScanner(this);
    }

    public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
        // this.reader = new AnnotatedBeanDefinitionReader(this);
        // this.scanner = new ClassPathBeanDefinitionScanner(this);
    }

    public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
        this();
        register(componentClasses);
        refresh();
    }

    public AnnotationConfigApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }

    // public void setEnvironment(ConfigurableEnvironment environment) {
    //     super.setEnvironment(environment);
    //     this.reader.setEnvironment(environment);
    //     this.scanner.setEnvironment(environment);
    // }

    // public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
    //     this.reader.setBeanNameGenerator(beanNameGenerator);
    //     this.scanner.setBeanNameGenerator(beanNameGenerator);
    //     getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
    // }

    // public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
    //     this.reader.setScopeMetadataResolver(scopeMetadataResolver);
    //     this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
    // }


    //---------------------------------------------------------------------
    // Implementation of AnnotationConfigRegistry
    //---------------------------------------------------------------------

    @Override
    public void register(Class<?>... componentClasses) {
        // Assert.notEmpty(componentClasses, "At least one component class must be specified");
        // StartupStep registerComponentClass = this.getApplicationStartup().start("spring.context.component-classes.register")
        //                                          .tag("classes", () -> Arrays.toString(componentClasses));
        // this.reader.register(componentClasses);
        // registerComponentClass.end();
    }

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
