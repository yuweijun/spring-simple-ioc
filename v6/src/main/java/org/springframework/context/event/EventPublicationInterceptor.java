package org.springframework.context.event;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Constructor;

/**
 * Interceptor that knows how to publish ApplicationEvents to all
 * ApplicationListeners registered with an ApplicationContext.
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptor.java,v 1.4 2004-04-01 15:09:31 jhoeller Exp $
 * @see ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 */
public class EventPublicationInterceptor implements MethodInterceptor, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private Class applicationEventClass;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set the application event class to publish.
	 * <p>The event class must have a constructor with a single Object argument
	 * for the event source. The interceptor will pass in the invoked object.
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		if (applicationEventClass == null || !ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("applicationEventClass needs to implement ApplicationEvent");
		}
		this.applicationEventClass = applicationEventClass;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[] {Object.class});
		ApplicationEvent event = (ApplicationEvent) constructor.newInstance(new Object[] {invocation.getThis()});
		this.applicationContext.publishEvent(event);
		return retVal;
	}

}
