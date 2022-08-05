 

package org.springframework.beans;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;

/**
 * @author Juergen Hoeller
 * @since 21.08.2003
 */
public class DerivedTestBean extends TestBean implements Serializable, BeanNameAware, DisposableBean {

	private String beanName;

	private boolean destroyed;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	public void destroy() {
		this.destroyed = true;
	}

	public boolean wasDestroyed() {
		return destroyed;
	}

}
