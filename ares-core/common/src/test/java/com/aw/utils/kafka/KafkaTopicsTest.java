package com.aw.utils.kafka;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

/**
 * Created by aron on 1/6/16.
 */
public class KafkaTopicsTest {

    @Test
    public void getEmptyOffsetNode() throws Exception {

        String nodePath = UUID.randomUUID().toString();
        String processorID = UUID.randomUUID().toString();
        String sourceTopic = UUID.randomUUID().toString();
        int partitionID = 1;
        Offset offset = KafkaTopics.getEmptyOffsetNode(nodePath, processorID, sourceTopic, partitionID);

        assertEquals( nodePath, offset.getZkPath());
        assertEquals( processorID, offset.getProcessorId());
        assertEquals( sourceTopic, offset.getSourceTopic());
        assertEquals( partitionID, offset.getPartitionId());
        assertEquals( Instant.ofEpochMilli(0L), offset.getStartTime());
        assertEquals( 0L, offset.getNextOffset());

    }

}
