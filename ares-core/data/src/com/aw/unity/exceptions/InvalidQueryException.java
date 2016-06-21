package com.aw.unity.exceptions;

public class InvalidQueryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidQueryException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidQueryException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidQueryException(String message) {
		super(message);
	}

}
