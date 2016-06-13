package com.aw.platform.monitoring;

import java.time.Instant;
import java.util.UUID;

import com.aw.common.messaging.Topic;
import com.aw.platform.monitoring.kafka.KafkaTopicPartitionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * correlates to the dg_status unity stream_status type
 *
 *
 *
 */
@JsonDeserialize(as=KafkaTopicPartitionStatus.class)
public interface TopicPartitionStatus extends Status {

	public static final String UNITY_TYPE = "topic_status";

	/**
	 * @return Unique id for this status
	 */
	@JsonProperty("dg_guid")
	public UUID getGuid();

	/**
	 * @return time of the status
	 */
	@JsonProperty("dg_time")
	public Instant getTime();

	/**
	 * @return partition id of this status object
	 */
	@JsonProperty("stat_partition")
	public int getPartition();

	/**
	 * @return the tenant associated with this status
	 */
	@JsonProperty("dg_tenant")
	public String getTenantId();

	/**
	 * @return The topic related to this status
	 */
	@JsonProperty("stat_topic")
	public Topic getTopic();

	/**
	 * @return The latest message on the topic
	 */
	@JsonProperty("stat_latest")
	public TopicMessage getLatest();

	/**
	 * @return The latest message on the topic that has been processed
	 */
	@JsonProperty("stat_processed")
	public TopicMessage getLatestProcessed();

	/**
	 * @return unity type for topic status
	 */
	@JsonProperty("eventType")
	default public String getUnityType() { return UNITY_TYPE; }

}
