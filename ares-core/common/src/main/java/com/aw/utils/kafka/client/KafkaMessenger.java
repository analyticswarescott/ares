package com.aw.utils.kafka.client;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.messaging.Message;
import com.aw.common.messaging.Messenger;
import com.aw.common.messaging.Topic;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;


/**
 * A kafka message sender for the platform.
 */
public class KafkaMessenger implements Messenger<String> {

	private static final Logger logger = LoggerFactory.getLogger(KafkaMessenger.class);

	private final Producer<String, String> _producer;

	private boolean _closed = false;

	public KafkaMessenger(Platform platform) throws Exception {

		m_platform = platform;

		Properties props = new Properties();
        props.put("metadata.broker.list", buildPlatformBrokerList());
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "com.aw.utils.kafka.client.RoundRobinPartitioner");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        // create producer
        _producer = new Producer<String, String>(config);

	}

	/**
	 * Build kafka the broker list for the platform
	 *
	 * @return
	 * @throws Exception
	 */
	private String buildPlatformBrokerList() throws Exception {

		List<PlatformNode> nodes = m_platform.getNodes(NodeRole.KAFKA);

		if (nodes.size() == 0) {
			throw new Exception("no kafka nodes defined in the platform, cannot send messages to kafka");
		}

		StringBuilder brokerList = new StringBuilder();
		for (PlatformNode node : nodes) {

			if (brokerList.length() > 0) {
				brokerList.append(",");
			}

			brokerList.append(node.getHost());
			brokerList.append(":");
			brokerList.append(node.getSetting(Kafka.PORT));

		}

		return brokerList.toString();

	}

	/**
	 * Is producer closed?
	 * @return
	 */
	public boolean isClosed() { return _closed; }

	/**
	 * Send a string message to the specified topic
	 *
	 * @param key
	 * @param message
	 */
	public void send(Message<String> message) throws IOException {

		if (!_closed) {

			String topic = Topic.toTopicString(message.getSourceTenant().getTenantID(), message.getTopic());
			String payload = message.getPayload();

			// write message.
			KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, message.getSourceTenant().getTenantID(), payload);
	        _producer.send(data);

		} else {
			throw new IOException("cannot send, sender alreadt closed");
		}

	}

	/**
	 * Close Producer
	 */
	public void close() {
		if (!_closed) {
			_producer.close();
			_closed = true;
		}
	}

	/**
	 * The platform on which this sender sends messages
	 */
	private Platform m_platform;

}
