package com.aw.platform.restcluster;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.common.messaging.Message;
import com.aw.common.messaging.Messenger;
import com.aw.common.util.TestRestClient;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.tenant.TenantMgr;
import com.aw.util.Statics;

public class LocalRestMemberTest {

/*	PlatformMgr mgr = null;
	LocalRestMember member = null;
	TestDocumentHandler docHandler = null;
	TestRestClient restClient = null;
	TestMessenger messenger;
	DocumentMgr docMgr;

	@Before
	public void setup() throws Exception {

		//suppress error logging
		System.setProperty(Statics.PROP_LOG_PLATFORM_ERRORS, "false");

		//create stubs
		this.messenger = new TestMessenger();
		this.mgr = mock(PlatformMgr.class);
		this.docHandler = new TestDocumentHandler();
		this.docMgr = mock(DocumentMgr.class);
		doReturn(docHandler).when(docMgr).getDocHandler();
		doReturn(docHandler).when(docMgr).getSysDocHandler();
		this.member = spy(new LocalRestMember(mock(RestCluster.class), mock(TenantMgr.class), mgr, new TestProvider<>(docHandler), new TestProvider<>(docMgr)));

		doReturn("0").when(mgr).getTenantID();
		doReturn("aw").when(mgr).getUserID();

		Platform platform = docHandler.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject();
		this.restClient = new TestRestClient(200, "test", platform);

		//wire them up
		doReturn(restClient).when(member).getLeaderClient();
		doReturn(platform).when(mgr).getPlatform();

	}

	@Test
	public void newNode() throws Exception {

		// show that we can provision a new node without explosion
		PlatformNode node = member.newNode("localhost");
		assertNotNull(node);

		//make sure the node is in the new platform
		Platform newPlatform = docHandler.getLastUpdate().getBodyAsObject();
		assertNotNull(newPlatform.getNode("localhost"));

	}

	private class TestMessenger implements Messenger<String> {

		private Message<String> lastMessage;

		@Override
		public void send(Message<String> message) throws IOException {
			this.lastMessage = message;
		}

		@Override
		public void close() {
		}


	}*/

}
