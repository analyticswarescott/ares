package com.aw.common.cluster.db.exceptions;

/**
 * A lock that was requested wasn't found
 *
 * @author shill
 *
 */
public class LockNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public LockNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LockNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockNotFoundException(String message) {
		super(message);
	}

}
