package org.springframework.beans.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.AssertionUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class ListableBeanFactoryTest extends BeanFactoryTest {

    protected ListableBeanFactory getListableBeanFactory() {
        BeanFactory bf = getBeanFactory();
        if (!(bf instanceof ListableBeanFactory)) {
            throw new RuntimeException("ListableBeanFactory required");
        }
        return (ListableBeanFactory) bf;
    }

    @Test
    protected final void assertCount() {
        String[] defnames = getListableBeanFactory().getBeanDefinitionNames();
        AssertionUtil.assertTrue("We should have " + defnames.length + " beans", defnames.length >= 13);
    }

    @Test
    public void testTestBeanCount() {
        assertTestBeanCount(7);
    }

    public void assertTestBeanCount(int count) {
        String[] defnames = getListableBeanFactory().getBeanDefinitionNames(TestBean.class);
        AssertionUtil.assertTrue("We should have " + count + " beans for class org.springframework.beans.TestBean, not " +
            defnames.length, defnames.length == count);
    }

    @Test
    public void testGetDefinitionsForNoSuchClass() {
        String[] defnames = getListableBeanFactory().getBeanDefinitionNames(String.class);
        AssertionUtil.assertTrue("No string definitions", defnames.length == 0);
    }

    /**
     * Check that count refers to factory class, not bean class (we don't know what type factories may return, and it may even change over time).
     */
    @Test
    public void testGetCountForFactoryClass() {
        AssertionUtil.assertTrue("Should have 2 factories, not " + getListableBeanFactory().getBeanDefinitionNames(FactoryBean.class).length,
            getListableBeanFactory().getBeanDefinitionNames(FactoryBean.class).length == 2);
    }

    @Test
    public void testContainsBeanDefinition() {
        assertTrue(getListableBeanFactory().containsBeanDefinition("rod"));
        assertTrue(getListableBeanFactory().containsBeanDefinition("roderick"));
    }

}
