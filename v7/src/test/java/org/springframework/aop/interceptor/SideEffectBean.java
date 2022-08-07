 

package org.springframework.aop.interceptor;

/**
 * Bean that changes state on a business invocation, so that
 * we can check whether it's been invoked
 * @author Rod Johnson
 * @version $Id: SideEffectBean.java,v 1.2 2004-03-18 03:01:18 trisberg Exp $
 */
public class SideEffectBean {
	
	private int count;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public void doWork() {
		++count;
	}

}
