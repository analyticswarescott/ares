package com.aw.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the logic in the {@link HttpStatusUtils} utility class.
 */
public class HttpStatusUtilsTest {

	@Test
	public void testIsSuccessful() throws Exception {

		for (int i = 0; i < 200; i++) {
			assertFalse(HttpStatusUtils.isSuccessful(i));
		}

		// Only statuses in the 200 block should be considered successful
		for (int i = 200; i < 300; i++) {
			assertTrue(HttpStatusUtils.isSuccessful(i));
		}

		for (int i = 300; i < 600; i++) {
			assertFalse(HttpStatusUtils.isSuccessful(i));
		}

	}
}