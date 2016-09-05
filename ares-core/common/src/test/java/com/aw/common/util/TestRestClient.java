package com.aw.common.util;

import org.apache.commons.pool.ObjectPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;

import javax.inject.Provider;

import static org.mockito.Mockito.mock;

/**
 * Rest client test stub
 *
 * TODO: put this in a unit test package for common test utilities
 *
 *
 *
 */
public class TestRestClient extends PlatformClient {

	public TestRestClient(int responseCode, String responsePhrase, Provider<Platform> platform) {
		super(platform);
		m_response = new DefaultRestResponse(new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, responseCode, responsePhrase)), mock(ObjectPool.class), mock(DefaultHttpClient.class));
	}

	public RestResponse execute(HttpMethod method, String path, HttpEntity payload)  {
		m_lastMethod = method;
		m_lastPath = path;
		m_lastPayload = payload;
		return m_response;
	}

	public void setResponse(HttpResponse response) { m_response = new DefaultRestResponse(response, mock(ObjectPool.class), mock(DefaultHttpClient.class)); }
	RestResponse m_response = null;

	public HttpMethod getLastMethod() { return m_lastMethod; }
	HttpMethod m_lastMethod;

	public String getLastPath() { return m_lastPath; }
	String m_lastPath;

	public HttpEntity getLastPayload() { return m_lastPayload; }
	HttpEntity m_lastPayload;

}
