package com.aw.platform.monitoring;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.aw.common.messaging.Topic;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;
import com.aw.platform.monitoring.kafka.KafkaStreamStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Holds status for a single stream within spark
 *
 *
 *
 */
@JsonDeserialize(as=KafkaStreamStatus.class)
public interface StreamStatus extends TimestampedStatus {

	/**
	 * Collect status for a stream
	 *
	 * @param platform
	 * @throws Exception
	 */
	public void collect(ZkAccessor zk, Platform platform, Tenant tenant, StreamDef stream) throws IOException;

	/**
	 * @return The status content per topic. Each topic contains status for each partition within that topic.
	 */
	public Map<Topic, List<TopicPartitionStatus>> getTopicStatus();

	public String getProcessorID();

}
