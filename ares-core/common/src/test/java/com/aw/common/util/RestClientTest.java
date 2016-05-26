package com.aw.common.util;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import com.aw.common.system.EnvironmentSettings.Setting;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformBuilder;
import com.aw.platform.PlatformClient;
import com.aw.platform.roles.Rest;

public class RestClientTest {

	@Test(expected=NullPointerException.class)
	public void test_nullPlatformNoEnv() throws Exception {

		//set the environment up
		System.setProperty(Setting.FIRST_NODE_CONNECT.name(), null);

		PlatformClient client = new PlatformClient((Platform)null);

		client.get("/test/path");

	}

	@Test
	public void test_nullPlatformWithEnv() throws Exception {

		//set the environment up
		System.setProperty(Setting.FIRST_NODE_CONNECT.name(), "host:1234");

		RestClient client = new RestClient(NodeRole.REST, (Platform)null) {
			@Override
			protected org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest request) throws Exception {
				HttpGet get = (HttpGet)request;
				assertEquals("host", get.getURI().getHost());
				assertEquals(1234, get.getURI().getPort());
				return new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "testing 123"));
			}
		};

		client.get("/test/path");

	}

	@Test
	public void test_withPlatform() throws Exception {

		//set the environment up
		System.setProperty(Setting.FIRST_NODE_CONNECT.name(), "host:1234");

		RestClient client = new RestClient(NodeRole.REST, new PlatformBuilder().withNode("host1", NodeRole.REST).withSetting(Rest.PORT, 2345).build()) {
			@Override
			protected org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest request) throws Exception {
				HttpGet get = (HttpGet)request;
				assertEquals("host1", get.getURI().getHost());
				assertEquals(2345, get.getURI().getPort());
				return new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "testing 123"));
			}
		};

		client.get("/test/path");

	}

}
