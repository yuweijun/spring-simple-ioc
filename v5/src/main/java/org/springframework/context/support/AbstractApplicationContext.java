package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ApplicationEventMulticasterImpl;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Partial implementation of ApplicationContext. Doesn't mandate the type
 * of storage used for configuration, but implements common functionality.
 * Uses the Template Method design pattern, requiring concrete subclasses
 * to implement abstract methods.
 *
 * <p>In contrast to a plain bean factory, an ApplicationContext is supposed
 * to detect special beans defined in its bean factory: Therefore, this class
 * automatically registers BeanFactoryPostProcessors, BeanPostProcessors
 * and ApplicationListeners that are defined as beans in the context.
 *
 * <p>A MessageSource may be also supplied as a bean in the context, with
 * the name "messageSource". Else, message resolution is delegated to the
 * parent context.
 *
 * <p>Implements resource loading through extending DefaultResourceLoader.
 * Therefore, treats resource paths as class path resources. Only supports
 * full classpath resource names that include the package path, like
 * "mypackage/myresource.dat".
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 21, 2001
 * @version $Revision: 1.43 $
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see #MESSAGE_SOURCE_BEAN_NAME
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent context */
	private ApplicationContext parent;

	/** BeanFactoryPostProcessors to apply on refresh */
	private final List beanFactoryPostProcessors = new ArrayList();

	/** Display name */
	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	/** System time in milliseconds when this context started */
	private long startupTime;

	/** MessageSource we delegate our implementation of this interface to */
	private MessageSource messageSource;

	/** Helper class used in event publishing */
	private final ApplicationEventMulticaster applicationEventMulticaster = new ApplicationEventMulticasterImpl();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this.parent = parent;
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext
	//---------------------------------------------------------------------

	/**
	 * Return the parent context, or null if there is no parent
	 * (that is, this context is the root of the context hierarchy).
	 */
	@Override
	public ApplicationContext getParent() {
		return parent;
	}

	/**
	 * Set a friendly name for this context.
	 * Typically done during initialization of concrete context implementations.
	 */
	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for this context.
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return the timestamp (ms) when this context was first loaded.
	 */
	@Override
	public long getStartupDate() {
		return startupTime;
	}

	/**
	 * Publish the given event to all listeners.
	 * <p>Note: Listeners get initialized after the MessageSource, to be able
	 * to access it within listener implementations. Thus, MessageSource
	 * implementation cannot publish events.
	 * @param event event to publish (may be application-specific or a
	 * standard framework event)
	 */
	@Override
	public void publishEvent(ApplicationEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event in context [" + getDisplayName() + "]: " + event.toString());
		}
		this.applicationEventMulticaster.onApplicationEvent(event);
		if (this.parent != null) {
			this.parent.publishEvent(event);
		}
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableApplicationContext
	//---------------------------------------------------------------------

	@Override
	public void setParent(ApplicationContext parent) {
		this.parent = parent;
	}

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		this.beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 */
	public List getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}

	/**
	 * Load or reload configuration.
	 * @throws org.springframework.context.ApplicationContextException if the
	 * configuration was invalid or couldn't be found, or if configuration
	 * has already been loaded and reloading is forbidden
	 * @throws BeansException if the bean factory could not be initialized
	 */
	@Override
	public void refresh() throws BeansException {
		this.startupTime = System.currentTimeMillis();

		// tell subclass to refresh the internal bean factory
		refreshBeanFactory();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();

		// configure the bean factory with context semantics
		beanFactory.registerCustomEditor(Resource.class, new ResourceEditor(this));
		beanFactory.registerCustomEditor(InputStream.class, new InputStreamEditor(new ResourceEditor(this)));
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
		beanFactory.ignoreDependencyType(ResourceLoader.class);
		beanFactory.ignoreDependencyType(ApplicationContext.class);

		// allows post-processing of the bean factory in context subclasses
		postProcessBeanFactory(beanFactory);

		// invoke factory processors registered with the context instance
		for (Iterator it = getBeanFactoryPostProcessors().iterator(); it.hasNext();) {
			BeanFactoryPostProcessor factoryProcessor = (BeanFactoryPostProcessor) it.next();
			factoryProcessor.postProcessBeanFactory(beanFactory);
		}

		if (getBeanDefinitionCount() == 0) {
			logger.warn("No beans defined in ApplicationContext [" + getDisplayName() + "]");
		}
		else {
			logger.info(getBeanDefinitionCount() + " beans defined in ApplicationContext [" + getDisplayName() + "]");
		}

		// invoke factory processors registered as beans in the context
		invokeBeanFactoryPostProcessors();

		// register bean processor that intercept bean creation
		registerBeanPostProcessors();

		// initialize message source for this context
		initMessageSource();

		// initialize other special beans in specific context subclasses
		onRefresh();

		// check for listener beans and register them
		refreshListeners();

		// instantiate singletons this late to allow them to access the message source
		beanFactory.preInstantiateSingletons();

		// last step: publish corresponding event
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for registering special
	 * BeanPostProcessors etc in certain ApplicationContext implementations.
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of errors
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * Must be called before singleton instantiation.
	 */
	private void invokeBeanFactoryPostProcessors() throws BeansException {
		String[] beanNames = getBeanDefinitionNames(BeanFactoryPostProcessor.class);
		BeanFactoryPostProcessor[] factoryProcessors = new BeanFactoryPostProcessor[beanNames.length];
		for (int i = 0; i < beanNames.length; i++) {
			factoryProcessors[i] = (BeanFactoryPostProcessor) getBean(beanNames[i]);
		}
		Arrays.sort(factoryProcessors, new OrderComparator());
		for (int i = 0; i < factoryProcessors.length; i++) {
			BeanFactoryPostProcessor factoryProcessor = factoryProcessors[i];
			factoryProcessor.postProcessBeanFactory(getBeanFactory());
		}
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before any instantiation of application beans.
	 */
	private void registerBeanPostProcessors() throws BeansException {
		String[] beanNames = getBeanDefinitionNames(BeanPostProcessor.class);
		if (beanNames.length > 0) {
			List beanProcessors = new ArrayList();
			for (int i = 0; i < beanNames.length; i++) {
				beanProcessors.add(getBean(beanNames[i]));
			}
			Collections.sort(beanProcessors, new OrderComparator());
			for (Iterator it = beanProcessors.iterator(); it.hasNext();) {
				getBeanFactory().addBeanPostProcessor((BeanPostProcessor) it.next());
			}
		}
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 */
	private void initMessageSource() throws BeansException {
		try {
			this.messageSource = (MessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
			// set parent message source if applicable,
			// and if the message source is defined in this context, not in a parent
			if (this.parent != null && (this.messageSource instanceof HierarchicalMessageSource) &&
			    Arrays.asList(getBeanDefinitionNames()).contains(MESSAGE_SOURCE_BEAN_NAME)) {
				((HierarchicalMessageSource) this.messageSource).setParentMessageSource(this.parent);
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.info("No MessageSource found for context [" + getDisplayName() + "]: using empty StaticMessageSource");
			// use empty message source to be able to accept getMessage calls
			this.messageSource = new StaticMessageSource();
		}
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * Called on initialization of special beans, before instantiation of singletons.
	 * @throws BeansException in case of errors during refresh
	 * @see #refresh
	 */
	protected void onRefresh() throws BeansException {
		// for subclasses: do nothing by default
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	private void refreshListeners() throws BeansException {
		logger.info("Refreshing listeners");
		Collection listeners = getBeansOfType(ApplicationListener.class, true, false).values();
		logger.debug("Found " + listeners.size() + " listeners in bean factory");
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ApplicationListener listener = (ApplicationListener) it.next();
			addListener(listener);
			if (logger.isInfoEnabled()) {
				logger.info("Application listener [" + listener + "] added");
			}
		}
	}

	/**
	 * Subclasses can invoke this method to register a listener.
	 * Any beans in the context that are listeners are automatically added.
	 * @param listener the listener to register
	 */
	protected void addListener(ApplicationListener listener) {
		this.applicationEventMulticaster.addApplicationListener(listener);
	}

	/**
	 * Destroy the singletons in the bean factory of this application context.
	 */
	@Override
	public void close() {
		logger.info("Closing application context [" + getDisplayName() + "]");

		// Destroy all cached singletons in this context,
		// invoking DisposableBean.destroy and/or "destroy-method".
		getBeanFactory().destroySingletons();

		// publish corresponding event
		publishEvent(new ContextClosedEvent(this));
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory
	//---------------------------------------------------------------------

	@Override
	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	@Override
	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	@Override
	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	@Override
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	@Override
	public String[] getBeanDefinitionNames(Class type) {
		return getBeanFactory().getBeanDefinitionNames(type);
	}

	@Override
	public boolean containsBeanDefinition(String name) {
		return getBeanFactory().containsBeanDefinition(name);
	}

	@Override
	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
			throws BeansException {
		return getBeanFactory().getBeansOfType(type, includePrototypes, includeFactoryBeans);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory
	//---------------------------------------------------------------------

	@Override
	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	/**
	 * Return the internal bean factory of the parent context if it implements
	 * ConfigurableApplicationContext; else, return the parent context itself.
	 * @see ConfigurableApplicationContext#getBeanFactory
	 */
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext) ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : (BeanFactory) getParent();
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	@Override
	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return this.messageSource.getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return this.messageSource.getMessage(resolvable, locale);
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by refresh before any other initialization work.
	 * @see #refresh
	 */
	protected abstract void refreshBeanFactory() throws BeansException;

	/**
	 * Subclasses must return their internal bean factory here.
	 * They should implement the lookup efficiently, so that it can be called
	 * repeatedly without a performance penalty.
	 * @return this application context's internal bean factory
	 */
	public abstract ConfigurableListableBeanFactory getBeanFactory();


	/**
	 * Return information about this context.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(": ");
		sb.append("displayName=[").append(this.displayName).append("]; ");
		sb.append("startup date=[").append(new Date(this.startupTime)).append("]; ");
		if (this.parent == null) {
			sb.append("root of ApplicationContext hierarchy");
		}
		else {
			sb.append("parent=[").append(this.parent).append(']');
		}
		return sb.toString();
	}

}
