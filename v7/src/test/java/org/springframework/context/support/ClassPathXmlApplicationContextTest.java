package org.springframework.context.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.ResourceTestBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Juergen Hoeller
 */
public class ClassPathXmlApplicationContextTest {

    @Test
    public void testResourceAndInputStream() throws IOException {
        // ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/org/springframework/beans/factory/xml/resource.xml") {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/org/springframework/beans/factory/**/resource.xml") {
            @Override
            public Resource getResource(String location) {
                if ("classpath:org/springframework/beans/factory/xml/test.properties".equals(location)) {
                    return new ClassPathResource("test.properties", ClassPathXmlApplicationContextTest.class);
                }
                return super.getResource(location);
            }
        };

        ResourceTestBean resource1 = (ResourceTestBean) ctx.getBean("resource1");
        ResourceTestBean resource2 = (ResourceTestBean) ctx.getBean("resource2");
        assertTrue(resource1.getResource() instanceof ClassPathResource);
        StringWriter writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource1.getResource().getInputStream()), writer);
        assertEquals("contexttest", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource1.getInputStream()), writer);
        assertEquals("contexttest", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource2.getResource().getInputStream()), writer);
        assertEquals("contexttest", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource2.getInputStream()), writer);
        assertEquals("contexttest", writer.toString());
    }

}
