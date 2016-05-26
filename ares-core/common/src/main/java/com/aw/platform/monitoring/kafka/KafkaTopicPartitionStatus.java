package com.aw.platform.monitoring.kafka;

import java.time.Instant;
import java.util.UUID;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.platform.monitoring.AbstractStatus;
import com.aw.platform.monitoring.TopicMessage;
import com.aw.platform.monitoring.TopicPartitionStatus;

/**
 * Status for a stream's position in a particular kafka topic partition
 *
 *
 *
 */
public class KafkaTopicPartitionStatus extends AbstractStatus implements TopicPartitionStatus {

	public KafkaTopicPartitionStatus() {
	}

	public KafkaTopicPartitionStatus(Tenant tenant, Topic topic, int partition, TopicMessage latestProcessed, TopicMessage latest) throws Exception {
		this.guid = UUID.randomUUID();
		this.tenantId = tenant.getTenantID();
		this.topic = topic;
		this.partition = partition;
		this.latestProcessed = latestProcessed;
		this.latest = latest;
	}

	public String getTenantId() { return this.tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	private String tenantId;

	public UUID getGuid() { return this.guid; }
	public void setGuid(UUID guid) { this.guid = guid; }
	private UUID guid;

	public Instant getTime() { return this.time; }
	public void setTime(Instant time) { this.time = time; }
	private Instant time = Instant.now(); //TODO: use TimeSource

	public int getPartition() { return this.partition; }
	public void setPartition(int partition) { this.partition = partition; }
	private int partition;

	public Topic getTopic() { return this.topic; }
	public void setTopic(Topic topic) { this.topic = topic; }
	private Topic topic;

	public TopicMessage getLatest() { return this.latest; }
	public void setLatest(TopicMessage latest) { this.latest = latest; }
	private TopicMessage latest;

	public TopicMessage getLatestProcessed() { return this.latestProcessed; }
	public void setLatestProcessed(TopicMessage latestProcessed) { this.latestProcessed = latestProcessed; }
	private TopicMessage latestProcessed;

}
