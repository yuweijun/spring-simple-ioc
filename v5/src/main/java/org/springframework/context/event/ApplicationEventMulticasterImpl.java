package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple implementation of the ApplicationEventMulticaster interface.
 *
 * <p>Doesn't permit multiple instances of the same listener, as it keeps
 * listeners in a HashSet.
 *
 * <p>Note that this class doesn't try to do anything clever to ensure thread
 * safety if listeners are added or removed at runtime. A technique such as
 * Copy-on-Write (Lea:137) could be used to ensure this, but the assumption in
 * this version of this framework is that listeners will be added at application
 * configuration time and not added or removed as the application runs.
 *
 * <p>All listeners are invoked in the calling thread. This allows the danger of
 * a rogue listener blocking the entire application, but adds minimal overhead.
 *
 * <p>An alternative implementation could be more sophisticated in both these respects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class ApplicationEventMulticasterImpl implements ApplicationEventMulticaster {

	/** Set of listeners */
	private final Set applicationListeners = new HashSet();

	public void addApplicationListener(ApplicationListener listener) {
		this.applicationListeners.add(listener);
	}

	public void removeApplicationListener(ApplicationListener listener) {
		this.applicationListeners.remove(listener);
	}

	public void onApplicationEvent(ApplicationEvent event) {
		Iterator it = this.applicationListeners.iterator();
		while (it.hasNext()) {
			ApplicationListener listener = (ApplicationListener) it.next();
			listener.onApplicationEvent(event);
		}
	}

	public void removeAllListeners() {
		this.applicationListeners.clear();
	}

}
