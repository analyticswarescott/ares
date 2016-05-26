package com.aw.platform.monitoring;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Timestamped status base class
 *
 *
 *
 */
public class AbstractTimestampedStatus extends AbstractStatus implements TimestampedStatus {

	public AbstractTimestampedStatus(Instant time) {
		this.timestamp = time;
	}

	/**
	 * Create a status with a timestamp of now
	 */
	public AbstractTimestampedStatus() {
		this.timestamp = Instant.now();
	}

	@JsonProperty("dg_time")
	public Instant getTimestamp() { return this.timestamp;  }
	private Instant timestamp;

}
