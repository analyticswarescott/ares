package com.aw.platform.monitoring;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A message on a topic, used in status
 *
 *
 *
 */
public class TopicMessage extends AbstractTimestampedStatus {

	public TopicMessage() {
		super(Instant.now());
	}

	public TopicMessage(Instant timestamp, long position) {
		super(timestamp);
		this.position = position;
	}

	/**
	 * @return The position in the topic
	 */
	@JsonProperty("stat_pos")
	public long getPosition() { return this.position; }
	public void setPosition(long position) { this.position = position; }
	private long position;

}
