package com.aw.platform;

import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.roles.HdfsJournal;
import com.aw.platform.roles.HdfsName;
import com.aw.platform.roles.Kafka;
import com.aw.platform.roles.SparkMaster;
import com.aw.platform.roles.Zookeeper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PlatformUtils} utility class.
 */
public class PlatformUtilsTest {

	private Platform platform;

	@Before
	public void setUp() throws Exception {
		platform = mock(Platform.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSparkSubmitURLWithNullPlatform() throws Exception {
		PlatformUtils.getSparkSubmitURL(null);
	}

	@Test
	public void testGetSparkSubmitURL() throws Exception {

		// Configure the spark master platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(SparkMaster.SPARK_REST_PORT)).thenReturn(8000);

		final PlatformNode platformNode2 = mock(PlatformNode.class);
		when(platformNode2.getHost()).thenReturn("127.0.0.1");
		when(platformNode2.getSettingInt(SparkMaster.SPARK_REST_PORT)).thenReturn(8001);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.SPARK_MASTER)).thenReturn(Arrays.asList(platformNode1, platformNode2));

		// Make sure that we have both nodes represented in the resultant string
		assertEquals("spark://localhost:8000,127.0.0.1:8001", PlatformUtils.getSparkSubmitURL(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSparkMasterWithNullPlatform() throws Exception {
		PlatformUtils.getSparkMaster(null);
	}

	@Test
	public void testGetSparkMaster() throws Exception {

		// Configure the spark master platform node
		final PlatformNode sparkNode = mock(PlatformNode.class);
		when(sparkNode.getHost()).thenReturn("localhost");
		when(sparkNode.getSetting(SparkMaster.SPARK_MASTER_PORT)).thenReturn("8000");
		when(platform.getNode(NodeRole.SPARK_MASTER)).thenReturn(sparkNode);

		// Make sure that we get the correct connection string
		assertEquals("localhost:8000", PlatformUtils.getSparkMaster(platform));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSparkMasterNodesWithNullPlatform() throws Exception {
		PlatformUtils.getSparkMasterNodes(null);
	}

	@Test
	public void testGetSparkMasterNodes() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platform.getNodes(NodeRole.SPARK_MASTER)).thenReturn(Collections.singletonList(platformNode));

		final List<PlatformNode> platformNodes = PlatformUtils.getSparkMasterNodes(platform);
		assertEquals(1, platformNodes.size());
		assertTrue(platformNode == platformNodes.get(0));
	}

	@Test
	public void testGetSparkMasterList() throws Exception {

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHadoopNameNodesWithNull() throws Exception {
		PlatformUtils.getHadoopNameNodes(null);
	}

	@Test
	public void testGetHadoopNameNodes() throws Exception {

		// Configure the spark master platform node
		final PlatformNode sparkNode = mock(PlatformNode.class);
		when(sparkNode.getHost()).thenReturn("localhost");
		when(platform.getNodes(NodeRole.HDFS_NAME)).thenReturn(Collections.singletonList(sparkNode));

		// Make sure that we get the correct connection string
		final List<PlatformNode> hadoopNameNodes = PlatformUtils.getHadoopNameNodes(platform);
		assertEquals(1, hadoopNameNodes.size());
		assertTrue(sparkNode == hadoopNameNodes.get(0));

	}

	@Test(expected = PlatformStateException.class)
	public void testGetMoreThanTwoHadoopNameNodes() throws Exception {

		// Return more than two name nodes
		final PlatformNode sparkNode = mock(PlatformNode.class);
		when(platform.getNodes(NodeRole.HDFS_NAME)).thenReturn(Arrays.asList(sparkNode, sparkNode, sparkNode));

		PlatformUtils.getHadoopNameNodes(platform);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHadoopNameNodeListWithNullPlatform() throws Exception {
		PlatformUtils.getHadoopNameNodeList(null);
	}

	@Test
	public void testGetHadoopNameNodeList() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");

		final PlatformNode platformNode2 = mock(PlatformNode.class);
		when(platformNode2.getHost()).thenReturn("127.0.0.1");

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.HDFS_NAME)).thenReturn(Arrays.asList(platformNode1, platformNode2));

		// Make sure that we have both nodes represented in the resultant string
		assertEquals("localhost,127.0.0.1", PlatformUtils.getHadoopNameNodeList(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHadoopClusterNameWithNullPlatform() throws Exception {
		PlatformUtils.getHadoopClusterName(null);
	}

	@Test
	public void testGetHadoopClusterName() throws Exception {

		final String clusterName = "cluster_name";

		// Configure the mock platform nodes
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getSetting(HdfsName.HA_CLUSTER_NAME)).thenReturn(clusterName);
		when(platform.getNode(NodeRole.HDFS_NAME)).thenReturn(platformNode);

		// Make sure that we have both nodes represented in the resultant string
		assertEquals(clusterName, PlatformUtils.getHadoopClusterName(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHadoopJournalNodeListWithNull() throws Exception {
		PlatformUtils.getHadoopJournalNodeList(null);
	}

	@Test
	public void testGetHadoopJournalNodeList() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(HdfsJournal.PORT)).thenReturn(8000);

		final PlatformNode platformNode2 = mock(PlatformNode.class);
		when(platformNode2.getHost()).thenReturn("127.0.0.1");
		when(platformNode2.getSettingInt(HdfsJournal.PORT)).thenReturn(8001);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.HDFS_JOURNAL)).thenReturn(Arrays.asList(platformNode1, platformNode2));

		// Make sure that we have both nodes represented in the resultant string
		assertEquals("qjournal://localhost:8000;127.0.0.1:8001/dgCluster", PlatformUtils.getHadoopJournalNodeList(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetZKQuorumWithNullPlatform() throws Exception {
		PlatformUtils.getZKQuorum(null);
	}

	@Test
	public void testGetZKQuorum() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(Zookeeper.PORT)).thenReturn(8000);
		when(platform.getNodes(NodeRole.ZOOKEEPER)).thenReturn(Collections.singletonList(platformNode1));

		assertEquals("localhost:8000", PlatformUtils.getZKQuorum(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetZkConnectStringWithNullPlatform() throws Exception {
		PlatformUtils.getZkConnectString(null);
	}

	@Test
	public void testGetZkConnectString() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(Zookeeper.PORT)).thenReturn(8000);

		final PlatformNode platformNode2 = mock(PlatformNode.class);
		when(platformNode2.getHost()).thenReturn("127.0.0.1");
		when(platformNode2.getSettingInt(Zookeeper.PORT)).thenReturn(8001);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.ZOOKEEPER)).thenReturn(Arrays.asList(platformNode1, platformNode2));

		// Make sure that we have both nodes represented in the resultant string
		assertEquals("localhost:8000,127.0.0.1:8001", PlatformUtils.getZkConnectString(platform));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetKafkaProducerParamsWithNullPlatform() throws Exception {
		PlatformUtils.getKafkaProducerParams(null);
	}

	@Test
	public void testGetKafkaProducerParams() throws Exception {

		// Configure the spark master platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(Kafka.PORT)).thenReturn(8000);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.KAFKA)).thenReturn(Collections.singletonList(platformNode1));

		// Make sure we have all the required params
		final Properties kafkaProducerParams = PlatformUtils.getKafkaProducerParams(platform);
		assertEquals("localhost:8000", kafkaProducerParams.getProperty("metadata.broker.list"));
		assertEquals("kafka.serializer.StringEncoder", kafkaProducerParams.getProperty("serializer.class"));
		assertEquals("1", kafkaProducerParams.getProperty("request.required.acks"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetKafkaBrokerNodesWithNullPlatform() throws Exception {
		PlatformUtils.getKafkaBrokerNodes(null);
	}

	@Test
	public void testGetKafkaBrokerNodes() throws Exception {

		// Configure the spark master platform nodes
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getHost()).thenReturn("localhost");
		when(platformNode.getSettingInt(Kafka.PORT)).thenReturn(8000);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.KAFKA)).thenReturn(Collections.singletonList(platformNode));

		final List<PlatformNode> kafkaBrokerNodes = PlatformUtils.getKafkaBrokerNodes(platform);
		assertEquals(1, kafkaBrokerNodes.size());
		assertTrue(platformNode == kafkaBrokerNodes.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetKafkaBrokerListWithNullPlatform() throws Exception {
		PlatformUtils.getKafkaBrokerList(null);
	}

	@Test
	public void testGetKafkaBrokerList() throws Exception {

		// Configure the mock platform nodes
		final PlatformNode platformNode1 = mock(PlatformNode.class);
		when(platformNode1.getHost()).thenReturn("localhost");
		when(platformNode1.getSettingInt(Kafka.PORT)).thenReturn(8000);

		final PlatformNode platformNode2 = mock(PlatformNode.class);
		when(platformNode2.getHost()).thenReturn("127.0.0.1");
		when(platformNode2.getSettingInt(Kafka.PORT)).thenReturn(8001);

		// Return both of these nodes when asked
		when(platform.getNodes(NodeRole.KAFKA)).thenReturn(Arrays.asList(platformNode1, platformNode2));

		// Make sure that we have both nodes represented in the resultant string
		assertEquals("localhost:8000,127.0.0.1:8001", PlatformUtils.getKafkaBrokerList(platform));
	}
}