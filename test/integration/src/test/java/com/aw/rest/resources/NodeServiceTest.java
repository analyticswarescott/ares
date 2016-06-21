package com.aw.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.aw.test.nodeservice.TestRoleManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.NodeServiceWrapper;
import com.aw.TestDependencies;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.RestClient;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.RoleManager;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;
import com.aw.test.nodeservice.TestNodeBinder;
import com.aw.util.ListMap;
import com.aw.util.Statics;

/**
 * Test the node service
 *
 * @author jlehmann
 *
 */
public class NodeServiceTest extends BaseIntegrationTest {

/*	//don't start rest at all here
	@Override
	protected void startRest() throws Exception {
		//don't start rest for this test
	}*/

	@Override
	protected void stopRest() throws Exception {
		//don't stop rest for this test
	}

	@Override
	protected boolean usesSpark() {
		return true;
	}

	@Override
	protected boolean startsSpark() {
		return true;
	}

	@Test
	public void nodeService() throws Exception {

		//configure the role managers
		System.setProperty("DG_HOST", "localhost");

		//use test role managers
		System.setProperty(EnvironmentSettings.Setting.DEPENDENCY_INJECTOR.name(), TestNodeBinder.class.getName());

		//start the node service
		NodeServiceWrapper wrapper = null;

		try {

			//start the node service
			//wrapper = new NodeServiceWrapper("../../node_service", 9101);
			//wrapper.start();

			Map<NodeRole, RoleManager> mgrs = TestNodeBinder.getNodeManager().getRoleManagers();
			while (mgrs.size() == 0) {
				Thread.sleep(1000L);
				mgrs = TestNodeBinder.getNodeManager().getRoleManagers();
			}
			NodeRole[] roles = mgrs.keySet().toArray(new NodeRole[mgrs.size()]);
			for (NodeRole role : roles) {
				mgrs.put(role, new TestRoleManager(TestDependencies.getPlatformMgr().get(), role, TestDependencies.getDocs()));
			}

			//configure the roles for the node
			new PlatformController(mock(RestCluster.class), mock(LocalRestMember.class), TestNodeBinder.getPlatformMgr().get()).configurePlatformNodes();

			//test apis
			RestClient client = new RestClient(NodeRole.NODE, TestDependencies.getPlatform().get());

			//get status of roles
			PlatformNode node = TestDependencies.getPlatform().get().getNode(NodeRole.NODE);
			for (NodeRole role : node.getRoles()) {
				logger.info("calling status on role " + role);
				HttpResponse response = client.execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/node/"+role.name().toLowerCase()+"/status");
				if (role != NodeRole.NODE) {
					assertEquals("expect a 200 from status for role " + role, response.getStatusLine().getStatusCode(), 200);
				}
				HttpClientUtils.closeQuietly(response);

			}

		} finally {

			//try to stop it
			if (wrapper != null) {
				wrapper.stop();
			}

		}

		//check calls
		for (RoleManager mgr : TestNodeBinder.getNodeManager().getRoleManagers().values()) {
			if (mgr.getRole() == NodeRole.NODE) {
				continue; //configure currently will not be called on NODE service role
			}
			//check the calls we currently expect to be made
		//	assertEquals("start not called for " + mgr.getRole(), 1, TestRoleManager.m_start.get(mgr.getRole()).size());
//			assertEquals("stop not called for " + mgr.getRole(), 1, TestRoleManager.m_stop.get(mgr.getRole()).size());
			System.out.println("calls for " + mgr.getRole() + " : " + TestRoleManager.configure.get(mgr.getRole()).size());

			assertEquals("configure not called for " + mgr.getRole(), 2, TestRoleManager.configure.get(mgr.getRole()).size());
//			assertEquals("status not called for " + mgr.getRole(), 1, ((TestRoleManager)mgr).m_getStatus);

		}

	}


}
