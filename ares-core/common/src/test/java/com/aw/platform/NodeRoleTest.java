package com.aw.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentHandler;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.nodes.managers.DefaultRoleManager;
import com.aw.platform.nodes.managers.ElasticSearchRoleManager;
import com.aw.platform.nodes.managers.HdfsDataRoleManager;
import com.aw.platform.nodes.managers.HdfsJournalRoleManager;
import com.aw.platform.nodes.managers.HdfsNameRoleManager;
import com.aw.platform.nodes.managers.KafkaRoleManager;
import com.aw.platform.nodes.managers.RestRoleManager;
import com.aw.platform.nodes.managers.SparkMasterRoleManager;
import com.aw.platform.nodes.managers.SparkWorkerRoleManager;
import com.aw.platform.nodes.managers.ZookeeperRoleManager;
import com.aw.platform.roles.Rest;
import com.aw.platform.roles.SparkMaster;

public class NodeRoleTest {

	private Provider<PlatformMgr> platformMgr;
	private Provider<DocumentHandler> docs;

	@Before
	public void before() throws Exception {
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getSettingInt(Rest.PORT)).thenReturn(8080);
		final Platform platform = mock(Platform.class);
		when(platform.getNode(NodeRole.REST)).thenReturn(platformNode);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		doReturn(platform).when(platformMgr).getPlatform();
		docs = new TestProvider<>(new TestDocumentHandler());
		this.platformMgr = new TestProvider<>(platformMgr);
	}
	@Test
	public void testNodeRoleRest() throws Exception {

		// Set up the mocks for this test
		// Make sure we get our mock platform when running the test
		final RestRoleManager roleManager = spy(new RestRoleManager(platformMgr.get(), docs));

		assertEquals(NodeRole.REST, roleManager.getRole());

		final List<RoleOSCommand> startCommands = roleManager.getStartCommands();

		assertNotNull(roleManager.getHome());

		assertNotNull(startCommands);
		assertEquals(1, startCommands.size());
		final RoleOSCommand osStartCommand = startCommands.get(0);
		final String expectedStartCmd = roleManager.getHome() + "/bin/./dg_rest.sh start " + roleManager.getHome() + " 8080";

		assertEquals(expectedStartCmd, osStartCommand.toString());

		// Test the stop commands
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();
		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		final RoleOSCommand osStopCommand = stopCommands.get(0);
		final String expectedStopCommand = roleManager.getHome() + "/bin/./dg_rest.sh stop";

		assertEquals(expectedStopCommand, osStopCommand.toString());

		NodeRoleStatus status = roleManager.getStatus();
		assertEquals(State.STOPPED, status.getState());
	}

	@Test
	public void testNodeRoleElasticSearch() throws Exception {

		final ElasticSearchRoleManager roleManager = new ElasticSearchRoleManager(platformMgr.get(), docs);
		PlatformNode node = mock(PlatformNode.class);
		roleManager.init(node);
		List<RoleOSCommand> startCommands = roleManager.getStartCommands();

		assertNotNull(roleManager.getHome());

		assertNotNull(startCommands);
		assertEquals(1, startCommands.size());
		final RoleOSCommand osStartCmd = startCommands.get(0);
		final String expectedStartCmd = roleManager.getHome() + "/bin/./elasticsearch -d --pidfile ./espid.pid";

		assertEquals(expectedStartCmd, osStartCmd.toString());

		// Verify the stop commands
		final List<RoleOSCommand> stopCommands = roleManager.getStopCommands();
		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		final RoleOSCommand osStopCommand = stopCommands.get(0);
		final String expectedStopCmd = roleManager.getHome() + "/bin/kill -9";

		assertEquals(expectedStopCmd, osStopCommand.toString());

	}

	@Test
	public void testNodeRoleKafka() throws Exception {

		final KafkaRoleManager roleManager = new KafkaRoleManager(platformMgr.get(), docs);

		assertNotNull(roleManager.getHome());

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull(startCommands);
		assertEquals(1, startCommands.size());
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/bin/./kafka-server-start.sh -daemon " + roleManager.getHome() + "/config/server.properties";

			assertEquals(expected, cmd);
		}

		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/bin/./kafka-server-stop.sh";

			assertEquals(expected, cmd);
		}

		procBasedGetStatusTest(roleManager, "kafka.Kafka");
	}

	@Test
	public void testNodeRoleZookeeper() throws Exception {

		final ZookeeperRoleManager roleManager = new ZookeeperRoleManager(platformMgr.get(), docs);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull(roleManager.getHome());

		assertNotNull(startCommands);
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/bin/./zkServer.sh start";

			assertEquals(expected, cmd);
		}

		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/bin/./zkServer.sh stop";

			assertEquals(expected, cmd);
		}

		{
			NodeRoleStatus status = roleManager.getStatus();
			assertEquals(State.STOPPED, status.getState());
		}

		procBasedGetStatusTest(roleManager, "zoo.cfg");
	}

	@Test
	public void testNodeRoleSparkMaster() throws Exception {

		// Set up the mocks for this test
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getHost()).thenReturn("localhost");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_PORT)).thenReturn("1234");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_UI_PORT)).thenReturn("4321");

		final SparkMasterRoleManager roleManager = new SparkMasterRoleManager(platformMgr.get(), docs);
		roleManager.init(platformNode);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull( roleManager.getHome() );

		assertNotNull(startCommands);
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			String expected = EnvironmentSettings.getDgSparkHome() + "/sbin/./start-master.sh --host localhost --port 1234 --webui-port 4321";

			assertEquals(expected, cmd);
		}

		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			String expected = EnvironmentSettings.getDgSparkHome() + "/sbin/./stop-master.sh";

			assertEquals(expected, cmd);
		}

	}

	@Ignore // todo: fix - relies on static PlatformSettingsProvider calls...
	public void testNodeRoleSparkWorker() throws Exception {

		// Set up the mocks for this test
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getHost()).thenReturn("localhost");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_PORT)).thenReturn("1234");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_UI_PORT)).thenReturn("4321");

		final Platform platform = mock(Platform.class);
		when(platform.getNode(NodeRole.SPARK_WORKER)).thenReturn(platformNode);

		doReturn(platform).when(platformMgr.get()).getPlatform();
		final SparkWorkerRoleManager roleManager = spy(new SparkWorkerRoleManager(platformMgr.get(), docs));

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull( roleManager.getHome() );

		assertNotNull(startCommands);
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./start-slave.sh")
				.append(" ")
				.append("localhost:7077")
				.append(" ")
				.append("--webui-port")
				.append(" 4321");

			assertEquals(expected.toString(), cmd);
		}

		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./stop-slave.sh");

			assertEquals(expected.toString(), cmd);
		}

	}

	@Ignore	// todo: fix - relies on static PlatformSettingsProvider calls...
	public void testNodeRoleHdfsName() throws Exception {

		// Set up the mocks for this test
		final PlatformNode platformNode = mock(PlatformNode.class);
		when(platformNode.getHost()).thenReturn("localhost");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_PORT)).thenReturn("1234");
		when(platformNode.getSetting(SparkMaster.SPARK_MASTER_UI_PORT)).thenReturn("4321");

		final HdfsNameRoleManager roleManager = new HdfsNameRoleManager(platformMgr.get(), docs);
		roleManager.init(platformNode);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull(roleManager.getHome());

		assertNotNull(startCommands);
		assertEquals(5, startCommands.size());
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/bin/")
				.append("./hdfs")
				.append(" ")
				.append("namenode")
				.append(" ")
				.append("-format");

			assertEquals(expected.toString(), cmd);
		}
		{
			RoleOSCommand osCommand = startCommands.get(1);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./hadoop-daemon.sh")
				.append(" ")
				.append("--config")
				.append(" ")
				.append("")
				.append("/etc/hadoop")
				.append(" ")
				.append("--script")
				.append(" ")
				.append("hdfs")
				.append(" ")
				.append("start")
				.append(" ")
				.append("namenode")
			;
			assertEquals(expected.toString(), cmd);
		}
		{
			RoleOSCommand osCommand = startCommands.get(2);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./hadoop-daemon.sh")
				.append(" ")
				.append("start")
				.append(" ")
				.append("secondarynamenode")
			;
			assertEquals(expected.toString(), cmd);
		}
		{
			RoleOSCommand osCommand = startCommands.get(3);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/bin/")
				.append("./hdfs")
				.append(" ")
				.append("zkfc")
				.append(" ")
				.append("-formatZK")
			;
			assertEquals(expected.toString(), cmd);
		}
		{
			RoleOSCommand osCommand = startCommands.get(4);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./hadoop-daemon.sh")
				.append(" ")
				.append("start")
				.append(" ")
				.append("zkfc")
			;
			assertEquals(expected.toString(), cmd);
		}

		assertNotNull(stopCommands);
		assertEquals(2, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./hadoop-daemon.sh")
				.append(" ")
				.append("stop")
				.append(" ")
				.append("namenode")
			;

			assertEquals(expected.toString(), cmd);
		}
		{
			RoleOSCommand osCommand = stopCommands.get(1);
			String cmd = osCommand.toString();
			StringBuilder expected = new StringBuilder();
			expected.append("")
				.append("/sbin/")
				.append("./hadoop-daemon.sh")
				.append(" ")
				.append("stop")
				.append(" ")
				.append("secondarynamenode")
			;

			assertEquals(expected.toString(), cmd);
		}

		// test env
		Map<String, String> env = roleManager.getEnv();
		assertNotNull(env);
		assertNotNull(env.get("HADOOP_HOME"));
		assertNotNull(env.get("HADOOP_CONF_DIR"));
		assertNotNull(env.get("HADOOP_PREFIX"));
		assertNotNull(env.get("HADOOP_LIBEXEC_DIR"));

		assertEquals(NodeRole.HDFS_NAME, roleManager.getRole());

	}

	@Test
	public void testNodeRoleHdfsData() throws Exception {

		final HdfsDataRoleManager roleManager = new HdfsDataRoleManager(platformMgr.get(), docs);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull( roleManager.getHome() );

		assertNotNull(startCommands);
		assertEquals(1, startCommands.size());
		assertEquals(roleManager.getHome() + "/sbin/./hadoop-daemon.sh start datanode", startCommands.get(0).toString());

		assertNotNull(stopCommands);
		assertEquals(1, stopCommands.size());
		assertEquals(roleManager.getHome() + "/sbin/./hadoop-daemon.sh stop datanode", stopCommands.get(0).toString());

		assertEquals(NodeRole.HDFS_DATA, roleManager.getRole());

		procBasedGetStatusTest(roleManager, "DataNode");
	}

	@Test
	public void testNodeRoleHdfsJournal() throws Exception {

		final HdfsJournalRoleManager roleManager = new HdfsJournalRoleManager(platformMgr.get(), docs);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();

		assertNotNull( roleManager.getHome() );

		assertEquals(1, startCommands.size());
		{
			RoleOSCommand osCommand = startCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/sbin/./hadoop-daemon.sh start journalnode";

			assertEquals(expected, cmd);
		}

		assertEquals(1, stopCommands.size());
		{
			RoleOSCommand osCommand = stopCommands.get(0);
			String cmd = osCommand.toString();
			String expected = roleManager.getHome() + "/sbin/./hadoop-daemon.sh stop journalnode";

			assertEquals(expected, cmd);
		}

		assertEquals(NodeRole.HDFS_JOURNAL, roleManager.getRole());

		procBasedGetStatusTest(roleManager, "JournalNode");
	}

	@Test
	public void testNodeRoleDefault() throws Exception {

		final DefaultRoleManager roleManager = new DefaultRoleManager(platformMgr.get(), docs);

		List<RoleOSCommand> startCommands = roleManager.getStartCommands();
		List<RoleOSCommand> stopCommands = roleManager.getStopCommands();
		assertNull( roleManager.getHome() );

		assertNull(startCommands);
		assertNull(stopCommands);
		assertEquals(NodeRole.NODE, roleManager.getRole());

		// these are no-op but should not blow up
		roleManager.start();
		roleManager.stop();

		assertEquals(State.UNKNOWN, roleManager.getStatus().getState());
	}

	private Process spawnFakeProcess(String v) throws IOException {
		ProcessBuilder worker = new ProcessBuilder("bash","-c","exec -a " + v + "  sleep 2");
		return worker.start();
	}

	private void procBasedGetStatusTest(RoleManager roleManager, String v) throws Exception {
		NodeRoleStatus status = roleManager.getStatus();
		//assertEquals(RoleStatus.RoleState.STOPPED, status.getState());

		// test status (# of spawned related processes)
		Process p = spawnFakeProcess(v);
		status = roleManager.getStatus();
		assertEquals(State.RUNNING, status.getState());
		p.destroyForcibly();

		Process p1 = spawnFakeProcess(v);
		Process p2 = spawnFakeProcess(v);
		status = roleManager.getStatus();
		assertEquals(State.ERROR, status.getState());
		p1.destroyForcibly();
		p2.destroyForcibly();
	}
}
