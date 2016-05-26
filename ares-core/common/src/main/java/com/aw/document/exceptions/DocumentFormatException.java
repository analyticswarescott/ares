package com.aw.document.exceptions;

public class DocumentFormatException extends RuntimeException {

	public DocumentFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentFormatException(String message) {
		super(message);
	}

}
