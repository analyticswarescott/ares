package com.aw.platform.exceptions;

/**
 * Node was not found.
 *
 *
 *
 */
public class NodeNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeNotFoundException(String message) {
		super(message);
	}

}
