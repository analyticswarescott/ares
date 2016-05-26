package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.common.util.TimeSource;

/**
 * Time source dependency provider, uses system time
 *
 *
 *
 */
public class TimeSourceProvider implements Provider<TimeSource> {

	public TimeSource get() {
		return TimeSource.SYSTEM_TIME;
	}

}
