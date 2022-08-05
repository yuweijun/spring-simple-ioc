package com.example.spring.simple.ioc.beans.factory.xml;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyBoImpl.java,v 1.2 2004-03-18 03:01:16 trisberg Exp $
 */
public class DummyBoImpl implements DummyBo {
	
	DummyDao dao;

	public DummyBoImpl(DummyDao dao) {
		this.dao = dao;
	}
	
	public void something() {
		
	}

}
