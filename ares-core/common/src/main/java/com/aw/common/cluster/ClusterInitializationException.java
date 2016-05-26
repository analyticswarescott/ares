package com.aw.common.cluster;

import com.aw.common.exceptions.InitializationException;

/**
 * An error occurred during cluster initialization
 *
 *
 *
 */
public class ClusterInitializationException extends InitializationException {

	private static final long serialVersionUID = 1L;

	public ClusterInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClusterInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClusterInitializationException(String message) {
		super(message);
	}

}
