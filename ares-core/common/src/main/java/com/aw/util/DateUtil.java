package com.aw.util;

import java.text.SimpleDateFormat;

/**
 * Common date utilities
 *
 *
 */
public class DateUtil {

	//MS ticks per millisecond - used to convert MS time to millis
	private static final long TICKS_PER_MILLI = 10000L;

	//millis between the two
	private static final long FILETIME_EPOCH_MS = 11644473600000L;

	private static final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Convert microsoft 100 nano ticks since 1601 to unix epoch time in millis since jan 1 1970
	 *
	 * @return
	 */
	public static long fromMSToUnixTime(long in) {

		//convert to milliseconds
		long time = in /= TICKS_PER_MILLI;
		time -= FILETIME_EPOCH_MS;
		return time;

	}

	public static long fromUnixToMSTime(long in) {

		//convert to milliseconds
		long time = in + FILETIME_EPOCH_MS;
		time *= TICKS_PER_MILLI;
		return time;

	}

}
