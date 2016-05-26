package com.aw.rest.validation;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

/**
 * Provides a simple validation framework.
 */
public interface Validator<T> {

	/**
	 * Validates the given object and throws a web app exception when a validation error occurs.
	 * @param form
	 * @throws WebApplicationException
     */
	void validate(T form) throws BadRequestException;

}
