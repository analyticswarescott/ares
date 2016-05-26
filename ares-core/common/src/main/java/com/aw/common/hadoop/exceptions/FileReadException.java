package com.aw.common.hadoop.exceptions;

public class FileReadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileReadException(String message) {
		super(message);
	}


}
