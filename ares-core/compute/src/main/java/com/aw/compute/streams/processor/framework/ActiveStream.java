package com.aw.compute.streams.processor.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.aw.common.zookeeper.ZkAccessor;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.Utils;
import com.aw.compute.streams.drivers.MessageHandlerTuple2;
import com.aw.platform.Platform;
import com.aw.platform.PlatformUtils;
import com.aw.unity.exceptions.InvalidConfigurationException;
import com.aw.utils.kafka.KafkaOffsetUtils;
import com.aw.utils.kafka.Offset;

import kafka.common.TopicAndPartition;
import kafka.serializer.StringDecoder;
import scala.Tuple2;

/**
 * A stream currently being processed in the platform.
 *
 *
 *
 */
public class ActiveStream
{
	public static final Logger logger = LoggerFactory.getLogger(ActiveStream.class);

	private Provider<Platform> platform;
	private ZkAccessor zk;

	public ActiveStream(ZkAccessor zk, Provider<Platform> platform, Tenant tenant, StreamDef def) {
		streamDef = def;
		this.tenant = tenant;
		this.platform = platform;
		this.zk = zk;
	}

	@SuppressWarnings("unchecked")
	public void start(JavaStreamingContext jssc) {

		try {

			Map<String, String> kafkaConsumerParams = new HashMap<>();
			kafkaConsumerParams.put("metadata.broker.list", PlatformUtils.getKafkaBrokerList(platform.get()));

			List<String> sourceTopics = streamDef.getSourceTopicNames(tenant);

			//map to spark offset format (keeping old format because it has min/max information)
			Map<TopicAndPartition, Long> offsetStart = new HashMap<>();
			for (String source_topic : sourceTopics) {

				Map<Integer, Offset> offsets = KafkaOffsetUtils.getCurrentOffsets(zk, platform.get(), getTenant(),  streamDef.getProcessorName(tenant), source_topic);

				//TODO: validate against topic offsets to be sure it is in range

				if (offsets.size() == 0) {
					throw new RuntimeException("offsets not found for processor " + streamDef.getProcessorName(tenant) + " topic " + source_topic);
				}

				for (Offset o : offsets.values()) {
					TopicAndPartition topicAndPartition = new TopicAndPartition(o.getSourceTopic(), o.getPartitionId());
					offsetStart.put(topicAndPartition, o.getNextOffset()); //start from next unread offset
				}

			}

			//end topic offset formatting

			StringBuilder sb = new StringBuilder();
			for (String s : sourceTopics) {
				sb.append(s);
				sb.append("\t");
			}
			logger.error(" DEBUG creating stream " + streamDef.getProcessorName(tenant) + "  to listen to topics: " + sb.toString());

			MessageHandlerTuple2 messageHandler = new MessageHandlerTuple2();

			//create the direct stream - we pass in a generic function since it seems to be impossible to get the correct class type in the
			//case of a generic function value
			JavaInputDStream<Tuple2<String, String>> messages = KafkaUtils.createDirectStream(
					jssc,
					String.class,
					String.class,
					StringDecoder.class,
					StringDecoder.class,
					String.class,
					kafkaConsumerParams,
					offsetStart,
					messageHandler.getA()
			);
			_messages = messages;

			logger.error(" DEBUG --- created stream " + streamDef.getProcessorName(tenant) + "  to listen to topics: " + sb.toString());

			//get the stream handler for this stream def and initialize it
			StreamHandler pf = initStreamHandler(streamDef);

			//handle the stream
			pf.handle(messages);

			logger.error(" DEBUG === handled stream " + streamDef.getProcessorName(tenant) + "  to listen to topics: " + sb.toString());

		}

		catch(Exception ex){
			//TODO: create typed Exception
			throw new RuntimeException(" error establishing stream ", ex);
		}

	}

	private StreamHandler initStreamHandler(StreamDef streamDef) throws InvalidConfigurationException {

		final AbstractStreamHandler pf;

		//see if there is a specific stream handler defined
		if (streamDef.getStreamHandlerClass() != null) {
			System.out.println(" StreamHandlerClass is " + streamDef.getStreamHandlerClass());

			pf = Utils.newInstance(streamDef.getStreamHandlerClass(), AbstractStreamHandler.class);
		} else {

			//create a default stream handler
			pf = new DefaultStreamHandler();

		}

		//set it up
		pf.setTenant(tenant);
		pf.setName(streamDef.getProcessorName(tenant));
		pf.setZk(zk);
		pf.init(streamDef);

		return pf;

	}

	public StreamDef getStreamDef() { return streamDef; }
	private StreamDef streamDef;

	public Tenant getTenant() { return this.tenant; }
	private Tenant tenant;

	public JavaInputDStream<Tuple2<String, String>> getMessageStream() { return _messages; }
	private JavaInputDStream<Tuple2<String, String>> _messages;


}
