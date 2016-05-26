package com.aw.platform.exceptions;

/**
 * An error occurred during a node operation.
 *
 *
 *
 */
public class NodeOperationException extends Exception {

	private static final long serialVersionUID = 1L;

	public NodeOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeOperationException(String message) {
		super(message);
	}

}
