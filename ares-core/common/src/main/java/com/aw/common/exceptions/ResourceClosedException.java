package com.aw.common.exceptions;

import java.io.IOException;

/**
 * A resource is closed before the operation was called on it, resulting in this exception
 *
 *
 *
 */
public class ResourceClosedException extends IOException {

	private static final long serialVersionUID = 1L;

	public ResourceClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceClosedException(String message) {
		super(message);
	}

}
