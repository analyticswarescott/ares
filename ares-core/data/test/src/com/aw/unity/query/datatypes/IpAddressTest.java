package com.aw.unity.query.datatypes;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Basic test of IP address functionality.
 *
 *
 */
public class IpAddressTest {

	@Test
	public void test() throws Exception {

		//test basic ip address equality
		IpAddress ipAddr1 = new IpAddress("1.2.3.4");
		IpAddress ipAddr2 = new IpAddress("2.3.4.5");
		IpAddress ipAddr3 = new IpAddress("1.2.3.4");

		assertEquals("ip equality", ipAddr1, ipAddr3);
		assertFalse("ips should not be equal", ipAddr1.equals(ipAddr2));

		//make sure an invalid ip doesn't parse
		try {
			new IpAddress("blah blah blah");
			fail("invalid IP should have thrown an exception");
		} catch (InvalidFilterException e) {
		}

	}
}
