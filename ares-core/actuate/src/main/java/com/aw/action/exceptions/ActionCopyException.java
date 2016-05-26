package com.aw.action.exceptions;

/**
 * An error occurred while copying an action
 *
 *
 */
public class ActionCopyException extends Exception {

	private static final long serialVersionUID = 1L;

	public ActionCopyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ActionCopyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionCopyException(String message) {
		super(message);
	}

}
