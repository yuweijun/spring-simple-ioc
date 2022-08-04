/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spring.simple.ioc.beans.factory.support;

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
