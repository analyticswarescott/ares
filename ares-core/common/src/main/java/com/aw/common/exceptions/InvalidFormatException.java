package com.aw.common.exceptions;

/**
 * An invalid format of some kind was encountered.
 *
 *
 *
 */
public class InvalidFormatException extends RuntimeException {

	public InvalidFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFormatException(String message) {
		super(message);
	}

}
