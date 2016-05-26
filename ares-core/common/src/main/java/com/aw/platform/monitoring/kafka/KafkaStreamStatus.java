package com.aw.platform.monitoring.kafka;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aw.common.messaging.Topic;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;
import com.aw.platform.monitoring.AbstractStreamStatus;
import com.aw.platform.monitoring.TopicMessage;
import com.aw.platform.monitoring.TopicPartitionStatus;
import com.aw.utils.kafka.KafkaOffsetUtils;
import com.aw.utils.kafka.KafkaTopics;
import com.aw.utils.kafka.Offset;

/**
 * Status of a stream based on its kafka topic positions
 *
 *
 *
 */
public class KafkaStreamStatus extends AbstractStreamStatus {

	/**
	 * We need a client name to ask kafka for stuff - for some reason - i'm sure there's a great reason for that
	 */
	private static final String CLIENT_NAME = "stream_status";

	@Override
	public void collect(ZkAccessor zk, Platform platform, Tenant tenant, StreamDef stream) throws IOException {

		try {

			for (Topic topic : stream.getSourceTopic()) {

				setProcessorID(stream.getProcessorId());

				//get latest offsets processed
				Map<Integer, Offset> latestProcessed = KafkaOffsetUtils.getCurrentOffsets(zk, platform, tenant, stream.getProcessorName(tenant), Topic.toTopicString(tenant.getTenantID(), topic));

				List<TopicPartitionStatus> partStatus = new ArrayList<TopicPartitionStatus>();
				for (int partition : latestProcessed.keySet()) {

					Offset offset = latestProcessed.get(partition);
					long[] latest = KafkaTopics.getCurrentOffset(platform, Topic.toTopicString(tenant.getTenantID(), topic), partition, kafka.api.OffsetRequest.LatestTime(), CLIENT_NAME);

					if (latest != null) {

						TopicMessage latestProcessedMessage = new TopicMessage(Instant.ofEpochMilli(0L), offset.getNextOffset());
						TopicMessage latestMessage = new TopicMessage(Instant.ofEpochMilli(0L), latest[0]);
						TopicPartitionStatus status = new KafkaTopicPartitionStatus(tenant, topic, partition, latestProcessedMessage, latestMessage);
						partStatus.add(status);

					}

				}

				getTopicStatus().put(topic, partStatus);

			}

		} catch (Exception e) {
			throw new IOException("error collecting status for stream " + stream.getProcessorName(tenant), e);
		}

	}

}
