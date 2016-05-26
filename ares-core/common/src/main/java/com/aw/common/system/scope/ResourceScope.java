package com.aw.common.system.scope;

/**
 * Defines the scope of a resource
 */
public enum ResourceScope {
	TENANT,
	SYSTEM,
	ALL;

	//to/from string, supporting lower case
	public String toString() { return name().toLowerCase(); }
	public static ResourceScope fromString(String str) { return valueOf(str.toUpperCase()); }

}
