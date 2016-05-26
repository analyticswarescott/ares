package com.aw.platform.exceptions;

import com.aw.common.exceptions.GroupedException;

public class NodeOperationGroupedException extends GroupedException {

	public NodeOperationGroupedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeOperationGroupedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeOperationGroupedException(String message) {
		super(message);
	}


}
