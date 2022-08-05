package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple implementation of MessageSource that allows messages
 * to be held in a Java object, and added programmatically.
 * This MessageSource supports internationalization.
 *
 * <p>Intended for testing rather than use in production systems.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StaticMessageSource extends AbstractMessageSource {

	private final Map messages = new HashMap();

	protected MessageFormat resolveCode(String code, Locale locale) {
		return (MessageFormat) this.messages.get(code + "_" + locale.toString());
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
   * @param locale locale message should be found within
	 * @param message message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String message) {
		this.messages.put(code + "_" + locale.toString(), new MessageFormat(message));
		logger.info("Added message [" + message + "] for code [" + code + "] and Locale [" + locale + "]");
	}

	public String toString() {
		return getClass().getName() + ": " + this.messages;
	}

}

