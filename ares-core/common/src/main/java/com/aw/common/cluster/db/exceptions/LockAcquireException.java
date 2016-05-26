package com.aw.common.cluster.db.exceptions;

/**
 * An error occurred while trying to acquire a lock
 *
 * @author shill
 *
 */
public class LockAcquireException extends Exception {

	private static final long serialVersionUID = 1L;

	public LockAcquireException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LockAcquireException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockAcquireException(String message) {
		super(message);
	}

}
