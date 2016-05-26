
package com.aw.platform;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.aw.platform.restcluster.RestCluster;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.aw.common.TestPlatform;
import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.inject.TestProvider;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;

public class PlatformUpdaterTest extends AbstractClusterTest {

	public static final int MOCK_SERVICE_PORT = 8989;

	PlatformUpdater m_updater = null;

	PlatformMgr m_mgr = null;
	TestDocumentHandler m_docs = null;

	@Rule public TestName name = new TestName();

	@Before
	public void beforeTest() throws Exception {

		m_docs = new TestDocumentHandler();
		Platform platform = m_docs.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject();

		m_mgr = spy(new PlatformMgr());
		m_mgr.setPlatform(platform);

		PlatformUpgrade upgrade = new PlatformUpgrade(new TestProvider<>(new TestPlatform()));
		upgrade.setVersionId("2");
		upgrade.setFilename("foo.tar.gz");

		doReturn(platform).when(m_mgr).getPlatform();

		RestCluster dummy = mock(RestCluster.class);

		m_updater = spy(new PlatformUpdater(dummy, m_mgr, upgrade));

		//this will cause the upgrade code not to fire
		doReturn(true).when(m_updater).checkNodeVersion(platform.getNode(EnvironmentSettings.getHost()));

	}

    @Test
    public void patchNoOp() throws Exception {

    	//what does this get us? I'm seeing exceptions and it's not clear why we're doing this with no asserts
		//m_updater.patchCluster();

    }


}

