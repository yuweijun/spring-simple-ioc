 

package com.example.spring.simple.ioc.beans;

import com.example.spring.simple.ioc.beans.factory.InitializingBean;

/**
 * Simple test of BeanFactory initialization
 * @author Rod Johnson
 * @since 12-Mar-2003
 * @version $Revision: 1.2 $
 */
public class MustBeInitialized implements InitializingBean {

	private boolean inited; 
	
	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		this.inited = true;
	}
	
	/**
	 * Dummy business method that will fail unless the factory
	 * managed the bean's lifecycle correctly
	 */
	public void businessMethod() {
		if (!this.inited)
			throw new RuntimeException("Factory didn't call afterPropertiesSet() on MustBeInitialized object");
	}

}
