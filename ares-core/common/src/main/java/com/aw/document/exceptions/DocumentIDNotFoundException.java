package com.aw.document.exceptions;

public class DocumentIDNotFoundException extends DocumentNotFoundException {

	private static final long serialVersionUID = 1L;

	public DocumentIDNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentIDNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentIDNotFoundException(String message) {
		super(message);
	}

}
