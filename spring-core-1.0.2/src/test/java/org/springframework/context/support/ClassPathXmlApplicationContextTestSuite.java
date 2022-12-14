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

package org.springframework.context.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.springframework.beans.ResourceTestBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * @author Juergen Hoeller
 */
public class ClassPathXmlApplicationContextTestSuite extends TestCase {

	public void testResourceAndInputStream() throws IOException {
		ClassPathXmlApplicationContext ctx =
		    new ClassPathXmlApplicationContext("/org/springframework/beans/factory/xml/resource.xml") {
			public Resource getResource(String location) {
				if ("classpath:org/springframework/beans/factory/xml/test.properties".equals(location)) {
					return new ClassPathResource("test.properties", ClassPathXmlApplicationContextTestSuite.class);
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
