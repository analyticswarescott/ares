package com.aw.utils.kafka;

import java.time.Instant;

/**
 * Class to hold properties of an offset that can be used to start a DStream
 */
public class Offset {

	public Offset() {
	}

	/**
	 * construct with basic, essential information for offsets
	 *
	 * @param id
	 * @param topic
	 * @param partition
	 * @param offset
	 */
	public Offset(String id, String topic, int partition, long offset) {
		this.processorId = id;
		this.sourceTopic = topic;
		this.partitionId = partition;
		this.nextOffset = offset;
		this.startTime = Instant.now();
	}

	public Offset(String path, String id, String topic, int partition) {
		this.zkPath = path;
		this.processorId = id;
		this.sourceTopic = topic;
		this.partitionId = partition;
		this.startTime = Instant.ofEpochMilli(0L);
	}

    public String getZkPath() { return this.zkPath;  }
	public void setZkPath(String zkPath) { this.zkPath = zkPath; }
	private String zkPath;

	public String getProcessorId() { return this.processorId;  }
	public void setProcessorId(String processorId) { this.processorId = processorId; }
	private String processorId;

	public String getSourceTopic() { return this.sourceTopic;  }
	public void setSourceTopic(String sourceTopic) { this.sourceTopic = sourceTopic; }
	private String sourceTopic;

	public int getPartitionId() { return this.partitionId;  }
	public void setPartitionId(int partitionId) { this.partitionId = partitionId; }
	private int partitionId;

	public Instant getStartTime() { return this.startTime;  }
	public void setStartTime(Instant startTime) { this.startTime = startTime; }
	private Instant startTime;

	/**
	 * @return the next unread offset
	 */
	public long getNextOffset() { return this.nextOffset;  }
	public void setNextOffset(long lastOffset) { this.nextOffset = lastOffset; }
	private long nextOffset;


	public long getCount() {return count;}
	public void setCount(long count) {this.count = count;}
	private long count;

}

