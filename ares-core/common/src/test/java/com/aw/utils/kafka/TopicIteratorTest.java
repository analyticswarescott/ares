package com.aw.utils.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.messaging.StringMessage;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.system.structure.Hive;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.utils.kafka.client.KafkaMessenger;

public class TopicIteratorTest extends AbstractKafkaZkUnitTest {

	@Test
	public void testTopicIteratorMessages() throws Exception {

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
		TopicIterator iterator = new TopicIterator(getPlatform(), zk, Topic.ACTION, tenant);

		int x = 0;
		while (iterator.hasNext()) {
			assertEquals("test message " + x++, iterator.next());
		}

		//make sure we read the right number of messages
		assertEquals(10, x);

	}

}
