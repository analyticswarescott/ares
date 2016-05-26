package com.aw.common.cluster.zk;

import javax.inject.Provider;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;

import com.aw.common.inject.TestProvider;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformBuilder;
import com.aw.platform.roles.Zookeeper;

/**
 * Base class for cluster tests
 *
 *
 *
 */
public abstract class AbstractClusterTest {

	private TestingServer zkServer;
	protected Platform platform;
	protected Provider<Platform> platformProvider;

	@Before
	public void before() throws Exception {
		this.zkServer = new TestingServer();
		this.platform = new PlatformBuilder()
				.withNode("localhost", NodeRole.ZOOKEEPER)
				.withSetting(Zookeeper.PORT, this.zkServer.getPort())
				.withSetting(Zookeeper.DG_ROOT_NODE, "/aw")
				.build();
		this.platformProvider = new TestProvider<Platform>(this.platform);
	}

	@After
	public void after() throws Exception {
		this.zkServer.close();
	}

}
