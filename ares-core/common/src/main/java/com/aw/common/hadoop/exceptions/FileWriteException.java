package com.aw.common.hadoop.exceptions;

public class FileWriteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileWriteException(String message) {
		super(message);
	}


}
