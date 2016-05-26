package com.aw.compute.streams.processor.framework;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.kafka.HasOffsetRanges;
import org.apache.spark.streaming.kafka.OffsetRange;

import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.utils.kafka.Offset;
import com.aw.utils.kafka.TopicCheckpointer;

/**
 * Handles checkpointing of topics within spark streaming. This class handles the conversion of spark offset information into
 * DG format offset information
 */
public class SparkCheckpointer extends TopicCheckpointer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger logger = Logger.getLogger(SparkCheckpointer.class);

	public SparkCheckpointer() {
		this(null, null);
	}

	public SparkCheckpointer(Tenant tenant, String checkpointId) {
		super(tenant, checkpointId);
	}


	/**
	 * stores spark offsets before processing is completed on the rdd
	 *
	 * @param rdd
	 */
    protected void storeOffsets(JavaRDD<?> rdd) {

    	//get the spark offsets
    	OffsetRange[] sparkOffsets = ((HasOffsetRanges) rdd.rdd()).offsetRanges();

    	//convert to our offset format
    	Collection<Offset> offsets = Arrays.stream(sparkOffsets)
    									.map(sparkOffset -> toOffset(sparkOffset))
    									.collect(Collectors.toList());

    	//set them
    	setOffsets(offsets);

    }

    /**
     * @return map spark offsets to aw offset data
     */
    Offset toOffset(OffsetRange range) {

    	Offset ret = new Offset();

    	ret.setStartTime(Instant.now());
    	ret.setNextOffset(range.untilOffset());
    	ret.setSourceTopic(range.topic());
    	ret.setProcessorId(getCheckpointId());
    	ret.setPartitionId(range.partition());
		ret.setCount(range.count());

    	return ret;

    }

	public ZkAccessor getZk() { return zk; }
	public void setZk(ZkAccessor zk) { this.zk = zk; }
	private ZkAccessor zk;

}
