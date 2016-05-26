package com.aw.common.task.exceptions;

/**
 * no task found
 *
 *
 *
 */
public class TaskNotFoundException extends TaskException {

	private static final long serialVersionUID = 1L;

	public TaskNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskNotFoundException(String message) {
		super(message);
	}

}
