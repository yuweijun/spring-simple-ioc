package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

import java.util.Iterator;
import java.util.Properties;

/**
 * A property resource configurer that overrides bean property values in an application
 * context definition. It <i>pushes</i> values from a properties file into bean definitions.
 *
 * <p>Configuration lines are expected to be of the following form:
 *
 * <pre>
 * beanName.property=value</pre>
 *
 * Example properties file:
 *
 * <pre>
 * dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * In contrast to PropertyPlaceholderConfigurer, the original definition can have default
 * values or no values at all for such bean properties. If an overriding properties file does
 * not have an entry for a certain bean property, the default context definition is used.
 *
 * <p>Note that the context definition <i>is not</i> aware of being overridden;
 * so this is not immediately obvious when looking at the XML definition file.
 *
 * <p>In case of multiple PropertyOverrideConfigurers that define different values for
 * the same bean property, the <i>last</i> one will win (due to the overriding mechanism).
 *
 * @author Juergen Hoeller
 * @since 12.03.2003
 * @see PropertyPlaceholderConfigurer
 * @version $Id: PropertyOverrideConfigurer.java,v 1.6 2004-04-22 07:58:22 jhoeller Exp $
 */
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {
		for (Iterator it = props.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			processKey(beanFactory, key, props.getProperty(key));
		}
	}

	/**
	 * Process the given key as 'beanName.property' entry.
	 */
	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
			throws BeansException {
		int dotIndex = key.indexOf('.');
		if (dotIndex == -1) {
			throw new FatalBeanException("Invalid key [" + key + "]: expected 'beanName.property'");
		}
		String beanName = key.substring(0, dotIndex);
		String beanProperty = key.substring(dotIndex+1);
		BeanDefinition bd = factory.getBeanDefinition(beanName);
		bd.getPropertyValues().addPropertyValue(beanProperty, value);
		logger.debug("Property '" + key + "' set to [" + value + "]");
	}

}
