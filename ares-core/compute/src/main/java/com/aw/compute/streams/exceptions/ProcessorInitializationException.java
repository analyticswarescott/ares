package com.aw.compute.streams.exceptions;

/**
 * An error occurred during driver initialization
 *
 *
 */
public class ProcessorInitializationException extends DriverException {

	private static final long serialVersionUID = 1L;

	public ProcessorInitializationException(String message, Throwable cause, boolean enableSuppression,
											boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessorInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessorInitializationException(String message) {
		super(message);
	}

}
