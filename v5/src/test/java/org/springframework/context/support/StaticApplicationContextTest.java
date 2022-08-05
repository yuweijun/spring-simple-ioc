package org.springframework.context.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTest;
import org.springframework.context.BeanThatListens;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StaticApplicationContextTest extends AbstractApplicationContextTest {

    protected StaticApplicationContext sac;

    /** Run for each test */
    protected ConfigurableApplicationContext createContext() throws Exception {
        StaticApplicationContext parent = new StaticApplicationContext();
        Map m = new HashMap();
        m.put("name", "Roderick");
        parent.registerPrototype("rod", TestBean.class, new MutablePropertyValues(m));
        m.put("name", "Albert");
        parent.registerPrototype("father", TestBean.class, new MutablePropertyValues(m));
        parent.refresh();
        parent.addListener(parentListener) ;

        StaticMessageSource parentMessageSource = (StaticMessageSource) parent.getBean("messageSource");
        parentMessageSource.addMessage("code1", Locale.getDefault(), "message1");

        this.sac = new StaticApplicationContext(parent);
        sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
        sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
        sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
        PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(sac.getDefaultListableBeanFactory());
        reader.loadBeanDefinitions(new ClassPathResource("testBeans.properties", getClass()));
        sac.refresh();
        sac.addListener(listener);

        StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
        sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");

        return sac;
    }

}