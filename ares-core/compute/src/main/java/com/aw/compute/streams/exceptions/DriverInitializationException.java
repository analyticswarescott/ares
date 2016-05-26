package com.aw.compute.streams.exceptions;

/**
 * An error occurred during driver initialization
 *
 *
 */
public class DriverInitializationException extends DriverException {

	private static final long serialVersionUID = 1L;

	public DriverInitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DriverInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DriverInitializationException(String message) {
		super(message);
	}

}
