package com.aw.compute.streams.exceptions;

/**
 * Indicates an exception occurred at the driver level
 *
 *
 */
public class DriverException extends RuntimeException {

	public DriverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DriverException(String message, Throwable cause) {
		super(message, cause);
	}

	public DriverException(String message) {
		super(message);
	}

}
