package com.aw.platform.nodes.exceptions;

/**
 * If a user of an api makes an invalid method call of some kind
 *
 *
 */
public class NodeRoleAlreadyRunningException extends RuntimeException {

	public NodeRoleAlreadyRunningException(String message, Throwable cause, boolean enableSuppression,
										   boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeRoleAlreadyRunningException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeRoleAlreadyRunningException(String message) {
		super(message);
	}

}
