package com.example.spring.simple.ioc.beans.factory.config;

import com.example.spring.simple.ioc.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple factory for shared List instances. Allows for central setup
 * of Lists via the "list" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class ListFactoryBean extends AbstractFactoryBean {

	private List sourceList;

	private Class targetListClass = ArrayList.class;

	/**
	 * Set the source List, typically populated via XML "list" elements.
	 */
	public void setSourceList(List sourceList) {
		this.sourceList = sourceList;
	}

	/**
	 * Set the class to use for the target List.
	 * Default is <code>java.util.ArrayList</code>.
	 * @see ArrayList
	 */
	public void setTargetListClass(Class targetListClass) {
		if (targetListClass == null) {
			throw new IllegalArgumentException("targetListClass must not be null");
		}
		if (!List.class.isAssignableFrom(targetListClass)) {
			throw new IllegalArgumentException("targetListClass must implement java.util.List");
		}
		this.targetListClass = targetListClass;
	}

	public Class getObjectType() {
		return List.class;
	}

	protected Object createInstance() {
		if (this.sourceList == null) {
			throw new IllegalArgumentException("sourceList is required");
		}
		List result = (List) BeanUtils.instantiateClass(this.targetListClass);
		result.addAll(this.sourceList);
		return result;
	}

}
