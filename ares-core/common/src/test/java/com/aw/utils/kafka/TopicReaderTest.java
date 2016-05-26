package com.aw.utils.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.messaging.StringMessage;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.system.structure.Hive;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.utils.kafka.client.KafkaMessenger;

public class TopicReaderTest extends AbstractKafkaZkUnitTest implements TenantAware {

	@Test
	public void testReadMessages() throws Exception {

		ZkAccessor zk = new DefaultZkAccessor(getPlatform(), Hive.TENANT);
		Tenant tenant = Tenant.forId("1");

		//create a test topic
		createTopic(tenant, Topic.ACTION);
		setupKafkaFor(Tenant.forId("1"), Collections.singletonList(Topic.toTopicString(tenant.getTenantID(), Topic.ACTION)), "id", 0L);

		//add some messages
		KafkaMessenger messenger = new KafkaMessenger(getPlatform());
		for (int x=0; x<10; x++) {
			messenger.send(new StringMessage(tenant, Topic.ACTION, "test message " + x));
		}

		Impersonation.impersonateTenant(tenant);

		//create the reader
		TopicReader reader = new TopicReader(getPlatform(), Topic.ACTION, "id", zk);

		//get some messages off of the topic
		List<String> messages = reader.next(5);

		//do our assertions
		assertEquals(5, messages.size());
		assertEquals("test message 0", messages.get(0));
		assertEquals("test message 4", messages.get(4));

		//checkpoint
		reader.onProcessed();

		//create a new reader, should pick up where the last one left off
		reader.close();

		reader = new TopicReader(getPlatform(), Topic.ACTION, "id", zk);

		//get another bundle of messages
		messages = reader.next(5);

		//more assertions for the second bundle
		assertEquals(5, messages.size());
		assertEquals("test message 5", messages.get(0));
		assertEquals("test message 9", messages.get(4));

		reader.onProcessed();

		//the last request should be empty
		messages = reader.next(5);

		//do our assertions
		assertEquals(0, messages.size());

	}


}
