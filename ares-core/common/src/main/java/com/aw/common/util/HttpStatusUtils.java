package com.aw.common.util;

/**
 * Utility class for checking the status of a HTTP response.
 */
public class HttpStatusUtils {

	// No need to instantiate this class
	private HttpStatusUtils() {}

	public static boolean isSuccessful(int statusCode) {
		return statusCode / 100 == 2;
	}

}
