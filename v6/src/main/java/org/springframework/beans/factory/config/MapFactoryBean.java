package org.springframework.beans.factory.config;

import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple factory for shared Map instances. Allows for central setup
 * of Maps via the "map" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class MapFactoryBean extends AbstractFactoryBean {

	private Map sourceMap;

	private Class targetMapClass = HashMap.class;

	/**
	 * Set the source Map, typically populated via XML "map" elements.
	 */
	public void setSourceMap(Map sourceMap) {
		this.sourceMap = sourceMap;
	}

	/**
	 * Set the class to use for the target Map.
	 * Default is <code>java.util.HashMap</code>.
	 * @see HashMap
	 */
	public void setTargetMapClass(Class targetMapClass) {
		if (targetMapClass == null) {
			throw new IllegalArgumentException("targetMapClass must not be null");
		}
		if (!Map.class.isAssignableFrom(targetMapClass)) {
			throw new IllegalArgumentException("targetMapClass must implement java.util.Map");
		}
		this.targetMapClass = targetMapClass;
	}

	public Class getObjectType() {
		return Map.class;
	}

	protected Object createInstance() {
		if (this.sourceMap == null) {
			throw new IllegalArgumentException("sourceMap is required");
		}
		Map result = (Map) BeanUtils.instantiateClass(this.targetMapClass);
		result.putAll(this.sourceMap);
		return result;
	}

}
