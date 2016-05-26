package com.aw.unity.exceptions;

public class InvalidFilterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * If thrown within the tree structure of a filter, provides the path to that element along with the reason for the failure
	 *
	 * @param msg
	 * @param path
	 */
	public InvalidFilterException(String msg, String path) {
		this(msg + " (path to error: " + path + ")");
	}

	public InvalidFilterException(String message, Throwable cause) {
		super(message, cause);
	}

    public InvalidFilterException(String s) {
        super(s);
    }

}
