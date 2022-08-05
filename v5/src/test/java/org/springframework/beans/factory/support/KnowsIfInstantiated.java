package org.springframework.beans.factory.support;

public class KnowsIfInstantiated {
	
	private static boolean instantiated;
	
	public static void clearInstantiationRecord() {
		instantiated = false;
	}
	
	public static boolean wasInstantiated() {
		return instantiated;
	}

	/**
	 * Constructor for KnowsIfLoaded.
	 */
	public KnowsIfInstantiated() {
		instantiated = true;
	}

}
