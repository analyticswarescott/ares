package com.aw.unity.exceptions;

/**
 * data source exception
 *
 *
 *
 */
public class DataSourceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataSourceException(String message) {
		super(message);
	}

}
