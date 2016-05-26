package com.aw.platform.monitoring.kafka;

import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.platform.monitoring.TopicMessage;

public class KafkaTopicPartitionStatusTest {

	@Test
	public void test_toFromJson() throws Exception {

		KafkaTopicPartitionStatus status = new KafkaTopicPartitionStatus(Tenant.forId("1"), Topic.BUNDLE_REF, 1, new TopicMessage(Instant.ofEpochMilli(1000), 100), new TopicMessage(Instant.ofEpochMilli(2000), 200));

		String strJson = JSONUtils.objectToString(status);

		JSONObject json = new JSONObject(strJson);

		//verify all unity fields are there
		assertTrue(json.has("dg_time"));
		assertTrue(json.has("dg_guid"));
		assertTrue(json.has("dg_tenant"));
		assertTrue(json.has("stat_partition"));
		assertTrue(json.has("stat_topic"));
		assertTrue(json.has("stat_latest"));
		assertTrue(json.has("stat_processed"));
		assertTrue(json.getJSONObject("stat_latest").has("stat_pos"));
		assertTrue(json.getJSONObject("stat_processed").has("stat_pos"));

	}

}
