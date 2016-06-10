package com.aw.platform.monitoring.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;

import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.messaging.Topic;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;

/**
 *
 *
 *
 */
public class KafkaStreamStatusTest extends AbstractKafkaZkUnitTest {

	//test offsets we will verify in the status
	private static long[] LAST_OFFSETS = { 10, 110 };

	@Test
	public void test() throws Exception {
		SecurityUtil.setThreadSystemAccess();

		//create a test stream def
		StreamDef testStream = newTestStreamDef();

		Tenant tenant = Tenant.forId("1");
		createTopic(tenant, Topic.EVENTS_ES);
		createTopic(tenant, Topic.ERRORS);

		setupKafkaFor(Tenant.forId("1"), testStream.getSourceTopicNames(tenant), testStream.getProcessorName(tenant), LAST_OFFSETS);

		//the class we're testing
		KafkaStreamStatus status = new KafkaStreamStatus();

		//get status for the test stream def
		ZkAccessor zk = new DefaultZkAccessor(getPlatform(), Hive.TENANT);
		status.collect(zk, getPlatform(), Tenant.forId("1"), testStream);

		//check the status
		assertEquals(2, status.getTopicStatus().size());

		//check bringing it back from json
		String strJson = JSONUtils.objectToString(status);
		status = JSONUtils.objectFromString(strJson, KafkaStreamStatus.class);

		//make sure properties are correct
		assertEquals(2, status.getTopicStatus().size());
		assertTrue(status.getTopicStatus().keySet().contains(Topic.EVENTS_ES));
		assertTrue(status.getTopicStatus().keySet().contains(Topic.ERRORS));

		assertEquals(0, status.getTopicStatus().get(Topic.EVENTS_ES).get(0).getLatest().getPosition());
		assertEquals(LAST_OFFSETS[0], status.getTopicStatus().get(Topic.EVENTS_ES).get(0).getLatestProcessed().getPosition());
		assertEquals(0, status.getTopicStatus().get(Topic.ERRORS).get(0).getLatest().getPosition());
		assertEquals(LAST_OFFSETS[1], status.getTopicStatus().get(Topic.ERRORS).get(0).getLatestProcessed().getPosition());

	}

}
