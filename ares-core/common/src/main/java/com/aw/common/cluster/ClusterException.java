package com.aw.common.cluster;

/**
 * A cluster related exception has occurred.
 *
 *
 *
 */
public class ClusterException extends Exception {

	private static final long serialVersionUID = 1L;

	public ClusterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClusterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClusterException(String message) {
		super(message);
	}

}
