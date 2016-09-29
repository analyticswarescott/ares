package com.aw.common.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.aw.common.exceptions.InitializationException;
import com.aw.util.Statics;

/**
 * Common class utilities
 *
 *
 *
 */
public class Utils {

	/**
	 * Initialize from the map using reflection. Property names should conform to standard json naming conventions (i.e. my_property_name)
	 *
	 * @param data The map of name->value pairs
	 * @param obj The object that should be updated
	 * @throws InitializationException If anything goes wrong
	 */
	public static <T> void initializeFromMap(Map<String, Object> data, Object obj) throws InitializationException {

		try {

			//bind using json which will use reflection
			String json = JSONUtils.objectToString(data);
			JSONUtils.updateFromString(json, obj);

		} catch (Exception e) {
			throw new InitializationException("error initializing from map", e);
		}
	}

	/**
	 * Create and initialize the processor
	 *
	 * @param clsName The class name of the processor we are creating
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String clsName, Class<T> baseType) throws InitializationException {

		try {

			Class<? extends T> cls = (Class<? extends T>)Class.forName(clsName);
			T processor = cls.newInstance();
			return processor;

		} catch (Exception e) {
			throw new InitializationException("error creating processor from class name: " + clsName, e);
		}

	}

	/**
	 * Generic method to determine if something is stale based on a timestamp, ttl, and this reference data's TimeSource
	 *
	 * @param time The time to check
	 * @param ttl The ttl to use
	 * @return Whether the time is expired (stale)
	 */
	public static boolean isStale(Instant time, Duration ttl, Instant currentTime) {
		return time.plus(ttl).isBefore(currentTime);
	}

	/**
	 * TODO: probably want to do this with an env variable or system property
	 *
	 * @return Whether elasticsearch is enabled - defaults to true
	 */
	public static boolean isElasticsearchEnabled() { return System.getProperty(Statics.PROP_ELASTICSEARCH_ENABLED) == null || Boolean.parseBoolean(System.getProperty(Statics.PROP_ELASTICSEARCH_ENABLED)); }

}
