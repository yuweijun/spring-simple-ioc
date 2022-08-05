package com.example.spring.simple.ioc.beans;

public class Employee extends com.example.spring.simple.ioc.beans.TestBean {
	
	private String co;

	/**
	 * Constructor for Employee.
	 */
	public Employee() {
		super();
	}
	
	public String getCompany() {
		return co;
	}
	
	public void setCompany(String co) {
		this.co = co;
	}

}
