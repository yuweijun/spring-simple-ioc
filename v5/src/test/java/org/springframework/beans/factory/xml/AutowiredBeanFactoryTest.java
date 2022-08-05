package org.springframework.beans.factory.xml;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class AutowiredBeanFactoryTest {

    @Test
    public void testAutowire() throws Exception {
        final ClassPathResource resource = new ClassPathResource("autowire.xml", getClass());
        System.out.println(resource);
        final File file = resource.getFile();
        System.out.println(file);

        XmlBeanFactory xbf = new XmlBeanFactory(resource);
        TestBean spouse = new TestBean("kerry", 0);
        xbf.registerSingleton("spouse", spouse);
        doTestAutowire(xbf);
    }

    @Test
    public void testAutowireWithParent() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("autowire.xml", getClass()));
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("name", "kerry");
        lbf.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class, pvs));
        xbf.setParentBeanFactory(lbf);
        doTestAutowire(xbf);
    }

    private void doTestAutowire(XmlBeanFactory xbf) throws Exception {
        DependenciesBean rod1 = (DependenciesBean) xbf.getBean("rod1");
        TestBean kerry = (TestBean) xbf.getBean("spouse");
        // Should have been autowired
        assertEquals(kerry, rod1.getSpouse());

        DependenciesBean rod1a = (DependenciesBean) xbf.getBean("rod1a");
        // Should have been autowired
        assertEquals(kerry, rod1a.getSpouse());

        DependenciesBean rod2 = (DependenciesBean) xbf.getBean("rod2");
        // Should have been autowired
        assertEquals(kerry, rod2.getSpouse());

        ConstructorDependenciesBean rod3 = (ConstructorDependenciesBean) xbf.getBean("rod3");
        IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
        // Should have been autowired
        assertEquals(kerry, rod3.getSpouse1());
        assertEquals(kerry, rod3.getSpouse2());
        assertEquals(other, rod3.getOther());

        ConstructorDependenciesBean rod3a = (ConstructorDependenciesBean) xbf.getBean("rod3a");
        // Should have been autowired
        assertEquals(kerry, rod3a.getSpouse1());
        assertEquals(kerry, rod3a.getSpouse2());
        assertEquals(other, rod3a.getOther());

        try {
            ConstructorDependenciesBean rod4 = (ConstructorDependenciesBean) xbf.getBean("rod4");
            fail("Should not have thrown FatalBeanException");
        } catch (FatalBeanException ex) {
            // expected
        }

        DependenciesBean rod5 = (DependenciesBean) xbf.getBean("rod5");
        // Should not have been autowired
        assertNull(rod5.getSpouse());

        BeanFactory appCtx = (BeanFactory) xbf.getBean("childAppCtx");
        assertTrue(appCtx.getBean("rod1") != null);
        assertTrue(appCtx.getBean("dependingBean") != null);
        assertTrue(appCtx.getBean("jenny") != null);
    }
}
