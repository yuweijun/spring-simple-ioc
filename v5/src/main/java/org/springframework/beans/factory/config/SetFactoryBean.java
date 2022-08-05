package org.springframework.beans.factory.config;

import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple factory for shared Set instances. Allows for central setup
 * of Sets via the "set" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 21.01.2003
 * @version $Id: SetFactoryBean.java,v 1.4 2004-04-26 22:03:08 jhoeller Exp $
 */
public class SetFactoryBean extends AbstractFactoryBean {

	private Set sourceSet;

	private Class targetSetClass = HashSet.class;

	/**
	 * Set the source Set, typically populated via XML "set" elements.
	 */
	public void setSourceSet(Set sourceSet) {
		this.sourceSet = sourceSet;
	}

	/**
	 * Set the class to use for the target Set.
	 * Default is <code>java.util.HashSet</code>.
	 * @see HashSet
	 */
	public void setTargetSetClass(Class targetSetClass) {
		if (targetSetClass == null) {
			throw new IllegalArgumentException("targetSetClass must not be null");
		}
		if (!Set.class.isAssignableFrom(targetSetClass)) {
			throw new IllegalArgumentException("targetSetClass must implement java.util.Set");
		}
		this.targetSetClass = targetSetClass;
	}

	public Class getObjectType() {
		return Set.class;
	}

	protected Object createInstance() {
		if (this.sourceSet == null) {
			throw new IllegalArgumentException("sourceSet is required");
		}
		Set result = (Set) BeanUtils.instantiateClass(this.targetSetClass);
		result.addAll(this.sourceSet);
		return result;
	}

}
