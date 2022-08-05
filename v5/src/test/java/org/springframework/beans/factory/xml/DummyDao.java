package org.springframework.beans.factory.xml;

import javax.sql.DataSource;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyDao.java,v 1.2 2004-03-18 03:01:16 trisberg Exp $
 */
public class DummyDao {
	
	DataSource ds;

	public DummyDao(DataSource ds) {
		this.ds = ds;
	}

}
