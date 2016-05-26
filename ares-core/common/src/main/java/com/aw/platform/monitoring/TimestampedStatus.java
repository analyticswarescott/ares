package com.aw.platform.monitoring;

import java.time.Instant;

/**
 * A status with a timestamp indicating when it was collected.
 *
 *
 *
 */
public interface TimestampedStatus extends Status {

	/**
	 * @return The time this status was collected
	 */
	public Instant getTimestamp();

}
