
package org.springframework.context;

import java.util.EventListener;

/**
 * Interface to be implemented by application event listeners.
 * Based on standard java.util base interface for Observer design pattern.
 * @author Rod Johnson
 */
public interface ApplicationListener extends EventListener {

	/**
	* Handle an application event
	* @param event the event to respond to
	*/
	void onApplicationEvent(ApplicationEvent event);

}
