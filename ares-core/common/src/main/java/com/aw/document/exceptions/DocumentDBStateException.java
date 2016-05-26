package com.aw.document.exceptions;

public class DocumentDBStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DocumentDBStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentDBStateException(String s) {
        super(s);
    }
}
