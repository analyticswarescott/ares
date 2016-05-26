package com.aw.common.exceptions;

/**
 * Runtime issue during initialization
 *
 *
 *
 */
public class InitializationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitializationException(String message) {
		super(message);
	}

}
