package org.springframework.beans.factory.xml;

import org.springframework.tests.sample.beans.TestBean;

/**
 * Simple bean used to check dependency checking
 * @author Rod Johnson
 * @since 04-Sep-2003
 * @version $Id: DependenciesBean.java,v 1.2 2004-03-18 03:01:16 trisberg Exp $
 */
public class DependenciesBean {
	
	private int age;
	
	private String name;
	
	private TestBean spouse;
	
	//private Dependencies recursive;

	/**
	 * @return int
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return TestBean
	 */
	public TestBean getSpouse() {
		return spouse;
	}

	/**
	 * Sets the age.
	 * @param age The age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the spouse.
	 * @param spouse The spouse to set
	 */
	public void setSpouse(TestBean spouse) {
		this.spouse = spouse;
	}

}
