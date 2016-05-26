package com.aw.common.util;

import java.time.Duration;
import java.time.Instant;

/**
 * A static time source (time doesn't change)
 *
 *
 *
 */
public class SetTimeSource implements TimeSource {

	public SetTimeSource(Instant time) {
		this.time = time;
	}

	public void plus(Duration duration) {
		time = time.plus(duration);
	}

	public void minus(Duration duration) {
		time = time.minus(duration);
	}

	@Override
	public long nowMillis() {
		return time.toEpochMilli();
	}

	@Override
	public Instant now() {
		return time;
	}

	/**
	 * Change the set current time
	 *
	 * @param time The new current time
	 */
	public void setCurrentTime(Instant time) { this.time = time; }
	private Instant time;

}
