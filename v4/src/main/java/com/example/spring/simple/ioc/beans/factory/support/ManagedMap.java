package com.example.spring.simple.ioc.beans.factory.support;

import java.util.LinkedHashMap;

/**
 * Tag subclass used to hold managed Map values, which may include runtime bean references.
 *
 * <p>Just used on JDK < 1.4, as java.util.LinkedHashMap -
 * which preserves key order - is preferred when it is available.
 *
 * @author Rod Johnson
 * @version $Id: ManagedMap.java,v 1.3 2004-06-02 17:10:48 jhoeller Exp $
 * @see java.util.LinkedHashMap
 * @since 27-May-2003
 */
public class ManagedMap extends LinkedHashMap {

    public ManagedMap() {
    }

    public ManagedMap(int initialCapacity) {
        super(initialCapacity);
    }

}
