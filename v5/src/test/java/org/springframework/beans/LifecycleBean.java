 

package org.springframework.beans;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Simple test of BeanFactory initialization
 * and lifecycle callbacks.
 * @author Rod Johnson
 * @since 12-Mar-2003
 * @version $Revision: 1.4 $
 */
public class LifecycleBean implements BeanNameAware, InitializingBean, BeanFactoryAware, DisposableBean {

	private String beanName;

	private BeanFactory owningFactory;

	private boolean postProcessedBeforeInit;

	private boolean inited;

	private boolean postProcessedAfterInit;

	private boolean destroyed;

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.owningFactory = beanFactory;
	}

	public void postProcessBeforeInit() {
		if (this.inited) {
			throw new RuntimeException("Factory called postProcessBeforeInit after afterPropertiesSet");
		}
		if (this.postProcessedBeforeInit) {
			throw new RuntimeException("Factory called postProcessBeforeInit twice");
		}
		this.postProcessedBeforeInit = true;
	}

	public void afterPropertiesSet() {
		if (this.owningFactory == null) {
			throw new RuntimeException("Factory didn't call setBeanFactory before afterPropertiesSet on lifecycle bean");
		}
		if (!this.postProcessedBeforeInit) {
			throw new RuntimeException("Factory didn't call postProcessBeforeInit before afterPropertiesSet on lifecycle bean");
		}
		if (this.inited) {
			throw new RuntimeException("Factory called afterPropertiesSet twice");
		}
		this.inited = true;
	}

	public void postProcessAfterInit() {
		if (!this.inited) {
			throw new RuntimeException("Factory called postProcessAfterInit before afterPropertiesSet");
		}
		if (this.postProcessedAfterInit) {
			throw new RuntimeException("Factory called postProcessAfterInit twice");
		}
		this.postProcessedAfterInit = true;
	}

	/**
	 * Dummy business method that will fail unless the factory
	 * managed the bean's lifecycle correctly
	 */
	public void businessMethod() {
		if (!this.inited || !this.postProcessedAfterInit) {
			throw new RuntimeException("Factory didn't initialize lifecycle object correctly");
		}
	}

	public void destroy() {
		if (this.destroyed) {
			throw new IllegalStateException("Already destroyed");
		}
		this.destroyed = true;
	}

	public boolean isDestroyed() {
		return destroyed;
	}


	public static class PostProcessor implements BeanPostProcessor {

		public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
			if (bean instanceof LifecycleBean) {
				((LifecycleBean) bean).postProcessBeforeInit();
			}
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
			if (bean instanceof LifecycleBean) {
				((LifecycleBean) bean).postProcessAfterInit();
			}
			return bean;
		}
	}

}
