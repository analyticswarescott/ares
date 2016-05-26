package com.aw.rest.resources.ping;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * @author jhaight
 */
public class PingResourceTest {

	private PingResource pingResource;

	@Before
	public void setup() {
		pingResource = new PingResource();
	}

	@Test
	public void testPing() throws Exception {
		Response response = pingResource.ping();
		assertEquals(200, response.getStatus());
		assertEquals("pong", response.getEntity());
	}
}
