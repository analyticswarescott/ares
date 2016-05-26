package com.aw.platform.exceptions;

/**
 * An error related to the initialization of the platform occurred.
 *
 *
 *
 */
public class PlatformInitializationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlatformInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PlatformInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlatformInitializationException(String message) {
		super(message);
	}

}
