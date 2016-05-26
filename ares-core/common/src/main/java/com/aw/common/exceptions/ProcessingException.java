package com.aw.common.exceptions;

/**
 * General processing exception
 *
 *
 *
 */
public class ProcessingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingException(String message) {
		super(message);
	}

}
