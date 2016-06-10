package com.aw.common;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.spark.PreReqUtil;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.structure.Hive;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformBuilder;
import com.aw.platform.roles.Kafka;
import com.aw.platform.roles.Zookeeper;
import com.aw.utils.kafka.KafkaOffsetUtils;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.TestZKUtils;
import kafka.utils.Time;
import kafka.zk.EmbeddedZookeeper;

/**
 * A base unit test for kafka+zk testing.
 *
 * The protected methods allow subclasses to set up kafka topics that can then be used in unit tests.
 *
 *
 *
 */
public class AbstractKafkaZkUnitTest {

	@Before
	public void before() throws Exception {

		//create test zk server for use with test kafka
		String connect = TestZKUtils.zookeeperConnect();
		zk = new EmbeddedZookeeper(connect);

		//embedded server provides no getPort()...
		String strPort = connect.substring(connect.lastIndexOf(':') + 1);
		int zkPort = Integer.parseInt(strPort);

        // setup Broker
        kafkaPort = TestUtils.choosePort();
        Properties props = TestUtils.createBrokerConfig(1, kafkaPort, true);

        //create test kafka server
        KafkaConfig config = new KafkaConfig(props);
        Time mock = new MockTime();
        kafka = TestUtils.createServer(config, mock);

        //platform pointing to these servers
        platform = new PlatformBuilder()
        		.withNode("localhost", NodeRole.ZOOKEEPER, NodeRole.KAFKA)
        		.withSetting(Zookeeper.PORT, zkPort)
				.withSetting(Zookeeper.DG_ROOT_NODE, "/aw")
        		.withSetting(Kafka.PORT, kafkaPort).build();

	}

	@After
	public void after() throws Exception {

		//stop kafka
		kafka.shutdown();

		//stop zk
		zk.shutdown();

	}

	/**
	 * @return The zookeeper connect string
	 */
	protected String getZkConnectString() { return this.zk.connectString(); }

	/**
	 * Create a topic in the test kafka server
	 *
	 * @param tenant
	 * @param topic
	 * @throws Exception
	 */
	protected void createTopic(Tenant tenant, Topic topic) throws Exception{

		PreReqUtil.createTopic(this.platform, Topic.toTopicString(tenant.getTenantID(), topic));

	}

	protected StreamDef newTestStreamDef() {

		StreamDef def = new StreamDef();
		def.setProcessorId("testId");
		def.setSourceTopic(Arrays.asList(Topic.EVENTS_ES, Topic.ERRORS));
		def.setStreamHandlerClass("testClass");
		def.setTargetDriver("testTargetDriver");
		def.setTenant(true);

		return def;

	}

	protected void setupKafkaFor(Tenant tenant, StreamDef streamDef, long... offsets) throws Exception {
		setupKafkaFor(tenant, streamDef.getSourceTopicNames(tenant), streamDef.getProcessorName(tenant), offsets);
	}

	/**
	 * Set up kafka as if it has processed the streams in the StreamDef up to the offsets provided in the offset array
	 *
	 * @param testStream
	 * @param offsets
	 * @throws Exception
	 */
	protected void setupKafkaFor(Tenant tenant, List<String> topics, String id, long... offsets) throws Exception {

		setupKafkaFor(tenant);

		Impersonation.impersonateTenant(tenant);

		try (ZkAccessor zk = new DefaultZkAccessor(platform, Hive.TENANT)) {

			//create initial offsets
			int offset = 0;
			for (String topic : topics) {
				KafkaOffsetUtils.verify(zk, getPlatform(), tenant, id, topics);
				KafkaOffsetUtils.writeSuccess(zk, getPlatform(), tenant, id, topic, 0, offsets[offset++], Instant.ofEpochMilli(10), Instant.ofEpochMilli(20));
			}

		}

	}

	protected void setupKafkaFor(Tenant tenant) throws Exception {

		try (ZkAccessor zk = new DefaultZkAccessor(platform, Hive.TENANT)) {

			//ensure tenant root exists as this is now checked
			zk.ensure("/aw/tenant/" + tenant.getTenantID());

		}

	}

	//test zk server
	private EmbeddedZookeeper zk;

	//test kafka server
	private KafkaServer kafka;

	/**
	 * @return The local kafka server port
	 */
	protected int getKafkaPort() { return this.kafkaPort;  }
	private int kafkaPort;

	/**
	 * @return A platform configured for the local zk and kafka servers
	 */
	protected Platform getPlatform() { return this.platform; }
	private Platform platform;


}
