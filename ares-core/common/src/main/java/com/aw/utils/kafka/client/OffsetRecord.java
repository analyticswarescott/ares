package com.aw.utils.kafka.client;

import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ProcessingException;

public class OffsetRecord {
	private int partition;
	private long offset;
	private String groupId;
	private String topic;
	private long timestamp;
	private int version;


	public int getPartition() { return partition; }
	public long getOffset() { return offset; }
	public String getGroupId() { return groupId; }
	public String getTopic() { return topic; }
	public long getTimestamp() { return timestamp; }
	public int getVersion() { return version; }


	public void setPartition(int partition) { this.partition = partition; }
	public void setOffset(long offset) { this.offset = offset; }
	public void setGroupId(String groupId) { this.groupId = groupId; }
	public void setTopic(String topic) { this.topic = topic; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	public void setVersion(int version) { this.version = version; }


	/**
	 * Create empty
	 */
	public OffsetRecord() {}


	/**
	 * Create new from JSON
	 * @param json
	 */
	public OffsetRecord(String json) throws ProcessingException {

		try {
			JSONObject j = new JSONObject(json);
			setGroupId((String)j.get("groupid"));
			setOffset((Long) j.get("offset"));
		} catch (Exception e) {
			throw new ProcessingException("error processing offset record", e);
		}
	}


	/**
	 * Write out as JSON
	 * @return
	 */
	public String toJSON() throws ProcessingException {

		try {

			JSONObject obj = new JSONObject();
	 		obj.put("topic",getTopic());
	 		obj.put("partition",getPartition());
	 		obj.put("groupid",getGroupId());
	 		obj.put("offset",getOffset());
	 		obj.put("version", getVersion());
	 		obj.put("ts", getTimestamp());
	 		return obj.toString();

		} catch (Exception e) {
			throw new ProcessingException("error processing offset record", e);
		}

	}

}
