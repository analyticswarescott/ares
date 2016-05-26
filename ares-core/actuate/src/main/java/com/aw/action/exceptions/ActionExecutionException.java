package com.aw.action.exceptions;

import java.util.HashMap;
import java.util.Map;

import com.aw.action.Action;

/**
 * An exception occurred executing one or more actions
 *
 *
 *
 */
public class ActionExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ActionExecutionException(String message, Action action, Exception cause) {
		super(message, cause);
		m_exceptionMap.put(action, cause);
	}

	public ActionExecutionException(Action action, Exception cause) {
		super(cause);
		m_exceptionMap.put(action, cause);
	}

	public ActionExecutionException(Exception cause) {
		super(cause);
	}


	public ActionExecutionException(String message, Action action) {
		super(message);
		m_exceptionMap.put(action, this);
	}


	public void addException(Action action, Exception exception) {
		m_exceptionMap.put(action, exception);
	}

	public Map<Action, Exception> getExceptionMap() { return m_exceptionMap; }
	private Map<Action, Exception> m_exceptionMap = new HashMap<Action, Exception>();

}
