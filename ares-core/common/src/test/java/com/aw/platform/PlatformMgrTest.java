package com.aw.platform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.common.messaging.Message;
import com.aw.common.messaging.Messenger;
import com.aw.common.messaging.StringMessage;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;
import com.aw.util.Statics;


public class PlatformMgrTest {
	public static final int MOCK_SERVICE_PORT = 8989;

	PlatformMgr mgr = null;
	LocalRestMember member = null;
	TestDocumentHandler docHandler = null;
	TestMessenger messenger;
	DocumentMgr docMgr;

	@Before
	public void setup() throws Exception {

		//suppress error logging
		System.setProperty(Statics.PROP_LOG_PLATFORM_ERRORS, "false");

		//create stubs
		this.messenger = new TestMessenger();
		this.mgr = spy(new PlatformMgr(messenger));
		this.docHandler = new TestDocumentHandler();
		this.docMgr = mock(DocumentMgr.class);
		doReturn(docHandler).when(docMgr).getDocHandler();
		doReturn(docHandler).when(docMgr).getSysDocHandler();
		this.member = spy(new LocalRestMember(mock(RestCluster.class), mock(TenantMgr.class), mgr, new TestProvider<>(docHandler), new TestProvider<>(docMgr)));

		doReturn("0").when(mgr).getTenantID();
		doReturn("aw").when(mgr).getUserID();

		Platform platform = docHandler.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject();

		//wire them up
		doReturn(platform).when(mgr).getPlatform();

	}

	@Test
	public void handleLog() throws Exception {

		String logMessage = UUID.randomUUID().toString();
		this.mgr.handleLog(logMessage, NodeRole.REST);
		assertFalse(this.messenger.lastMessage instanceof StringMessage); //should not send to messenger for regular log messages

	}

	@Test
	public void handleException() throws Exception {

		String exceptionMessage = UUID.randomUUID().toString();
		Exception exception = new Exception(exceptionMessage);
		mgr.handleException(exception, NodeRole.REST);
		assertTrue(this.messenger.lastMessage instanceof StringMessage);

	}

	@Test
	public void handleExceptionWithHost() throws Exception {

		String exceptionMessage = UUID.randomUUID().toString();
		Exception exception = new Exception(exceptionMessage);
		mgr.handleError("localhost", exception, NodeRole.REST);
		assertTrue(this.messenger.lastMessage instanceof StringMessage);
		assertTrue(this.messenger.lastMessage.getPayload().contains(exceptionMessage));

	}

	private static void setConfDir() {
		if(  System.getenv("CONF_DIRECTORY") == null ) {
			File dgCoreDir = new File( new File(new File("").getAbsolutePath()).getParentFile().getParentFile(), Statics.CONFIG_DIR_NAME );
			File confDir = new File( dgCoreDir, "conf");
			System.setProperty("CONF_DIRECTORY", confDir.getAbsolutePath());
		}
	}

	@Test
	public void isPlatformComplete() throws Exception {
		//test platform should be complete
		assertTrue( mgr.isPlatformComplete() );
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


	}

}
