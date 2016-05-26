package com.aw.platform.restcluster;

import java.io.InputStream;

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
		super((Platform)null);
	}

	@Override
	public void cleanup() throws Exception {

	}

	@Override
	public boolean isMaster() {
		throw new UnsupportedOperationException("isMaster not yet implemented for remote rest member");
	}


	public HttpResponse execute(HttpMethod method, String path, InputStream payload) throws Exception {
		return execute(method, path, new InputStreamEntity(payload, -1L));
	}

	@Override
	public HttpResponse execute(HttpMethod method, String path, HttpEntity payload) throws Exception {


		logger.warn("in RR Member execute");

		//make sure node is set
		if (specificNode == null) {
			logger.warn(" debug not set in RR Member m_host is " + m_host);
			specificNode = platform.getNode(m_host);
		}

		//if it's still null, error
		if (specificNode == null) {
			throw new NodeNotFoundException("couldn't find remote rest member to communicate with: " + m_host);
		}


		logger.debug("about to call RestClient execute method");
		return super.execute(method, path, payload);

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
		return platform;
	}
}
