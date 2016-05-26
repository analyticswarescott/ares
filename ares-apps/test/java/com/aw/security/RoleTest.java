package com.aw.security;

import com.aw.common.util.JSONUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jhaight
 */
public class RoleTest {

	@Test
	public void testHasPermissions() {
		Role role = getDefaultRole();
		assertTrue(role.hasPermission("earth"));
		assertTrue(role.hasPermission("fire"));
		assertTrue(role.hasPermission("wind"));
		assertTrue(role.hasPermission("water"));
		assertTrue(role.hasPermission("heart"));
		assertTrue(role.hasPermission(new PlatformPermission("earth")));
		assertTrue(role.hasPermission(new PlatformPermission("fire")));
		assertTrue(role.hasPermission(new PlatformPermission("wind")));
		assertTrue(role.hasPermission(new PlatformPermission("water")));
		assertTrue(role.hasPermission(new PlatformPermission("heart")));
		assertFalse(role.hasPermission("dirt"));
		assertFalse(role.hasPermission("heat"));
		assertFalse(role.hasPermission("air"));
		assertFalse(role.hasPermission("h20"));
		assertFalse(role.hasPermission("love"));
		assertFalse(role.hasPermission(new PlatformPermission("dirt")));
		assertFalse(role.hasPermission(new PlatformPermission("heat")));
		assertFalse(role.hasPermission(new PlatformPermission("air")));
		assertFalse(role.hasPermission(new PlatformPermission("h20")));
		assertFalse(role.hasPermission(new PlatformPermission("love")));
	}

	@Test
	public void testGetAndSetPermission() {
		Role role = getDefaultRole();
		List<PlatformPermission> permissions = new ArrayList<>();
		String[] permissionNames = { "earth", "fire", "wind", "water", "heart" };
		for (String permissionName : permissionNames) {
			permissions.add(new PlatformPermission(permissionName));
		}
		assertNotEquals(permissions, role.getPermissions());
		assertNotEquals(permissions, role.getPermissionsAsSet());
		role.setPermissions(permissions);
		assertNotEquals(permissions, role.getPermissions());
		assertNotEquals(permissions, role.getPermissionsAsSet());
	}

	@Test
	public void testEquals() {
		Role role1 = new Role();
		Role role2 = new Role();
		LinkedHashSet<PlatformPermission> permissions = new LinkedHashSet<>();
		String[] permissionNames = { "earth", "fire", "wind", "water", "heart" };
		for (String permissionName : permissionNames) {
			permissions.add(new PlatformPermission(permissionName));
		}
		role1.setPermissions(permissions);
		for (String permissionName : permissionNames) {
			role2.addPermission(new PlatformPermission(permissionName));
		}
		assertEquals(role1, role2);
		assertEquals(role1.hashCode(), role2.hashCode());
		role2.addPermission(new PlatformPermission("I am captain planet"));
		assertNotEquals(role1, role2);
		assertNotEquals(role1.hashCode(), role2.hashCode());
	}

	@Test
	public void testSerializeAndDeserialize() throws Exception {
		Role role = getDefaultRole();
		String roleJson = JSONUtils.objectToString(role);
		Role duplicateRole = JSONUtils.objectFromString(roleJson, Role.class);
		assertEquals(role, duplicateRole);
	}

	private Role getDefaultRole() {
		Role role = new Role();
		String[] permissionNames = { "earth", "fire", "wind", "water", "heart" };
		for (String permissionName : permissionNames) {
			role.addPermission(new PlatformPermission(permissionName));
		}
		return role;
	}
}
