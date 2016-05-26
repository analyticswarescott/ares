package com.aw.platform.restcluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import org.junit.Test;

import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.cluster.zk.DefaultClusterProperty;
import com.aw.platform.restcluster.PlatformController.PlatformState;

public class RestClusterTest extends AbstractClusterTest {

	@Test
	public void basicInit() throws Exception {

		RestCluster cluster = new RestCluster(this.platformProvider, "rest cluster");
		cluster.init();

		//set the status message
		((DefaultClusterProperty<String>)cluster.getStatusMessageProperty()).set("testing 123");

		assertEquals(PlatformState.UNKNOWN, cluster.getState());

		//check setting state on a new state node
		checkSetState(cluster, PlatformState.PENDING);

		//and existing state node
		checkSetState(cluster, PlatformState.RUNNING);

		assertEquals("testing 123", cluster.getStatusMessage());

		//check no leader
		assertNull(cluster.getLeader());

		assertNotNull(cluster.getStateProperty());
		assertNotNull(cluster.getStatusMessageProperty());

	}

	private void checkSetState(RestCluster cluster, PlatformState state) throws Exception {

		//make sure state updates correctly
		cluster.setState(state);

		//state should update
		cluster.waitFor(state, 2000L);

		//getState should now reflect the new state
		assertEquals(state, cluster.getState());

	}

	@Test(expected=ClusterException.class)
	public void stateTimeout() throws Exception {

		RestCluster cluster = new RestCluster(this.platformProvider, "rest cluster");
		cluster.init();

		cluster.waitFor(PlatformState.RUNNING, 10L);

	}

	@Test
	public void waitForRunning() throws Exception {

		RestCluster cluster = new RestCluster(this.platformProvider, "rest cluster");
		cluster.init();
		cluster = spy(cluster);

		//should come back right away
		cluster.waitForRunning();

	}

}
