package org.springframework.core;

/**
 * Exception thrown when the Constants class is asked for an invalid
 * constant name.
 * @version $Id: ConstantException.java,v 1.4 2004-06-02 00:49:40 jhoeller Exp $
 * @author Rod Johnson
 * @since 28-Apr-2003
 * @see Constants
 */
public class ConstantException extends IllegalArgumentException {
	
	/**
	 * Thrown when an invalid constant name is requested.
	 * @param className name of the class containing the constant definitions
	 * @param field invalid constant name
	 * @param message description of the problem
	 */
	public ConstantException(String className, String field, String message) {
		super("Field '" + field + "' " + message + " in class [" + className + "]");
	}

	/**
	 * Thrown when an invalid constant value is looked up.
	 * @param className name of the class containing the constant definitions
	 * @param namePrefix prefix of the searched constant names
	 * @param value the looked up constant value
	 */
	public ConstantException(String className, String namePrefix, Object value) {
		super("No '" + namePrefix + "' field with value '" + value + "' found in class [" + className + "]");
	}

}
