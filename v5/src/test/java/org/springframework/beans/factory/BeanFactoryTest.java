package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MustBeInitialized;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.AssertionUtil;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactoryTest;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Subclasses must implement setUp() to initialize bean factory and any other variables they need
 *
 * @author Rod Johnson
 * @version $Id: AbstractBeanFactoryTests.java,v 1.11 2004-03-18 03:01:20 trisberg Exp $
 */
public class BeanFactoryTest {

    private DefaultListableBeanFactory parent;

    private XmlBeanFactory factory;

    protected BeanFactory getBeanFactory() {
        return factory;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        parent = new DefaultListableBeanFactory();
        Map m = new HashMap();
        m.put("name", "Albert");
        parent.registerBeanDefinition("father", new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));
        m = new HashMap();
        m.put("name", "Roderick");
        parent.registerBeanDefinition("rod", new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));

        // Load from classpath, NOT a file path
        this.factory = new XmlBeanFactory(new ClassPathResource("test.xml", XmlBeanFactoryTest.class), parent);
        this.factory.addBeanPostProcessor(new BeanPostProcessor() {
            public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
                if (bean instanceof TestBean) {
                    ((TestBean) bean).setPostProcessed(true);
                }
                if (bean instanceof DummyFactory) {
                    ((DummyFactory) bean).setPostProcessed(true);
                }
                return bean;
            }

            public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
                return bean;
            }
        });
        this.factory.addBeanPostProcessor(new LifecycleBean.PostProcessor());
        this.factory.preInstantiateSingletons();
    }

    /**
     * Roderick beans inherits from rod, overriding name only
     */
    @Test
    public void testInheritance() {
        assertTrue(getBeanFactory().containsBean("rod"));
        assertTrue(getBeanFactory().containsBean("roderick"));
        TestBean rod = (TestBean) getBeanFactory().getBean("rod");
        TestBean roderick = (TestBean) getBeanFactory().getBean("roderick");
        AssertionUtil.assertTrue("not == ", rod != roderick);
        AssertionUtil.assertTrue("rod.name is Rod", rod.getName().equals("Rod"));
        AssertionUtil.assertTrue("rod.age is 31", rod.getAge() == 31);
        AssertionUtil.assertTrue("roderick.name is Roderick", roderick.getName().equals("Roderick"));
        AssertionUtil.assertTrue("roderick.age was inherited", roderick.getAge() == rod.getAge());
    }

    @Test
    public void testGetNull() {
        try {
            getBeanFactory().getBean(null);
            fail("Can't get null bean");
        } catch (NoSuchBeanDefinitionException ex) {
            // OK
        }
    }

    /**
     * Test that InitializingBean objects receive the afterPropertiesSet() callback
     */
    @Test
    public void testInitializingBeanCallback() {
        MustBeInitialized mbi = (MustBeInitialized) getBeanFactory().getBean("mustBeInitialized");
        // The dummy business method will throw an exception if the
        // afterPropertiesSet() callback wasn't invoked
        mbi.businessMethod();
    }

    /**
     * Test that InitializingBean/BeanFactoryAware/DisposableBean objects receive the afterPropertiesSet() callback before BeanFactoryAware callbacks
     */
    @Test
    public void testLifecycleCallbacks() {
        LifecycleBean lb = (LifecycleBean) getBeanFactory().getBean("lifecycle");
        assertEquals("lifecycle", lb.getBeanName());
        // The dummy business method will throw an exception if the
        // necessary callbacks weren't invoked in the right order
        lb.businessMethod();
        AssertionUtil.assertTrue("Not destroyed", !lb.isDestroyed());
    }

    @Test
    public void testFindsValidInstance() {
        try {
            Object o = getBeanFactory().getBean("rod");
            AssertionUtil.assertTrue("Rod bean is a TestBean", o instanceof TestBean);
            TestBean rod = (TestBean) o;
            AssertionUtil.assertTrue("rod.name is Rod", rod.getName().equals("Rod"));
            AssertionUtil.assertTrue("rod.age is 31", rod.getAge() == 31);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance");
        }
    }

    @Test
    public void testGetInstanceByMatchingClass() {
        try {
            Object o = getBeanFactory().getBean("rod", TestBean.class);
            AssertionUtil.assertTrue("Rod bean is a TestBean", o instanceof TestBean);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance with matching class");
        }
    }

    @Test
    public void testGetInstanceByNonmatchingClass() {
        try {
            Object o = getBeanFactory().getBean("rod", BeanFactory.class);
            fail("Rod bean is not of type BeanFactory; getBeanInstance(rod, BeanFactory.class) should throw BeanNotOfRequiredTypeException");
        } catch (BeanNotOfRequiredTypeException ex) {
            // So far, so good
            AssertionUtil.assertTrue("Exception has correct bean name", ex.getBeanName().equals("rod"));
            AssertionUtil.assertTrue("Exception requiredType must be BeanFactory.class", ex.getRequiredType().equals(BeanFactory.class));
            AssertionUtil.assertTrue("Exception actualType as TestBean.class", TestBean.class.isAssignableFrom(ex.getActualType()));
            AssertionUtil.assertTrue("Actual instance is correct", ex.getActualInstance() == getBeanFactory().getBean("rod"));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance");
        }
    }

    @Test
    public void testGetSharedInstanceByMatchingClass() {
        try {
            Object o = getBeanFactory().getBean("rod", TestBean.class);
            AssertionUtil.assertTrue("Rod bean is a TestBean", o instanceof TestBean);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance with matching class");
        }
    }

    @Test
    public void testGetSharedInstanceByMatchingClassNoCatch() {
        Object o = getBeanFactory().getBean("rod", TestBean.class);
        AssertionUtil.assertTrue("Rod bean is a TestBean", o instanceof TestBean);
    }

    @Test
    public void testGetSharedInstanceByNonmatchingClass() {
        try {
            Object o = getBeanFactory().getBean("rod", BeanFactory.class);
            fail("Rod bean is not of type BeanFactory; getBeanInstance(rod, BeanFactory.class) should throw BeanNotOfRequiredTypeException");
        } catch (BeanNotOfRequiredTypeException ex) {
            // So far, so good
            AssertionUtil.assertTrue("Exception has correct bean name", ex.getBeanName().equals("rod"));
            AssertionUtil.assertTrue("Exception requiredType must be BeanFactory.class", ex.getRequiredType().equals(BeanFactory.class));
            AssertionUtil.assertTrue("Exception actualType as TestBean.class", TestBean.class.isAssignableFrom(ex.getActualType()));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance");
        }
    }

    @Test
    public void testSharedInstancesAreEqual() {
        try {
            Object o = getBeanFactory().getBean("rod");
            AssertionUtil.assertTrue("Rod bean1 is a TestBean", o instanceof TestBean);
            Object o1 = getBeanFactory().getBean("rod");
            AssertionUtil.assertTrue("Rod bean2 is a TestBean", o1 instanceof TestBean);
            AssertionUtil.assertTrue("Object equals applies", o == o1);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on getting valid instance");
        }
    }

    @Test
    public void testNotThere() {
        assertFalse(getBeanFactory().containsBean("Mr Squiggle"));
        try {
            Object o = getBeanFactory().getBean("Mr Squiggle");
            fail("Can't find missing bean");
        } catch (BeansException ex) {
            //ex.printStackTrace();
            //fail("Shouldn't throw exception on getting valid instance");
        }
    }

    @Test
    public void testValidEmpty() {
        try {
            Object o = getBeanFactory().getBean("validEmpty");
            AssertionUtil.assertTrue("validEmpty bean is a TestBean", o instanceof TestBean);
            TestBean ve = (TestBean) o;
            AssertionUtil.assertTrue("Valid empty has defaults", ve.getName() == null && ve.getAge() == 0 && ve.getSpouse() == null);
        } catch (BeansException ex) {
            ex.printStackTrace();
            fail("Shouldn't throw exception on valid empty");
        }
    }

    @Test
    public void testTypeMismatch() {
        try {
            Object o = getBeanFactory().getBean("typeMismatch");
            fail("Shouldn't succeed with type mismatch");
        } catch (BeanCreationException wex) {
            assertTrue(wex.getCause() instanceof PropertyAccessExceptionsException);
            PropertyAccessExceptionsException ex = (PropertyAccessExceptionsException) wex.getCause();
            // Further tests
            AssertionUtil.assertTrue("Has one error ", ex.getExceptionCount() == 1);
            AssertionUtil.assertTrue("Error is for field age", ex.getPropertyAccessException("age") != null);

            TestBean tb = (TestBean) ex.getBeanWrapper().getWrappedInstance();
            AssertionUtil.assertTrue("Age still has default", tb.getAge() == 0);
            AssertionUtil.assertTrue("We have rejected age in exception", ex.getPropertyAccessException("age").getPropertyChangeEvent().getNewValue().equals("34x"));
            AssertionUtil.assertTrue("valid name stuck", tb.getName().equals("typeMismatch"));
            AssertionUtil.assertTrue("valid spouse stuck", tb.getSpouse().getName().equals("Rod"));
        }
    }

    @Test
    public void testGrandparentDefinitionFoundInBeanFactory() throws Exception {
        TestBean dad = (TestBean) getBeanFactory().getBean("father");
        AssertionUtil.assertTrue("Dad has correct name", dad.getName().equals("Albert"));
    }

    @Test
    public void testFactorySingleton() throws Exception {
        assertTrue(getBeanFactory().isSingleton("&singletonFactory"));
        assertTrue(getBeanFactory().isSingleton("singletonFactory"));
        TestBean tb = (TestBean) getBeanFactory().getBean("singletonFactory");
        AssertionUtil.assertTrue("Singleton from factory has correct name, not " + tb.getName(), tb.getName().equals(DummyFactory.SINGLETON_NAME));
        DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
        TestBean tb2 = (TestBean) getBeanFactory().getBean("singletonFactory");
        AssertionUtil.assertTrue("Singleton references ==", tb == tb2);
        AssertionUtil.assertTrue("FactoryBean is BeanFactoryAware", factory.getBeanFactory() != null);
    }

    @Test
    public void testFactoryPrototype() throws Exception {
        assertTrue(getBeanFactory().isSingleton("&prototypeFactory"));
        assertFalse(getBeanFactory().isSingleton("prototypeFactory"));
        TestBean tb = (TestBean) getBeanFactory().getBean("prototypeFactory");
        assertTrue(!tb.getName().equals(DummyFactory.SINGLETON_NAME));
        TestBean tb2 = (TestBean) getBeanFactory().getBean("prototypeFactory");
        AssertionUtil.assertTrue("Prototype references !=", tb != tb2);
    }

    /**
     * Check that we can get the factory bean itself. This is only possible if we're dealing with a factory
     *
     * @throws Exception
     */
    @Test
    public void testGetFactoryItself() throws Exception {
        DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
        assertTrue(factory != null);
    }

    /**
     * Check that afterPropertiesSet gets called on factory
     *
     * @throws Exception
     */
    @Test
    public void testFactoryIsInitialized() throws Exception {
        TestBean tb = (TestBean) getBeanFactory().getBean("singletonFactory");
        DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
        AssertionUtil.assertTrue("Factory was initialized because it implemented InitializingBean", factory.wasInitialized());
    }

    /**
     * It should be illegal to dereference a normal bean as a factory
     */
    @Test
    public void testRejectsFactoryGetOnNormalBean() {
        try {
            getBeanFactory().getBean("&rod");
            fail("Shouldn't permit factory get on normal bean");
        } catch (BeanIsNotAFactoryException ex) {
            // Ok
        }
    }

    @Test
    public void testAliasing() {
        if (!(getBeanFactory() instanceof AbstractBeanFactory))
            return;

        String alias = "rods alias";
        try {
            getBeanFactory().getBean(alias);
            fail("Shouldn't permit factory get on normal bean");
        } catch (NoSuchBeanDefinitionException ex) {
            // Ok
            assertTrue(alias.equals(ex.getBeanName()));
        }

        // Create alias
        ((AbstractBeanFactory) getBeanFactory()).registerAlias("rod", alias);
        Object rod = getBeanFactory().getBean("rod");
        Object aliasRod = getBeanFactory().getBean(alias);
        assertTrue(rod == aliasRod);

        try {
            ((AbstractBeanFactory) getBeanFactory()).registerAlias("father", alias);
            fail("Should have thrown FatalBeanException");
        } catch (FatalBeanException ex) {
            // expected
        }
    }

    public static class TestBeanEditor extends PropertyEditorSupport {

        public void setAsText(String text) {
            TestBean tb = new TestBean();
            StringTokenizer st = new StringTokenizer(text, "_");
            tb.setName(st.nextToken());
            tb.setAge(Integer.parseInt(st.nextToken()));
            setValue(tb);
        }
    }

}
