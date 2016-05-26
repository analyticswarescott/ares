package com.aw.action.exceptions;

/**
 * An exception during preparation of an action for execution
 *
 *
 *
 */
public class ActionPreparationException extends Exception {

	public ActionPreparationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ActionPreparationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionPreparationException(String message) {
		super(message);
	}

}
