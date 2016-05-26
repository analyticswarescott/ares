package com.aw.platform.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.PlatformNode;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.nodes.NodeClient;
import com.aw.platform.nodes.NodeClientFactory;
import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.PlatformController.PlatformState;

public class DefaultPlatformStatusTest extends AbstractKafkaZkUnitTest {

	DefaultNodeClient mockNodeClient;
	PlatformNode node;
	List<NodeStatus> statuslist;

	@Test
	public void test() throws Exception {

		//mock platformMgr
		RestCluster mockCluster = mock(RestCluster.class);
		doReturn(PlatformState.RUNNING).when(mockCluster).getPlatformState();
		doReturn("this is only a test").when(mockCluster).getPlatformStatus();

		mockNodeClient = mock(DefaultNodeClient.class);
		node = new DefaultPlatformNode();
		NodeStatus testNs = new NodeStatus();
		statuslist = new ArrayList<NodeStatus>();
		statuslist.add(testNs);

		doReturn(statuslist).when(mockNodeClient).getNodeStatus(any(Instant.class));

		Tenant tenant = Tenant.forId("1");
		Document doc = new Document();
		doc.setBodyFromObject(tenant);

		TestDocumentHandler docs = spy(new TestDocumentHandler());
		doReturn(Collections.singleton(doc)).when(docs).getAllTenants();

		DefaultPlatformStatus status = new DefaultPlatformStatus(getPlatform(), docs, mockCluster);
		//doReturn(mockNodeClient).when(status).newNodeClient(any(PlatformNode.class));

		NodeClientFactory ncf = new NodeClientFactory() {
			@Override
			public NodeClient newNodeClient(PlatformNode node) {
				return mockNodeClient;
			}
		};

		ZkAccessor zk = new DefaultZkAccessor(getPlatform(), Hive.TENANT);
		status.collect(zk, ncf , Instant.now());



		//test to/from json
		String statusJson = JSONUtils.objectToString(status);

		status = JSONUtils.objectFromString(statusJson, DefaultPlatformStatus.class);

		//just make sure the top level status contains what it should
		assertTrue(status.getRoleStatus().containsKey(NodeRole.KAFKA));
		assertTrue(status.getRoleStatus().containsKey(NodeRole.ZOOKEEPER));
		assertEquals(PlatformState.RUNNING, status.getState());
		assertEquals("this is only a test", status.getStatusMessage());

	}

}
