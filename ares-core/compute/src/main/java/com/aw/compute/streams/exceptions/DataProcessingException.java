package com.aw.compute.streams.exceptions;

/**
 * An exception occurred processing unity data.
 *
 *
 */
public class DataProcessingException extends StreamProcessingException {

	private static final long serialVersionUID = 1L;

	public DataProcessingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataProcessingException(String message) {
		super(message);
	}

}
