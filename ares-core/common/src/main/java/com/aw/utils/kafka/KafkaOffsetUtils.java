package com.aw.utils.kafka;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.Platform;

/*
 *
 * Static methods for reading offsets and verifying zookeeper is initiall set up to receive them
 */
public class KafkaOffsetUtils  {





    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOffsetUtils.class);


    /*
     * makes sure the root is in zookeeper, could verify that each processor has root too, but for now that is set
     * first time processor handles rdd
     */
    public static void verify(ZkAccessor zk, Platform platform, Tenant tenant, String id, List<String> sourceTopicNames) throws Exception {

		for (String sourceTopicName : sourceTopicNames) {
			LOGGER.debug(" Verifying zookeeper setup for processor " + id + "  topic: " + sourceTopicName);
			if (!zk.exists(ZkPurpose.OFFSETS, "")) {
				byte[] data = "streams_root".getBytes();
				zk.put(ZkPurpose.OFFSETS, null, data);
			}

			if (!zk.exists(ZkPurpose.OFFSETS, id)) {
				LOGGER.info(zk.getAbsoluteZkPath(ZkPurpose.OFFSETS, id) + " : NODE NOT FOUND: CREATING ");
				byte[] data = "app_root".getBytes();
				zk.put(ZkPurpose.OFFSETS, id, data);


			} else {
				LOGGER.debug(zk.getAbsoluteZkPath(ZkPurpose.OFFSETS, id) + ": found");
			}

			String zkNodeName = getTopicRootForProcessor(id, sourceTopicName);

			if (!zk.exists(ZkPurpose.OFFSETS,  zkNodeName)) {

				byte[] data = "streams_root".getBytes();
				zk.put(ZkPurpose.OFFSETS, zkNodeName, data);

				//write a 0 offset for each partition
				//TODO: make this the start offset in case we lose ZK offsets after Kafka is around a while
				List<Integer> partitions = KafkaTopics.getPartitionKeys(zk, platform, sourceTopicName);

				for (int key : partitions) {
					KafkaOffsetUtils.writeInitial(zk, platform, tenant, id, sourceTopicName, key);
				}

			}

		}

    }

/*    public static String getOffsetRootNameForProcessor(String processorName) {
        return ZK_STREAMS_ROOT + "/" + processorName;
    }*/

    /**
     * Get a full path to the topic offsets node given a tenant qualified topic name
     *
     * @param processorName
     * @param fullyQualifiedTopic
     * @return
     */
    public static final String getTopicRootForProcessor(String processorName, String fullyQualifiedTopic) {
    	return processorName + "/" + fullyQualifiedTopic;
    }

    /**
     * Get a full path to the topic offsets given a processor name, tenant and topic
     *
     * @param processorName
     * @param tenant
     * @param topic
     * @return
     */
    public static final String getTopicRootForProcessor(String processorName, Tenant tenant, Topic topic) {
    	return getTopicRootForProcessor(processorName, Topic.toTopicString(tenant.getTenantID(), topic));
    }

    public static Map<Integer, Offset> getCurrentOffsets(ZkAccessor zk, Platform platform, Tenant tenant, String processorName, String sourceTopicName) throws Exception {

        HashMap<Integer, Offset> ret = new HashMap<Integer, Offset>();

        try {

            //note path is written by OffsetWriter - need better way to keep in sync
            String nodeName = getTopicRootForProcessor(processorName, sourceTopicName);

            List<String> partitionKeys = new ArrayList<String>();

            if (zk.exists( ZkPurpose.OFFSETS, nodeName)) {
					partitionKeys = zk.getChildren(zk.getAbsoluteZkPath(ZkPurpose.OFFSETS, nodeName));
            } else {
                LOGGER.warn("partition keys null for " + nodeName);
            }

            for (String key : partitionKeys) {


                Offset o = zk.get(ZkPurpose.OFFSETS,  nodeName + "/" + key, Offset.class);
                if (o != null) {
                    ret.put(Integer.parseInt(key), o);
                } else {
                    LOGGER.warn("null offset data for " + processorName); //TODO: out is more readable in spark than err
                    throw new RuntimeException("null offset data for " + processorName);
                }
            }


        } catch(Exception ex){

            throw new RuntimeException("error getting offsets for " + processorName + "/" + sourceTopicName, ex);

        }

        return ret;

    }

	public static void writeSuccess(ZkAccessor zk, Platform platform, Tenant tenant, String processorName, String sourceTopic, int partitionId, long lastOffset, Instant startTS, Instant endTS) throws Exception {

    	String path = getTopicRootForProcessor(processorName, sourceTopic);


        if (!zk.exists(ZkPurpose.OFFSETS, processorName) ) {
            LOGGER.error(processorName + " NODE NOT FOUND and should have been; exiting");
            throw new RuntimeException("Zookeeper node " + zk.getAbsoluteZkPath(ZkPurpose.OFFSETS, processorName) + "  NOT FOUND and should have been; exiting");

        } else {
            LOGGER.debug(processorName + " found");
        }


        //verify that our parent exists
        if (!zk.exists(ZkPurpose.OFFSETS, path)) {
            LOGGER.debug(path + " Adding processor:topic root node");
            byte[] data = "streams_root".getBytes();
            zk.put(ZkPurpose.OFFSETS, path, data);
        }

        //now update or create our data node
        String partitionPath = path + "/" + partitionId;

        Offset offset = new Offset(path, processorName, sourceTopic, partitionId);
        offset.setStartTime(startTS);
        offset.setNextOffset(lastOffset);

        LOGGER.debug(" writing last offset of " + lastOffset + "  to topic  -----------------------------------> " + sourceTopic);


       // byte[] data = JSONUtils.objectToString(offset).getBytes();
		zk.put(ZkPurpose.OFFSETS, partitionPath, offset);

	}

	/**
	 * Used to write starting offsets when initializing processors
	 *
	 * @param rootName
	 * @param processor_id
	 * @param source_topic
	 * @param partition_id
	 */
	public static void writeInitial(ZkAccessor zk, Platform platform , Tenant tenant,  String processor_id, String source_topic, int partition_id) throws Exception {

	    LOGGER.warn(" Writing initialization offset for " + source_topic);
	    writeSuccess(zk, platform, tenant,  processor_id, source_topic, partition_id, 0, Instant.ofEpochMilli(0), Instant.ofEpochMilli(0));

	}

}
