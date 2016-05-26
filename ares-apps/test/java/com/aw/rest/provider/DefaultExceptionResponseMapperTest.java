package com.aw.rest.provider;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.aw.common.MockProvider;
import com.aw.platform.PlatformMgr;

/**
 * Tests the default error response mapping.
 */
public class DefaultExceptionResponseMapperTest {

	@Test
	public void testResponseMapping() {

		final MockProvider<PlatformMgr> platformMgr = new MockProvider<>(PlatformMgr.class);
		final Response response = new DefaultExceptionResponseMapper(platformMgr).toResponse(new Exception());

		assertEquals(500, response.getStatus());
		assertEquals("{\"message\":\"Internal Server Error\"}", response.getEntity().toString());

	}

}