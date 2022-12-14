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

package org.springframework.aop.framework.adapter;

/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry instance.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: GlobalAdvisorAdapterRegistry.java,v 1.5 2004-05-30 15:13:37 jhoeller Exp $
 * @see DefaultAdvisorAdapterRegistry
 */
public abstract class GlobalAdvisorAdapterRegistry {

	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 */
	private static final AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();
	
	/**
	 * Return the singleton DefaultAdvisorAdapterRegistry instance.
	 */
	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}

}
