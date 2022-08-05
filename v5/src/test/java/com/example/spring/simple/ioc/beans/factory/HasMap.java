 

package com.example.spring.simple.ioc.beans.factory;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Bean exposing a map. Used for bean factory tests.
 * @author Rod Johnson
 * @since 05-Jun-2003
 * @version $Id: HasMap.java,v 1.4 2004-05-18 08:03:31 jhoeller Exp $
 */
public class HasMap {
	
	private Map map;

	private Set set;

	private Properties props;
	
	private Object[] objectArray;
	
	private Class[] classArray;
	
	private Integer[] intArray;

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		this.set = set;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public Object[] getObjectArray() {
		return objectArray;
	}

	public void setObjectArray(Object[] objectArray) {
		this.objectArray = objectArray;
	}

	public Class[] getClassArray() {
		return classArray;
	}

	public void setClassArray(Class[] classArray) {
		this.classArray = classArray;
	}

	public Integer[] getIntegerArray() {
		return intArray;
	}

	public void setIntegerArray(Integer[] is) {
		intArray = is;
	}

}
