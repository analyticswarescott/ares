package com.aw.document.exceptions;

public class DocumentNameNotFoundException extends DocumentNotFoundException {

	private static final long serialVersionUID = 1L;

	public DocumentNameNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentNameNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentNameNotFoundException(String message) {
		super(message);
	}


}
