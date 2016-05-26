package com.aw.utils.kafka;

import java.util.Iterator;
import java.util.LinkedList;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;

/**
 * A tenant topic in the form of an iterable of strings
 *
 *
 *
 */
public class TopicIterator implements Iterable<String>, Iterator<String> {

	public static final int DEFAULT_BATCH_SIZE = 1000;

	private TopicReader topicReader;

	//current bundle of values
	private LinkedList<String> values = new LinkedList<>();

	public TopicIterator(Platform platform, ZkAccessor zk, Topic topic, Tenant tenant) throws Exception {
		this.topicReader = new TopicReader(platform, topic, getClass().getName() + "_" + tenant.getTenantID(), zk);
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {

		fill();

		return values.size() > 0; //if we have data, there is a next

	}

	private void fill() {

		try {

			if (values.size() == 0) {

				//let the reader know we processed up to this point
				this.topicReader.onProcessed();
				values.addAll(this.topicReader.next(batchSize));

			}

		} catch (Exception e) {
			throw new RuntimeException("error getting next from topic " + Topic.toTopicString(topicReader.getTenantID(), topicReader.getTopic()), e);
		}


	}

	@Override
	public String next() {

		fill();

		return values.size() == 0 ? null : values.removeFirst();

	}

	/**
	 * @return the batch size to use in reading messages from the topic
	 */
	public int getBatchSize() { return this.batchSize; }
	public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
	private int batchSize = DEFAULT_BATCH_SIZE;

}
