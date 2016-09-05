package com.aw.platform.restcluster;

import java.io.InputStream;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.RestResponse;
import com.aw.platform.PlatformMgr;
import com.google.inject.Provider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.cluster.ClusterException;
import com.aw.common.util.HttpMethod;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.exceptions.NodeNotFoundException;

/**
 * A rest member that is not the local rest member.
 *
 *
 *
 */
public class RemoteRestMember extends PlatformClient implements RestMember, Provider<Platform> {

	public static Logger logger = LoggerFactory.getLogger(RemoteRestMember.class);

	public RemoteRestMember() {
		super((PlatformMgr)null);
	}

	@Override
	public void cleanup() throws Exception {

	}

	@Override
	public boolean isMaster() {
		throw new UnsupportedOperationException("isMaster not yet implemented for remote rest member");
	}


	public RestResponse execute(HttpMethod method, String path, InputStream payload)  {
		return execute(method, path, new InputStreamEntity(payload, -1L));
	}

	@Override
	public RestResponse execute(HttpMethod method, String path, HttpEntity payload)  {


		logger.warn("in RR Member execute");

		//make sure node is set
		if (specificNode == null) {
			logger.warn(" debug not set in RR Member m_host is " + m_host);
			specificNode = platform.get().getNode(m_host);
		}


		logger.debug("about to call RestClient execute method");
		try {
			return super.execute(method, path, payload);
		} catch (ProcessingException e) {
			throw new RuntimeException((e));
		}

	}

	@Override
	public void upgrade(PlatformUpgrade upgrade) throws ClusterException {

		logger.debug(" hit upgrade in RR member");

		try {

			this.execute(HttpMethod.POST, ADMIN_BASE_PATH + "/platform/patch/" + upgrade.getVersionId(), upgrade.getPatchFile(this).getInputStream());


		} catch (Exception e) {
			throw new ClusterException("error posting upgrade to master", e);
		}

	}


	public String getHost() { return m_host; }
	public void setHost(String host) { m_host = host; }
	private String m_host;

	@Override
	public Platform get() {
		return platform.get();
	}
}
