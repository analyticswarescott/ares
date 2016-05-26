package com.aw.common.util;

import java.time.Instant;

/**
 * A source of the current time
 *
 *
 *
 */
public interface TimeSource {

	/**
	 * A default time source that returns the current system time
	 */
	public static final TimeSource SYSTEM_TIME = new TimeSource() {};

	/**
	 * @return The current time in millis
	 */
	default long nowMillis() { return System.currentTimeMillis(); }

	/**
	 * @return The current time as an Instant
	 */
	default Instant now() { return Instant.now(); }

}
