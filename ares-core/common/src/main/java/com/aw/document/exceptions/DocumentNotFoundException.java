package com.aw.document.exceptions;

public class DocumentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DocumentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentNotFoundException(String message) {
		super(message);
	}

}
