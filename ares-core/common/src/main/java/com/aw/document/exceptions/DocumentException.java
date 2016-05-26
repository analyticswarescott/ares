package com.aw.document.exceptions;

/**
 * General exception related to documents
 *
 *
 *
 */
public class DocumentException extends Exception {

	private static final long serialVersionUID = 1L;

	public DocumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentException(String message) {
		super(message);
	}

}
