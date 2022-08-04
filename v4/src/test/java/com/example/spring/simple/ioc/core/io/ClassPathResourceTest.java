package com.example.spring.simple.ioc.core.io;

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

}