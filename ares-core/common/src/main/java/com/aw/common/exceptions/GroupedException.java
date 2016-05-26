package com.aw.common.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Used when multiple exceptions occur, mapped to a particular type of object
 *
 *
 *
 */
public class GroupedException extends Exception {

	private static final long serialVersionUID = 1L;

	public GroupedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GroupedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupedException(String message) {
		super(message);
	}

	/**
	 * @return The keys for this exception group
	 */
	public <T> Iterable<T> keys() {
		return (Iterable<T>)m_exceptions.keySet().iterator();
	}

	/**
	 * Add an exception to this group
	 *
	 * @param object The key for this exception
	 * @param e The exception
	 */
	public <T> void add(T object, Exception e) { m_exceptions.put(object, e); }

	/**
	 * Get an exception from this group
	 *
	 * @param object The key for the exception
	 * @return The exception
	 */
	public <T> T get(T object) { return (T)m_exceptions.get(object); }

	private Map<Object, Exception> m_exceptions = new HashMap<>();


}
