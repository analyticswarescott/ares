package com.aw.common.util;

/**
 * Handles pieces of a larger JSON document being streamed
 *
 *
 *
 */
public interface JSONHandler {

	/**
	 * @param object The json object to handle
	 */
	public void handleJson(String json, long ordinal, Object context) throws Exception;

}
