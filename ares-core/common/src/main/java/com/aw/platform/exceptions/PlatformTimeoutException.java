package com.aw.platform.exceptions;

/**
 * A platform timeout has occurred
 *
 *
 *
 */
public class PlatformTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlatformTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PlatformTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlatformTimeoutException(String message) {
		super(message);
	}

}
