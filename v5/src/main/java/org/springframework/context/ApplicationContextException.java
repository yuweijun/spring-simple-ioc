package org.springframework.context;

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown during application context initialization.
 * @author Rod Johnson
 */
public class ApplicationContextException extends FatalBeanException {

	/**
	 * Constructs an <code>ApplicationContextException</code>
	 * with the specified detail message and no root cause.
	 * @param msg the detail message
	 */
	public ApplicationContextException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an <code>ApplicationContextException</code>
	 * with the specified detail message and the given root cause.
	 * @param msg the detail message
	 */
	public ApplicationContextException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
