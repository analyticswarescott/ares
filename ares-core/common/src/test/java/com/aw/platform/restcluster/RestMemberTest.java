/*
package com.aw.platform.restcluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.aw.common.TestPlatform;
import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.inject.TestProvider;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.JSONUtils;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.nodes.NodeManagerTest;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.tenant.TenantMgr;

public class RestMemberTest extends AbstractClusterTest {

	@Rule public TestName testName = new TestName();

	@Before
	public void init() throws Exception {
		NodeManagerTest.setConfDir();
	}

	@Test
	public void startStop() throws Exception {

		try (RestCluster cluster = new RestCluster(this.platformProvider, "rest cluster")) {

			cluster.init();

			PlatformMgr mgr = spy(new PlatformMgr());
			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			LocalRestMember member = new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr));
			member = spy(member);
			doNothing().when(member).ensureConfigDbRunning();

			member.initialize();
			member.tryToJoin();

			//make sure we joined
			assertTrue(member.isActive());

			member.leave();

			assertFalse(member.isActive());

		}

	}

	@Test
		(expected=ClusterException.class)
	public void startStopIncomplete() throws Exception {

		try (RestCluster cluster = new RestCluster(this.platformProvider, "rest cluster")) {

			cluster.init();

			PlatformMgr mgr = spy(new PlatformMgr());

			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			LocalRestMember member = spy(new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr)));

			member.initialize();

			//set first node.. should cause an exception
			System.setProperty(EnvironmentSettings.Setting.FIRST_NODE.name(), "true");

			member.tryToJoin();

			//make sure we didn't join
			assertFalse(member.isActive());

			member.leave();

		}

	}

	@Test
	public void executeLeadership_stopLeadership() throws Exception {

		try (RestCluster cluster = spy(new RestCluster(this.platformProvider, "rest cluster"))) {

			cluster.init();

			PlatformMgr mgr = spy(new PlatformMgr());

			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			final LocalRestMember member = spy(new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr)));

			//mock controller
			PlatformController controller = mock(PlatformController.class);
			doReturn(controller).when(member).newController();

			//execute leadership
			Thread runningThread = new Thread(() -> member.executeLeadership(cluster));
			runningThread.start();

			//make sure we become master - leader will notify the cluster
			synchronized (member) {

				//wait for member to become leader
				if (!member.isMaster()) {
					member.wait(2000L);
				}

				//make sure we think we're master
				assertTrue(member.isMaster());


			}

			//request platform state
			member.requestPlatformState(PlatformState.RUNNING);

			//make sure platform was started
			cluster.waitFor(PlatformState.RUNNING, 1000L);

			//now stop leadership
			member.stopLeadership(cluster);

			//make sure the running thread dies
			runningThread.join(2000L);

			//make sure executeLeadership no longer runs after stopLeadership is called
			assertFalse(runningThread.isAlive());

		}

	}

	@Test
	public void startFirstNode() throws Exception {

		try (RestCluster cluster = spy(new RestCluster(this.platformProvider, "rest cluster"))) {

			cluster.init();

			PlatformMgr mgr = mock(PlatformMgr.class);
			doReturn(new TestPlatform()).when(mgr).getPlatform();

			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			final LocalRestMember member = spy(new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr)));

			//mock controller
			PlatformController controller = mock(PlatformController.class);
			doReturn(controller).when(member).newController();
			doNothing().when(cluster).leave(member);

			member.startFirstNode();

			//when first node is started, we should start zookeeper
			//	verify(controller).startZookeeper();

			member.leave();

		}

	}

	@Test(expected=ClusterException.class)
	public void requestPlatformState() throws Exception {

		try (RestCluster cluster = spy(new RestCluster(this.platformProvider, "rest cluster"))) {

			cluster.init();

			PlatformMgr mgr = mock(PlatformMgr.class);

			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			final LocalRestMember member = spy(new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr)));

			//mock controller
			PlatformController controller = mock(PlatformController.class);
			doReturn(controller).when(member).newController();

			//should fail with cluster exception
			member.requestPlatformState(PlatformState.STOPPED);

		}

	}



	@Test
	public void getHost() throws Exception {

		LocalRestMember member = new LocalRestMember(null, null, null, null, null);
		assertEquals(EnvironmentSettings.getHost(), member.getHost());

	}

	@Test
	public void platformChangeStart() throws Exception {

		try (RestCluster cluster = spy(new RestCluster(this.platformProvider, "rest cluster"))) {

			cluster.init();

			PlatformMgr mgr = mock(PlatformMgr.class);
			doReturn(false).when(mgr).isPlatformComplete();
			doReturn(platform).when(mgr).getPlatform();

			TestDocumentHandler docs = new TestDocumentHandler();
			mgr.setPlatform(docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject());

			DocumentMgr docMgr = spy(new DocumentMgr(cluster, mgr, mock(DocumentJDBCProvider.class)));
			doNothing().when(docMgr).initDocuments();

			final LocalRestMember member = spy(new LocalRestMember(cluster, mock(TenantMgr.class), mgr, new TestProvider<DocumentHandler>(docs), new TestProvider<DocumentMgr>(docMgr)));

			//mock controller
			PlatformController controller = mock(PlatformController.class);
			doReturn(controller).when(member).newController();

			member.onPlatformChange(mgr.getPlatform());

			//make sure we checked if we should start the platform on platform change
			verify(mgr).isPlatformComplete();

		}

	}

	@Test
	public void toFromJson() throws Exception {

		//test ability to serialize to/from json and maintain host property, which is needed in production
		System.setProperty("ARES_HOST", "1.2.3.4");

		LocalRestMember member = new LocalRestMember(null, null, null, null, null);

		String json = JSONUtils.objectToString(member);

		RestMember fromJson = JSONUtils.objectFromString(json, RestMember.class);

		//only thing that can be set, check it
		assertEquals("1.2.3.4", fromJson.getHost());


	}
}
*/
