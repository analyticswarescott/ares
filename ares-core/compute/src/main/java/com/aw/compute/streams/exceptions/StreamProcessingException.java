package com.aw.compute.streams.exceptions;

import com.aw.common.exceptions.ProcessingException;

/**
 * An error occurred processing data
 *
 *
 */
public class StreamProcessingException extends ProcessingException {

	private static final long serialVersionUID = 1L;

	public StreamProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StreamProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public StreamProcessingException(String message) {
		super(message);
	}

}
