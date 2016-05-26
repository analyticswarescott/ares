package com.aw.common.exceptions;

/**
 * Invalid configuration exception
 *
 *
 *
 */
public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(String message) {
		super(message);
	}

}
