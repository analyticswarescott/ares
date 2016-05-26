package com.aw.unity.query.datatypes;

/**
 * If a value can be considered space of one or more ip addresses, it is "addressable".
 *
 *
 *
 */
public interface Addressable extends Comparable<Addressable> {

	/**
	 * Is the given IP address in this addressable space?
	 *
	 * @param ip
	 * @return
	 */
	public boolean contains(IpAddress ip);

}
