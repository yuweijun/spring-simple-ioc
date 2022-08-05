

package com.example.spring.simple.ioc.beans.factory;

import com.example.spring.simple.ioc.beans.factory.xml.XmlBeanFactory;
import com.example.spring.simple.ioc.beans.propertyeditors.CustomDateEditor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Guillaume Poirier
 * @author Juergen Hoeller
 * @since 10.03.2004
 */
public class ConcurrentBeanFactoryTest {

    private static final Log logger = LogFactory.getLog(ConcurrentBeanFactoryTest.class);

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");

    private static final Date date1;

    private static final Date date2;

    static {
        try {
            date1 = df.parse("2004/08/08");
            date2 = df.parse("2000/02/02");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private BeanFactory factory;

    private Set set = Collections.synchronizedSet(new HashSet());

    private Throwable ex = null;

    @BeforeEach
    protected void setUp() throws Exception {
        XmlBeanFactory factory = new XmlBeanFactory(getClass().getResourceAsStream("concurrent.xml"));
        CustomDateEditor editor = new CustomDateEditor(df, false);
        factory.registerCustomEditor(Date.class, editor);
        this.factory = factory;
    }

    @Test
    public void testSingleThread() {
        for (int i = 0; i < 100; i++) {
            performTest();
        }
    }

    @Test
    public void testConcurrent() {
        for (int i = 0; i < 30; i++) {
            TestRun run = new TestRun();
            set.add(run);
            Thread t = new Thread(run);
            t.setDaemon(true);
            t.start();
        }
        logger.info("Thread creation over, " + set.size() + " still active.");
        synchronized (set) {
            while (!set.isEmpty() && ex == null) {
                try {
                    set.wait();
                } catch (InterruptedException e) {
                    logger.info(e.toString());
                }
                logger.info(set.size() + " threads still active.");
            }
        }
        if (ex != null) {
            fail(ex.getMessage());
        }
    }

    private class TestRun implements Runnable {

        @Test
        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    performTest();
                }
            } catch (Throwable e) {
                ex = e;
            } finally {
                synchronized (set) {
                    set.remove(this);
                    set.notifyAll();
                }
            }
        }

    }

    private void performTest() {

        ConcurrentBean b1 = (ConcurrentBean) factory.getBean("bean1");
        ConcurrentBean b2 = (ConcurrentBean) factory.getBean("bean2");

        assertEquals(b1.getDate(), date1);
        assertEquals(b2.getDate(), date2);
    }

    public static class ConcurrentBean {

        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

}
