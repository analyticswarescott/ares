package com.aw.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.roles.Elasticsearch;
import com.aw.platform.roles.HdfsName;
import com.aw.platform.roles.Kafka;
import com.aw.platform.roles.Node;
import com.aw.platform.roles.SparkMaster;
import com.aw.platform.roles.Zookeeper;

public class DefaultPlatformTest {


	/**
	 *
	 */
	@Test
	public void testCreation() throws Exception {

		Document p = new TestDocumentHandler().getDocument(DocumentType.PLATFORM, Platform.DEFAULT);

		DefaultPlatform platform = p.getBodyAsObject();

		//make sure things were loaded
		assertNotNull("platform nodes missing", platform.getNodes());
		assertEquals("platform node count wrong", 1, platform.getNodes().size());

		PlatformNode node = platform.getNodes().values().iterator().next();

		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().size() == 12);
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.KAFKA));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.ELASTICSEARCH));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.HDFS_NAME));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.HDFS_DATA));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.HDFS_JOURNAL));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.REST));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.SPARK_MASTER));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.SPARK_WORKER));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.ZOOKEEPER));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.CONFIG_DB_MASTER));
		assertTrue("node roles wrong", node.getRoles() != null && node.getRoles().contains(NodeRole.CONFIG_DB_WORKER));
		assertEquals("port wrong", 9200, node.getSettingInt(Elasticsearch.PORT));
		assertEquals("cluster wrong", "aw", node.getSetting(Elasticsearch.CLUSTER_NAME));
		assertEquals("port wrong", 9092, node.getSettingInt(Kafka.PORT));
		assertEquals("port wrong", 9000, node.getSettingInt(HdfsName.PORT));
		assertEquals("port wrong", 2181, node.getSettingInt(Zookeeper.PORT));
		assertEquals("host wrong", "localhost", node.getHost());
		assertEquals("default port wrong", 1234, node.getSettingInt(SparkMaster.PORT, 1234));
		assertEquals("node port wrong", 9100, node.getSettingInt(Node.PORT, 1234));

		//make sure the platform json back to a string matches the original
		String expected = p.getBodyAsString();

		assertTrue("platform converted back to json wrong", JSONUtils.jsonEquals(expected, JSONUtils.objectToString(platform)));

	}

	@Test
	public void testEqualNodes() throws Exception {

		//should return 2 since hosts are different even though configuration maps are identical
		Platform platform = new PlatformBuilder()
								.withNode("host1", NodeRole.ELASTICSEARCH).withSetting(Elasticsearch.PORT, 1234)
								.withNode("host2", NodeRole.ELASTICSEARCH).withSetting(Elasticsearch.PORT, 1234).build();

		assertEquals(2, platform.getNodes(NodeRole.ELASTICSEARCH).size());

		List<PlatformNode> nodes = platform.getNodes(NodeRole.ELASTICSEARCH);
		assertNotEquals(nodes.get(0), nodes.get(1));

	}

}
