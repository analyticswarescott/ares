package com.aw.platform.monitoring;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.common.messaging.Topic;

/**
 * Base class for holding status for a given stream within the platform
 *
 * Example json output:
 *
 * <code><pre>
{
  "timestamp" : 1457204556585,
  "topic_status" : {
    "bundle_ref" : [ {
      "partition" : 0,
      "topic" : "bundle_ref",
      "latest" : {
        "timestamp" : 0,
        "position" : 0
      },
      "latest_processed" : {
        "timestamp" : 0,
        "position" : 10
      }
    } ],
    "scan_ref" : [ {
      "partition" : 0,
      "topic" : "scan_ref",
      "latest" : {
        "timestamp" : 0,
        "position" : 0
      },
      "latest_processed" : {
        "timestamp" : 0,
        "position" : 110
      }
    } ]
  }
}
 * </pre></code>
 *
 *
 *
 */
public abstract class AbstractStreamStatus extends AbstractTimestampedStatus implements StreamStatus {

	public AbstractStreamStatus() {
		this.timestamp = Instant.now();
	}

	/**
	 * Hold status of all partitions for this tenant + topic
	 */
	public Map<Topic, List<TopicPartitionStatus>> getTopicStatus() { return this.topicStatus; }
	public void setTopicStatus(Map<Topic, List<TopicPartitionStatus>> topicStatus) { this.topicStatus = topicStatus; }
	protected Map<Topic, List<TopicPartitionStatus>> topicStatus = new HashMap<Topic, List<TopicPartitionStatus>>();

	/**
	 * The time this status was collected
	 */
	public Instant getTimestamp() { return this.timestamp;  }
	public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
	private Instant timestamp;

	public String getProcessorID() {return processor_id;}
	public void setProcessorID(String processor_id) {this.processor_id = processor_id;}
	private String processor_id;

}
