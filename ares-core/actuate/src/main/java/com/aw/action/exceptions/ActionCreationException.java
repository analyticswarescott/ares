package com.aw.action.exceptions;

/**
 * error creating an action
 *
 *
 *
 */
public class ActionCreationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ActionCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionCreationException(String message) {
		super(message);
	}


}
