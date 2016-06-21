package com.aw.unity;

/**
 * All known fields in a system.
 *
 *
 *
 */
public interface FieldRepository {

	/**
	 * @param name The name of the field being requested
	 * @return The field for this name. If not found, a FieldNotFoundException will be thrown.
	 */
	public Field getField(String name);

	/**
	 * @return All fields known to this unity instance
	 */
	public Field[] getFields();

	/**
	 * @param name The name of the field to check
	 * @return Whether the repository contains the given field
	 */
	public boolean hasField(String name);
}
