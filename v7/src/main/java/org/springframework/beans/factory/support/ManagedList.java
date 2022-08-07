package org.springframework.beans.factory.support;

import java.util.ArrayList;

/**
 * Tag subclass used to hold managed List elements, which may include runtime bean references.
 *
 * @author Rod Johnson
 * @version $Id: ManagedList.java,v 1.4 2004-06-02 17:10:48 jhoeller Exp $
 * @since 27-May-2003
 */
public class ManagedList extends ArrayList {

    public ManagedList() {
    }

    public ManagedList(int initialCapacity) {
        super(initialCapacity);
    }

}
