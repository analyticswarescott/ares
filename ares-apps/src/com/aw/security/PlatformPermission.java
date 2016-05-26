package com.aw.security;

/**
 * @author jhaight
 */
public class PlatformPermission {

	private String name;

	public PlatformPermission() { }
	public PlatformPermission(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlatformPermission that = (PlatformPermission) o;

		return name != null ? name.equals(that.name) : that.name == null;

	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
