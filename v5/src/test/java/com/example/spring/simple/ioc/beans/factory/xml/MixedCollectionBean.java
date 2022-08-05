package com.example.spring.simple.ioc.beans.factory.xml;

import java.util.Collection;

/**
 * Bean that exposes a simple property that can be set
 * to a mix of references and individual values
 * @author Rod Johnson
 * @since 27-May-2003
 * @version $Id: MixedCollectionBean.java,v 1.4 2004-03-18 03:01:16 trisberg Exp $
 */
public class MixedCollectionBean {

	protected static int nrOfInstances = 0;
	
	public static void resetStaticState() {
		nrOfInstances = 0;
	}

	private Collection jumble;

	public MixedCollectionBean() {
		nrOfInstances++;
	}

	/**
	 * @return Collection
	 */
	public Collection getJumble() {
		return jumble;
	}

	/**
	 * Sets the jumble.
	 * @param jumble The jumble to set
	 */
	public void setJumble(Collection jumble) {
		this.jumble = jumble;
	}

}
