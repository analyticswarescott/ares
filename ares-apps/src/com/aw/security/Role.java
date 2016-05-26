package com.aw.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jhaight
 */
public class Role {

	private Set<PlatformPermission> permissions = new HashSet<>();

	public Collection<PlatformPermission> getPermissions() {
		return permissions;
	}

	public Set<PlatformPermission> getPermissionsAsSet() {
		return permissions;
	}

	public void setPermissions(Collection<PlatformPermission> permissions) {
		if (permissions instanceof Set) {
			this.permissions = (Set<PlatformPermission>)permissions;
		} else {
			this.permissions = new HashSet<>();
			this.permissions.addAll(permissions);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Role role = (Role) o;

		return permissions != null ? permissions.equals(role.permissions) : role.permissions == null;

	}

	@Override
	public int hashCode() {
		return permissions != null ? permissions.hashCode() : 0;
	}

	public boolean hasPermission(PlatformPermission permission) {
		return permissions.contains(permission);
	}

	public boolean hasPermission(String permissionName) {
		return hasPermission(new PlatformPermission(permissionName));
	}

	public boolean addPermission(PlatformPermission permission) {
		return permissions.add(permission);
	}
}
