package com.aw.platform.nodes.exceptions;

/**
 * If a user of an api makes an invalid method call of some kind
 *
 *
 */
public class NodeRoleException extends RuntimeException {

	public NodeRoleException(String message, Throwable cause, boolean enableSuppression,
							 boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeRoleException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeRoleException(String message) {
		super(message);
	}

}
