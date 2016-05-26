package com.aw.platform;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.platform.roles.Elasticsearch;
import com.aw.platform.roles.Kafka;

public class PlatformBuilderTest {

	@Test
	public void test() throws Exception {

		Platform platform = new PlatformBuilder()
				.withNode("node1", NodeRole.ZOOKEEPER, NodeRole.ELASTICSEARCH, NodeRole.KAFKA)
				.withNode("node2", NodeRole.KAFKA)
				.withSetting(Elasticsearch.PORT, 12345)
				.withSetting(Kafka.PORT, 23456)
				.withSetting("node2", Kafka.PORT, 34567)
				.build();

		//check basics
		assertEquals(2, platform.getNodes().size());
		assertNotNull(platform.getNode("node1"));
		assertNotNull(platform.getNode("node2"));
		assertNotNull(platform.getNode(NodeRole.ZOOKEEPER));
		assertNotNull(platform.getNodes(NodeRole.KAFKA));
		assertEquals(2, platform.getNodes(NodeRole.KAFKA).size());

		//now make sure settings work, with and without overrides
		assertEquals(23456, platform.getNode("node1").getSettingInt(Kafka.PORT));
		assertEquals(34567, platform.getNode("node2").getSettingInt(Kafka.PORT));

	}

}
