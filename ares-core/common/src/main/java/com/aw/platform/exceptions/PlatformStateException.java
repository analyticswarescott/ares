package com.aw.platform.exceptions;

/**
 * If a user of an api makes an invalid method call of some kind
 *
 *
 */
public class PlatformStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlatformStateException(String message, Throwable cause, boolean enableSuppression,
								  boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PlatformStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlatformStateException(String message) {
		super(message);
	}

}
