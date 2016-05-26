package com.aw.common;

/**
 * Data that can be validated in some way. If validation fails, an appropriate exception
 * should be thrown with a meaningful exception message.
 *
 *
 */
public interface Validatable {

	/**
	 * Validate the object.
	 */
	public void validate();

}
