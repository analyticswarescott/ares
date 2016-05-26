package com.aw.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * @author jhaight
 */
public class PlatformPermissionTest {
	@Test
	public void testEquals() {
		String name = "I have the power!";
		PlatformPermission permission1 = new PlatformPermission(name);
		PlatformPermission permission2 = new PlatformPermission();
		permission2.setName(name);
		assertEquals(permission1, permission2);
		assertEquals(permission1.hashCode(), permission2.hashCode());
		permission1.setName("My power fades...");
		assertNotEquals(permission1, permission2);
		assertNotEquals(permission1.hashCode(), permission2.hashCode());
	}

	@Test
	public void testSetAndGetName() {
		PlatformPermission permission = new PlatformPermission();
		assertNull(permission.getName());
		String name = "With all your power combined...";
		permission.setName(name);
		assertEquals(name, permission.getName());
	}
}
