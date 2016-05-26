/*

package com.aw.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.spark.DriverState;
import com.aw.common.spark.DriverStatus;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.platform.roles.Zookeeper;
import com.aw.util.Statics;

public class PlatformControllerTest extends AbstractClusterTest {

	PlatformController controller = null;
	TestDocumentHandler docs = null;
	PlatformMgr mgr = null;
	RestCluster cluster = null;
	LocalRestMember member = null;

	@Rule public TestName name = new TestName();

	@Before
	public void beforeTest() throws Exception {

		mgr = spy(new PlatformMgr());
		docs = new TestDocumentHandler();
		cluster = mock(RestCluster.class);
		member = mock(LocalRestMember.class);
		controller = spy(new PlatformController(cluster, member, mgr));

		//build a test platform
		Platform platform = docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject();
		int port = this.platform.getNode(NodeRole.ZOOKEEPER).getSettingInt(Zookeeper.PORT);
		NodeRoleSettings settings = platform.getNode(NodeRole.ZOOKEEPER).getSettings(NodeRole.ZOOKEEPER);
		settings.put(Zookeeper.PORT, port);

		//use the test platform
		doReturn(platform).when(this.mgr).getPlatform();

	}

    @Test
    public void setPlatformState() throws Exception {
    	controller.setPlatformState(PlatformState.PENDING);

    	//make sure the controller set the state on the cluster
        verify(this.cluster).setState(PlatformState.PENDING);

        //make sure the controller waited for the state to change in the cluster
        verify(this.cluster).waitFor(PlatformState.PENDING, 10000L);
    }

    @Test
    public void setPlatformHealth() {
        // initial health is unknown
        PlatformController.PlatformHealth platformHealth = controller.getPlatformHealth();
        assertEquals(PlatformController.PlatformHealth.UNKNOWN, platformHealth);

        controller.setPlatformHealth(PlatformController.PlatformHealth.GREEN);
        platformHealth = controller.getPlatformHealth();
        assertEquals(PlatformController.PlatformHealth.GREEN, platformHealth);
    }

    @Test
    public void isPlatformStartEnabled() {
        System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, "true");
        assertTrue( PlatformController.isPlatformStartEnabled() );
        System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, "false");
        assertFalse( PlatformController.isPlatformStartEnabled() );
    }

    @Test
    public void verifyPlatformSpec() throws Exception {
    	doReturn(PlatformState.VIABLE).when(this.cluster).getState();
        controller.verifyPlatformSpec();
        cluster.waitFor(PlatformState.VIABLE, 2000L);
		assertEquals(PlatformController.PlatformState.VIABLE, controller.getPlatformState());
    }

    @Test
    public void testSetPlatformStatus() throws Exception {
    	controller.setPlatformStatus("testing 123");
    	assertEquals("testing 123", controller.getPlatformStatus());
    }

    @Test
    public void testStopPlatform() throws Exception {

    	//stopped role status
    	NodeRoleStatus mockStatus = new NodeRoleStatus();
    	mockStatus.setState(State.STOPPED);

    	//mock node client
    	DefaultNodeClient mockClient = mock(DefaultNodeClient.class);
    	doReturn(mockStatus).when(mockClient).changeRoleState(any(NodeRole.class), any(State.class));
    	doReturn(mockClient).when(controller).newNodeClient(any(PlatformNode.class));

    	controller.shutdownPlatform();
    	verify(controller).stopPlatform();
    	verify(mockClient).changeRoleState(NodeRole.ELASTICSEARCH, State.STOPPED);
    }

    @Test
    public void testRegisterDriver() throws Exception {

    	JSONObject registration = new JSONObject();
        registration.put("driver_id", "testId");
        registration.put("driver_root_name", "testRootName");
        registration.put("driver_name", "testName");
        controller.getDrivers().put("testName", new DriverStatus("testName", new Document(), DriverState.UNKNOWN));
        controller.registerDriver(registration);

        //verify we're registered
    	assertEquals(controller.getDrivers().get("testName").get_state(), DriverState.REGISTERED);

    }

    @Test(expected=RuntimeException.class)
    public void testRegisterDriver_missing() throws Exception {

    	JSONObject registration = new JSONObject();
        registration.put("driver_id", "testId");
        registration.put("driver_root_name", "testRootName");
        registration.put("driver_name", "testMissingDriver");
        controller.getDrivers().put("testName", new DriverStatus("testName", new Document(), DriverState.UNKNOWN));
        controller.registerDriver(registration);

    }

}

*/
