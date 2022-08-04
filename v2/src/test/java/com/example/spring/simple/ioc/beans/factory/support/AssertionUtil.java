package com.example.spring.simple.ioc.beans.factory.support;

import static org.junit.jupiter.api.Assertions.fail;

public class AssertionUtil {

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

}
