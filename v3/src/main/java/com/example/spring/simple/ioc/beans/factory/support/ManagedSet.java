package com.example.spring.simple.ioc.beans.factory.support;

import java.util.HashSet;

/**
 * Tag subclass used to hold managed Set elements, which may include runtime bean references.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 */
public class ManagedSet extends HashSet {

    public ManagedSet() {
    }

    public ManagedSet(int initialCapacity) {
        super(initialCapacity);
    }

}
