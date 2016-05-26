package com.aw.compute.referencedata.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeoLocationTest {

	@Test
	public void testHashcode() {

		GeoLocation loc1 = GeoLocation.forLocation(1, 2);
		GeoLocation loc2 = GeoLocation.forLocation(2, 3);
		GeoLocation loc3 = GeoLocation.forLocation(1, 2);

		assertNotEquals(loc1.hashCode(), loc2.hashCode());
		assertEquals(loc1.hashCode(), loc3.hashCode());

	}

	@Test
	public void testEquals() {

		GeoLocation loc1 = GeoLocation.forLocation(1, 2);
		GeoLocation loc2 = null;
		GeoLocation loc3 = GeoLocation.forLocation(1, 3);
		GeoLocation loc4 = GeoLocation.forLocation(2, 3);
		GeoLocation loc5 = GeoLocation.forLocation(1, 2);

		assertFalse(loc1.equals(loc2));
		assertFalse(loc1.equals(loc3));
		assertFalse(loc1.equals(loc4));
		assertTrue(loc1.equals(loc5));

	}

}
