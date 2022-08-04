package com.example.spring.simple.ioc.beans.factory.support;

import com.example.spring.simple.ioc.aop.interceptor.SideEffectBean;
import com.example.spring.simple.ioc.beans.BeansException;
import com.example.spring.simple.ioc.beans.MutablePropertyValues;
import com.example.spring.simple.ioc.beans.PropertyValue;
import com.example.spring.simple.ioc.beans.TestBean;
import com.example.spring.simple.ioc.beans.factory.BeanDefinitionStoreException;
import com.example.spring.simple.ioc.beans.factory.BeanFactoryUtils;
import com.example.spring.simple.ioc.beans.factory.DummyFactory;
import com.example.spring.simple.ioc.beans.factory.ITestBean;
import com.example.spring.simple.ioc.beans.factory.ListableBeanFactory;
import com.example.spring.simple.ioc.beans.factory.NestedTestBean;
import com.example.spring.simple.ioc.beans.factory.UnsatisfiedDependencyException;
import com.example.spring.simple.ioc.beans.factory.config.AutowireCapableBeanFactory;
import com.example.spring.simple.ioc.beans.factory.xml.ConstructorDependenciesBean;
import com.example.spring.simple.ioc.beans.propertyeditors.CustomNumberEditor;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DefaultListableBeanFactoryTest {

    @Test
    public void testUnreferencedSingletonWasInstantiated() {
        KnowsIfInstantiated.clearInstantiationRecord();
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("x1.class", KnowsIfInstantiated.class.getName());
        AssertionUtil.assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        lbf.preInstantiateSingletons();
        AssertionUtil.assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
    }

    @Test
    public void testLazyInitialization() {
        KnowsIfInstantiated.clearInstantiationRecord();
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("x1.class", KnowsIfInstantiated.class.getName());
        p.setProperty("x1.(lazy-init)", "true");
        AssertionUtil.assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        AssertionUtil.assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
        lbf.preInstantiateSingletons();
        AssertionUtil.assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
        lbf.getBean("x1");
        AssertionUtil.assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
    }

    @Test
    public void testFactoryBeanDidNotCreatePrototype() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("x1.class", DummyFactory.class.getName());
        // Reset static state
        DummyFactory.reset();
        p.setProperty("x1.singleton", "false");
        AssertionUtil.assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        AssertionUtil.assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
        lbf.preInstantiateSingletons();
        AssertionUtil.assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
        lbf.getBean("x1");
        AssertionUtil.assertTrue("prototype was instantiated", DummyFactory.wasPrototypeCreated());
    }

    @Test
    public void testEmpty() {
        ListableBeanFactory lbf = new DefaultListableBeanFactory();
        AssertionUtil.assertTrue("No beans defined --> array != null", lbf.getBeanDefinitionNames() != null);
        AssertionUtil.assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionNames().length == 0);
        AssertionUtil.assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionCount() == 0);
    }

    @Test
    public void testEmptyPropertiesPopulation() throws BeansException {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        AssertionUtil.assertTrue("No beans defined after ignorable invalid", lbf.getBeanDefinitionCount() == 0);
    }

    @Test
    public void testHarmlessIgnorableRubbish() throws BeansException {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("qwert", "er");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, "test");
        AssertionUtil.assertTrue("No beans defined after harmless ignorable rubbish", lbf.getBeanDefinitionCount() == 0);
    }

    @Test
    public void testPropertiesPopulationWithNullPrefix() throws Exception {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("test.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("test.name", "Tony");
        p.setProperty("test.age", "48");
        //p.setProperty("
        int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        AssertionUtil.assertTrue("1 beans registered, not " + count, count == 1);
        testSingleTestBean(lbf);
    }

    @Test
    public void testPropertiesPopulationWithPrefix() throws Exception {
        String PREFIX = "beans.";
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty(PREFIX + "test.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty(PREFIX + "test.name", "Tony");
        p.setProperty(PREFIX + "test.age", "48");

        int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
        AssertionUtil.assertTrue("1 beans registered, not " + count, count == 1);
        testSingleTestBean(lbf);
    }

    @Test
    public void testSimpleReference() throws Exception {
        String PREFIX = "beans.";
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();

        p.setProperty(PREFIX + "rod.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty(PREFIX + "rod.name", "Rod");

        p.setProperty(PREFIX + "kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty(PREFIX + "kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty(PREFIX + "kerry.name", "Kerry");
        p.setProperty(PREFIX + "kerry.age", "35");
        p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");
        //p.setProperty("
        int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
        AssertionUtil.assertTrue("2 beans registered, not " + count, count == 2);

        TestBean kerry = (TestBean) lbf.getBean("kerry", TestBean.class);
        AssertionUtil.assertTrue("Kerry name is Kerry", "Kerry".equals(kerry.getName()));
        ITestBean spouse = kerry.getSpouse();
        AssertionUtil.assertTrue("Kerry spouse is non null", spouse != null);
        AssertionUtil.assertTrue("Kerry spouse name is Rod", "Rod".equals(spouse.getName()));
    }

    @Test
    public void testUnresolvedReference() throws Exception {
        String PREFIX = "beans.";
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();

        //p.setProperty(PREFIX + "rod.class", "com.example.spring.simple.ioc.beans.TestBean");
        //p.setProperty(PREFIX + "rod.name", "Rod");

        try {
            p.setProperty(PREFIX + "kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
            p.setProperty(PREFIX + "kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
            p.setProperty(PREFIX + "kerry.name", "Kerry");
            p.setProperty(PREFIX + "kerry.age", "35");
            p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");

            (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);

            Object kerry = lbf.getBean("kerry");
            fail("Unresolved reference should have been detected");
        } catch (BeansException ex) {
            // cool
        }
    }

    @Test
    public void testPrototype() throws Exception {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        TestBean kerry1 = (TestBean) lbf.getBean("kerry");
        TestBean kerry2 = (TestBean) lbf.getBean("kerry");
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Singletons equal", kerry1 == kerry2);

        lbf = new DefaultListableBeanFactory();
        p = new Properties();
        p.setProperty("kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("kerry.(singleton)", "false");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        kerry1 = (TestBean) lbf.getBean("kerry");
        kerry2 = (TestBean) lbf.getBean("kerry");
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Prototypes NOT equal", kerry1 != kerry2);

        lbf = new DefaultListableBeanFactory();
        p = new Properties();
        p.setProperty("kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("kerry.(singleton)", "true");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        kerry1 = (TestBean) lbf.getBean("kerry");
        kerry2 = (TestBean) lbf.getBean("kerry");
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Specified singletons equal", kerry1 == kerry2);
    }

    @Test
    public void testPrototypeExtendsPrototype() throws Exception {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("wife.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("wife.name", "kerry");

        p.setProperty("kerry.parent", "wife");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        TestBean kerry1 = (TestBean) lbf.getBean("kerry");
        TestBean kerry2 = (TestBean) lbf.getBean("kerry");
        assertEquals("kerry", kerry1.getName());
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Singletons equal", kerry1 == kerry2);

        lbf = new DefaultListableBeanFactory();
        p = new Properties();
        p.setProperty("wife.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("wife.name", "kerry");
        p.setProperty("wife.(singleton)", "false");
        p.setProperty("kerry.parent", "wife");
        p.setProperty("kerry.(singleton)", "false");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        assertFalse(lbf.isSingleton("kerry"));
        kerry1 = (TestBean) lbf.getBean("kerry");
        kerry2 = (TestBean) lbf.getBean("kerry");
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Prototypes NOT equal", kerry1 != kerry2);

        lbf = new DefaultListableBeanFactory();
        p = new Properties();
        p.setProperty("kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("kerry.(singleton)", "true");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        kerry1 = (TestBean) lbf.getBean("kerry");
        kerry2 = (TestBean) lbf.getBean("kerry");
        AssertionUtil.assertTrue("Non null", kerry1 != null);
        AssertionUtil.assertTrue("Specified singletons equal", kerry1 == kerry2);
    }

    @Test
    public void testNameAlreadyBound() throws Exception {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("kerry.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("kerry.age", "35");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        try {
            (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        } catch (BeanDefinitionStoreException ex) {
            // expected
        }
    }

    private void testSingleTestBean(ListableBeanFactory lbf) throws BeansException {
        AssertionUtil.assertTrue("1 beans defined", lbf.getBeanDefinitionCount() == 1);
        String[] names = lbf.getBeanDefinitionNames();
        AssertionUtil.assertTrue("Array length == 1", names.length == 1);
        AssertionUtil.assertTrue("0th element == test", names[0].equals("test"));
        TestBean tb = (TestBean) lbf.getBean("test");
        AssertionUtil.assertTrue("Test is non null", tb != null);
        AssertionUtil.assertTrue("Test bean name is Tony", "Tony".equals(tb.getName()));
        AssertionUtil.assertTrue("Test bean age is 48", tb.getAge() == 48);
    }

    @Test
    public void testBeanDefinitionOverriding() throws BeansException {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, null));
        lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class, null));
        assertTrue(lbf.getBean("test") instanceof NestedTestBean);
    }

    @Test
    public void testBeanDefinitionOverridingNotAllowed() throws BeansException {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        lbf.setAllowBeanDefinitionOverriding(false);
        lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, null));
        try {
            lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class, null));
            fail("Should have thrown BeanDefinitionStoreException");
        } catch (BeanDefinitionStoreException ex) {
            // expected
        }
    }

    @Test
    public void testBeanReferenceWithNewSyntax() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("r.class", TestBean.class.getName());
        p.setProperty("r.name", "rod");
        p.setProperty("k.class", TestBean.class.getName());
        p.setProperty("k.name", "kerry");
        p.setProperty("k.spouse", "*r");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        TestBean k = (TestBean) lbf.getBean("k");
        TestBean r = (TestBean) lbf.getBean("r");
        assertTrue(k.getSpouse() == r);
    }

    @Test
    public void testCanEscapeBeanReferenceSyntax() {
        String name = "*name";
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("r.class", TestBean.class.getName());
        p.setProperty("r.name", "*" + name);
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        TestBean r = (TestBean) lbf.getBean("r");
        assertTrue(r.getName().equals(name));
    }

    @Test
    public void testCustomEditor() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        NumberFormat nf = NumberFormat.getInstance(Locale.UK);
        lbf.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, true));
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("myFloat", "1.1");
        lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, pvs));
        TestBean testBean = (TestBean) lbf.getBean("testBean");
        System.out.println(testBean.getMyFloat().floatValue());
        assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
    }

    @Test
    public void testRegisterExistingSingletonWithReference() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Properties p = new Properties();
        p.setProperty("test.class", "com.example.spring.simple.ioc.beans.TestBean");
        p.setProperty("test.name", "Tony");
        p.setProperty("test.age", "48");
        p.setProperty("test.spouse(ref)", "singletonObject");
        (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
        Object singletonObject = new TestBean();
        lbf.registerSingleton("singletonObject", singletonObject);
        assertTrue(lbf.isSingleton("singletonObject"));
        TestBean test = (TestBean) lbf.getBean("test");
        assertEquals(singletonObject, lbf.getBean("singletonObject"));
        assertEquals(singletonObject, test.getSpouse());
        Map beansOfType = lbf.getBeansOfType(TestBean.class, false, true);
        assertEquals(2, beansOfType.size());
        assertTrue(beansOfType.containsValue(test));
        assertTrue(beansOfType.containsValue(singletonObject));
    }

    @Test
    public void testRegisterExistingSingletonWithAutowire() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("name", "Tony");
        pvs.addPropertyValue("age", "48");
        RootBeanDefinition bd = new RootBeanDefinition(DependenciesBean.class, pvs, true);
        bd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
        bd.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        lbf.registerBeanDefinition("test", bd);
        Object singletonObject = new TestBean();
        lbf.registerSingleton("singletonObject", singletonObject);
        assertTrue(lbf.containsBean("singletonObject"));
        assertTrue(lbf.isSingleton("singletonObject"));
        assertEquals(0, lbf.getAliases("singletonObject").length);
        DependenciesBean test = (DependenciesBean) lbf.getBean("test");
        assertEquals(singletonObject, lbf.getBean("singletonObject"));
        assertEquals(singletonObject, test.getSpouse());
    }

    @Test
    public void testRegisterExistingSingletonWithAlreadyBound() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        Object singletonObject = new TestBean();
        lbf.registerSingleton("singletonObject", singletonObject);
        try {
            lbf.registerSingleton("singletonObject", singletonObject);
            fail("Should have thrown BeanDefinitionStoreException");
        } catch (BeanDefinitionStoreException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireConstructor() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spouse", bd);
        ConstructorDependenciesBean bean = (ConstructorDependenciesBean) lbf.autowire(ConstructorDependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
        Object spouse = lbf.getBean("spouse");
        assertTrue(bean.getSpouse1() == spouse);
        assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
    }

    @Test
    public void testAutowireBeanByName() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spouse", bd);
        DependenciesBean bean = (DependenciesBean) lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
        TestBean spouse = (TestBean) lbf.getBean("spouse");
        assertEquals(bean.getSpouse(), spouse);
        assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
    }

    @Test
    public void testAutowireBeanByNameWithDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spous", bd);
        try {
            lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireBeanByNameWithNoDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spous", bd);
        DependenciesBean bean = (DependenciesBean) lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        assertNull(bean.getSpouse());
    }

    @Test
    public void testAutowireBeanByType() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("test", bd);
        DependenciesBean bean = (DependenciesBean) lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        TestBean test = (TestBean) lbf.getBean("test");
        assertEquals(bean.getSpouse(), test);
    }

    @Test
    public void testAutowireBeanByTypeWithDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        try {
            lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireBeanByTypeWithNoDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        DependenciesBean bean = (DependenciesBean) lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        assertNull(bean.getSpouse());
    }

    @Test
    public void testAutowireExistingBeanByName() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spouse", bd);
        DependenciesBean existingBean = new DependenciesBean();
        lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
        TestBean spouse = (TestBean) lbf.getBean("spouse");
        assertEquals(existingBean.getSpouse(), spouse);
        assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
    }

    @Test
    public void testAutowireExistingBeanByNameWithDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spous", bd);
        DependenciesBean existingBean = new DependenciesBean();
        try {
            lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireExistingBeanByNameWithNoDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("spous", bd);
        DependenciesBean existingBean = new DependenciesBean();
        lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        assertNull(existingBean.getSpouse());
    }

    @Test
    public void testAutowireExistingBeanByType() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("test", bd);
        DependenciesBean existingBean = new DependenciesBean();
        lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        TestBean test = (TestBean) lbf.getBean("test");
        assertEquals(existingBean.getSpouse(), test);
    }

    @Test
    public void testAutowireExistingBeanByTypeWithDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        DependenciesBean existingBean = new DependenciesBean();
        try {
            lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireExistingBeanByTypeWithNoDependencyCheck() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        DependenciesBean existingBean = new DependenciesBean();
        lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        assertNull(existingBean.getSpouse());
    }

    @Test
    public void testInvalidAutowireMode() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        try {
            lbf.autowireBeanProperties(new TestBean(), 0, false);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testAutowireWithNoDependencies() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
        lbf.registerBeanDefinition("rod", bd);
        assertEquals(1, lbf.getBeanDefinitionCount());
        Object registered = lbf.autowire(NoDependencies.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
        assertEquals(1, lbf.getBeanDefinitionCount());
        assertTrue(registered instanceof NoDependencies);
    }

    @Test
    public void testAutowireWithSatisfiedJavaBeanDependency() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("name", "Rod"));
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
        lbf.registerBeanDefinition("rod", bd);
        assertEquals(1, lbf.getBeanDefinitionCount());
        String name = "kerry";
        // Depends on age, name and spouse (TestBean)
        Object registered = lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
        assertEquals(1, lbf.getBeanDefinitionCount());
        DependenciesBean kerry = (DependenciesBean) registered;
        TestBean rod = (TestBean) lbf.getBean("rod");
        assertEquals(rod, kerry.getSpouse());
    }

    @Test
    public void testAutowireWithSatisfiedConstructorDependency() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("name", "Rod"));
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
        lbf.registerBeanDefinition("rod", bd);
        assertEquals(1, lbf.getBeanDefinitionCount());
        Object registered = lbf.autowire(ConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
        assertEquals(1, lbf.getBeanDefinitionCount());
        ConstructorDependency kerry = (ConstructorDependency) registered;
        TestBean rod = (TestBean) lbf.getBean("rod");
        assertSame(rod, kerry.spouse);
    }

    @Test
    public void testAutowireWithUnsatisfiedConstructorDependency() {
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue(new PropertyValue("name", "Rod"));
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
        lbf.registerBeanDefinition("rod", bd);
        assertEquals(1, lbf.getBeanDefinitionCount());
        try {
            lbf.autowire(UnsatisfiedConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
            fail("Should have unsatisfied constructor dependency on SideEffectBean");
        } catch (UnsatisfiedDependencyException ex) {
            // Ok
        }
    }

    public static class NoDependencies {
    }

    public static class ConstructorDependency {

        public TestBean spouse;

        public ConstructorDependency(TestBean spouse) {
            this.spouse = spouse;
        }
    }

    public static class UnsatisfiedConstructorDependency {

        public UnsatisfiedConstructorDependency(TestBean t, SideEffectBean b) {
        }
    }
}