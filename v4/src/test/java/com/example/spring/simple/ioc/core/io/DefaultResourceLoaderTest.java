package com.example.spring.simple.ioc.core.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class DefaultResourceLoaderTest {

    @Test
    public void testGetResource() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource("classpath:log4j.properties");
        System.out.println(resource);
        final File file = resource.getFile();
        System.out.println(file);
    }

    @Test
    public void testCurrentDirectory() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource1 = resourceLoader.getResource(".");
        System.out.println(resource1.getFile());

        final Resource resource2 = resourceLoader.getResource("./");
        System.out.println(resource2.getFile());
    }


}