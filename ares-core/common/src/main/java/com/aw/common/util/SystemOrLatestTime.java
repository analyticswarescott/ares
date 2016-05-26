package com.aw.common.util;

import java.time.Instant;

/**
 * A current time equal to the current system time, or the latest time seen, whichever is later.
 *
 *
 *
 */
public class SystemOrLatestTime implements TimeSource {

	/**
	 * @return The latest time seen - if greater than the system time, this will be the current time
	 */
	private Instant latest;

	@Override
	public long nowMillis() {

		return now().toEpochMilli();

	}

	@Override
	public Instant now() {

		if (this.latest == null) {
			return TimeSource.super.now();
		}

		else {
			return this.latest;
		}

	}

	public void updateLatest(Instant time) {
		this.latest = time;
	}

}
