package com.aw.unity.query.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for a CIDR value.
 *
 *
 *
 */
public class CIDRTest {

	@Test
	public void match() throws Exception {

		CIDR cidr = new CIDR("1.2.3.4/16");

		//check on either side and in the cidr
		assertFalse("cidr should not contain 1.1.100.200", cidr.contains(new IpAddress("1.1.100.200")));
		assertTrue("cidr should contain 1.2.80.100", cidr.contains(new IpAddress("1.2.80.100")));
		assertFalse("cidr should not contain 1.3.3.4", cidr.contains(new IpAddress("1.3.3.4")));

	}

}
