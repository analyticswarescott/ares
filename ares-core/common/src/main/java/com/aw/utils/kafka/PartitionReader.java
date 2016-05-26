package com.aw.utils.kafka;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;

/**
 * reads data from a partition
 *
 *
 *
 */
class PartitionReader {

	public PartitionReader(int partition, SimpleConsumer consumer, String topic) {
		this.partition = partition;
		this.consumer = consumer;
		this.topic = topic;
	}

	/**
	 * read the next batch of messages from the partition
	 *
	 * @param maxCount the maximum number of messages to reader
	 * @return the messages read, may be empty but will not be null
	 * @throws Exception if anything goes wrong
	 */
	public PartitionMessageList next(int maxCount) throws Exception {

		//build the fetch request
		FetchRequest fetch = new FetchRequestBuilder()
					.clientId(consumer.clientId())
					.addFetch(topic, partition, nextOffset, maxCount * 100000)
					.build();

		//create the return value
		PartitionMessageList ret = new PartitionMessageList();

		//fetch from the partition
		FetchResponse response = consumer.fetch(fetch);
		ByteBufferMessageSet messages = response.messageSet(topic, partition);
		for (MessageAndOffset message : messages) {

			//check size
			if (ret.size() < maxCount) {
				ret.add(message);
				nextOffset = message.nextOffset();
			}

			//fill to max size
			else {
				break;
			}

		}

		return ret;

	}

	/**
	 * @return the partition offset we'll be reading from next
	 */
	public long getNextOffset() { return this.nextOffset;  }
	public void setNextOffset(long offset) { this.nextOffset = offset; }
	private long nextOffset;

	/**
	 * @return the partition id
	 */
	public int getPartition() { return this.partition;  }
	public void setPartition(int partition) { this.partition = partition; }
	private int partition;

	/**
	 * @return the topic name
	 */
	public String getTopic() { return this.topic;  }
	public void setTopic(String topic) { this.topic = topic; }
	private String topic;

	/**
	 * @return the kafka consumer used to read the data
	 */
	public SimpleConsumer getConsumer() { return this.consumer;  }
	public void setConsumer(SimpleConsumer consumer) { this.consumer = consumer; }
	private SimpleConsumer consumer;

}
