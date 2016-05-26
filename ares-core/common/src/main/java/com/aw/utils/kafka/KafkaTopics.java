package com.aw.utils.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;

import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.Kafka;

import kafka.admin.AdminUtils;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZKStringSerializer$;

//TODO: why is this all static ?? this needs to be a client instance
public class KafkaTopics {

	public static Logger logger = Logger.getLogger(KafkaTopics.class);

	//if a topic isn't found, kafka may not have fully created it yet - for now we give it this much time for the topic to be created
	private static long KAFKA_CREATION_WAIT_TIME = 1000L;





	public static List<Integer> getPartitionKeys(ZkAccessor zk, Platform platform, String topic) throws Exception{


        if (!zk.exists( "/brokers/topics/" + topic)) {
            logger.info("topic not found: " + topic);
            return Collections.emptyList();
        }


        String path = "/brokers/topics/" + topic + "/partitions";
        List<String> partitions = zk.getChildren(path);

        List<Integer> ret = new ArrayList<Integer>();
        for (String cur : partitions) {
        	ret.add(Integer.parseInt(cur));
        }

        return ret;

    }


    public static void deleteTopic(Platform platform, String topic) throws Exception{



        logger.warn(" deleting topic " + topic);
        ZkClient zk = new ZkClient(PlatformUtils.getZKQuorum(platform), 5000, 5000, ZKStringSerializer$.MODULE$);
		try {
			AdminUtils.deleteTopic(zk, topic);
		}
		catch (Exception ex) {
			logger.error(" error deleting topic " + ex.getMessage());
		}

        int i=0;
        while (topicExists(platform, topic)) {
            Thread.sleep(2000);
            i=i+2000;

			logger.warn(" topic deletion failed -- trying again to delete topic " + topic);
			try {
				AdminUtils.deleteTopic(zk, topic);
			}
			catch (Exception ex) {
				logger.error(" error deleting topic " + ex.getMessage());
			}
            if (i> 20000) {

				//TODO: topic deletion needs to work...for now proceeed
				logger.error( "deleted topic " + topic + " still exists after ");
                //throw new RuntimeException(" deleted topic still exists");
            }
        }

    }

    public static boolean topicExists(Platform platform, String topic) throws  Exception{
        ZkClient zk = new ZkClient(PlatformUtils.getZKQuorum(platform), 5000, 5000, ZKStringSerializer$.MODULE$);
        return AdminUtils.topicExists(zk, topic);
    }

    public static  List<String> getTopicList(ZkAccessor zk, Platform platform) throws Exception{
        List<String> topics = zk.getChildren("/brokers/topics");
        return topics;

    }

    public static SimpleConsumer newSimpleConsumer(Platform platform, String topic, int partition, String clientName) throws Exception {
        PartitionMetadata metadata = findLeader(platform, topic, partition);

        //if no metadata, the topic might have just been created - kafka's createTopic returns before the topic is actually fully created - give it a second
        //TODO: expenential backoff with more tries?
        if (metadata == null) {
        	Thread.sleep(KAFKA_CREATION_WAIT_TIME);
        	metadata = findLeader(platform, topic, partition);
        }

        if (metadata == null) {
            logger.warn("Can't find metadata for Topic and Partition. Exiting");
            return null;
        }
        if (metadata.leader() == null) {
        	logger.warn("Can't find Leader for Topic and Partition. Exiting");
            return null;
        }

        String leadBroker = metadata.leader().host();
        int leadPort = metadata.leader().port();

        SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, 10000, 100000, clientName);
        return consumer;
    }

    public static long[] getCurrentOffset(Platform platform, String topic, int partition, long whichTime, String clientName) throws Exception {

    	SimpleConsumer consumer = newSimpleConsumer(platform, topic, partition, clientName);

        try {

            TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
            Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
            requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
            kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
            OffsetResponse response = consumer.getOffsetsBefore(request);

            consumer.close();

            if (response.hasError()) {
            	logger.error(topic + ":" + partition + ":Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
                return null;
            }

            long[] offsets = response.offsets(topic, partition);
            return offsets;

        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    private static  PartitionMetadata findLeader(Platform platform, String a_topic, int a_partition) throws Exception{
        PartitionMetadata returnMetaData = null;
        loop:
        for (PlatformNode broker : PlatformUtils.getKafkaBrokerNodes(platform)) {
            SimpleConsumer consumer = null;
            try {
				String brokerHost = broker.getHost();
				int port = broker.getSettingInt(Kafka.PORT);

                consumer = new SimpleConsumer(brokerHost, port, 100000, 64 * 1024, "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error communicating with Kafka Brokers to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }

        return returnMetaData;
    }

   public static Offset getEmptyOffsetNode(String nodePath, String processorId, String sourceTopic, int partitionId) throws Exception{
    	return new Offset(nodePath, processorId, sourceTopic, partitionId);
    }

}
