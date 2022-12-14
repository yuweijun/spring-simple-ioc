package org.springframework.core.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ClassPathResourceTest {

    @Test
    public void testGetFile() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("log4j.properties");
        final String description = classPathResource.getDescription();
        System.out.println(description);
        final File file = classPathResource.getFile();
        System.out.println(file);
        final URL url = classPathResource.getURL();
        System.out.println(url);
    }

    @Test
    public void test() throws IOException {
        final String dtd = "org/springframework/beans/factory/xml/spring-beans.dtd";
        final ClassPathResource classPathResource = new ClassPathResource(dtd);
        System.out.println(classPathResource);
        final File file = classPathResource.getFile();
        System.out.println(file);
    }

}