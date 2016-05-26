package com.aw.utils.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;
import com.google.common.base.Preconditions;

/**
 * reads topic data from kafka using the given batch size
 *
 *
 *
 */
public class TopicReader implements TenantAware {

	static final Logger LOGGER = Logger.getLogger(TopicReader.class);

	//start out successful, i.e. allowing next() the first time
	private boolean lastBatchProcessed = true;

	/**
	 * our simple consumer for kafka, which will read messages
	 */
	private List<PartitionReader> consumers = new ArrayList<>();

	/**
	 * the platform
	 */
	private Platform platform;

	/**
	 * the topic we are reading from
	 */
	public Topic getTopic() { return topic; }
	private Topic topic;

	/**
	 * the unique id of this topic reader
	 */
	private String id;

	/**
	 * the zookeeper accessor
	 */
	private ZkAccessor zk;

	/**
	 * used to store checkpoints for the topics
	 */
	private TopicCheckpointer checkpointer;

	/**
	 * Set up a reader of a topic
	 *
	 * @param platform The platform in which the reader is running
	 * @param topic The topic to reader from
	 * @param id The id of this reader, must be globally unique
	 */
	public TopicReader(Platform platform, Topic topic, String id, ZkAccessor zk) throws Exception {
		this.platform = platform;
		this.topic = topic;
		this.id = id;
		this.zk = zk;
		init();
	}

	private void init() throws Exception {

		//get the topic name
		String strTopic =  Topic.toTopicString(getTenantID(), topic);

		//make sure the platform is set up for this reader
		KafkaOffsetUtils.verify(zk, platform, Tenant.forId(getTenantID()), id, Collections.singletonList(strTopic));

		//create the consumers
		List<Integer> partitions = KafkaTopics.getPartitionKeys(zk, platform,Topic.toTopicString(getTenantID(), topic));

		//get previous offsets, if any
		Map<Integer, Offset> offsets = KafkaOffsetUtils.getCurrentOffsets(zk, platform, Tenant.forId(getTenantID()), id, strTopic);

		//get the current offsets, if any
		for (int partition : partitions) {
			PartitionReader reader = new PartitionReader(partition, KafkaTopics.newSimpleConsumer(platform, strTopic, partition, id + "_" + strTopic + "_" + partition), strTopic);
			reader.setNextOffset(offsets.get(partition).getNextOffset());
			consumers.add(reader);
		}

		//create the checkpointer
		checkpointer = new TopicCheckpointer(Tenant.forId(getTenantID()), id);

	}

	/**
	 * Gets the next list of messages from the topic, up to maxCount.
	 *
	 * This method will store topic+partition position information for messages returned, and when onProcessed() is called, the offsets
	 * will be persisted to zookeeper.
	 *
	 * @return get the next set of message from the topic, up to maxCount messages
	 */
	public List<String> next(int maxCount) throws Exception {

		Preconditions.checkState(lastBatchProcessed, "cannot call next() until previous batch returned has been completed successfully, using onProcessed()");

		//reorder partitions each time to avoid starving partitions
		Collections.shuffle(consumers);

		//return will be the data itself from each partition
		List<String> ret = new ArrayList<String>();

		int max = maxCount;

		//get the latest messages per partition until we're done
		for (PartitionReader reader : consumers) {

			//get the message set
			PartitionMessageList messages = reader.next(max);

			//add messages if we got any and remember them for checkpointing once they are processed
			if (!messages.isEmpty()) {

				//add the checkpoint information
				checkpointer.addOffset(new Offset(this.id, reader.getTopic(), reader.getPartition(), messages.getNextOffset()));

				//add the message data
				ret.addAll(messages.getData());

			}

			//keep adding until we have enough
			max -= messages.size();
			if (max <= 0) {
				break;
			}

		}

		//we need a success callback to persist the offsets
		lastBatchProcessed = false;

		//return what we have
		return ret;

	}

	/**
	 * close the topic reader
	 */
	public void close() throws Exception {

		Exception lastE = null;
		for (PartitionReader reader : consumers) {
			try {
				reader.getConsumer().close();
			} catch (Exception e) {
				LOGGER.error("error while closing topic partition reader, topic=" + reader.getTopic() + " partition=" + reader.getPartition() + " tenant=" + getTenantID(), e);
				lastE = e;
			}
		}

		//bubble up the last exception if any
		if (lastE != null) {
			throw lastE;
		}

	}


	/**
	 * Indicates processing the previous next() call was successful. This must be called between each next() call,
	 * or an exception will be thrown. When this completes, offsets have been persisted in zookeeper.
	 */
	public void onProcessed() throws Exception {

		//write the offsets
		checkpointer.writeOffsetsOnSuccess(zk, platform);

		//we can now allow another next() call
		lastBatchProcessed = true;

	}

}
