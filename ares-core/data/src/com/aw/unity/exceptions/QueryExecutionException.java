package com.aw.unity.exceptions;

public class QueryExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public QueryExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public QueryExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryExecutionException(String message) {
		super(message);
	}

}
