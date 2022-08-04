package com.example.spring.simple.ioc.core;

/**
 * Interface that can be implemented by exceptions etc that are error coded. The error code is a String, rather than a number, so it can be given user-readable values, such as "object.failureDescription".
 *
 * <p>An error code can be resolved by a MessageSource, for example.
 *
 * @author Rod Johnson
 * @version $Id: ErrorCoded.java,v 1.4 2004-04-22 07:58:23 jhoeller Exp $
 */
public interface ErrorCoded {

    /**
     * Return the error code associated with this failure. The GUI can render this any way it pleases, allowing for localization etc.
     *
     * @return a String error code associated with this failure, or null if not error-coded
     */
    String getErrorCode();

}
