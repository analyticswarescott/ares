package com.aw.rest.provider;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.aw.common.MockProvider;
import com.aw.platform.PlatformMgr;

/**
 * Tests the {@link javax.ws.rs.WebApplicationException} response mapping.
 */
public class WebApplicationExceptionResponseMapperTest {


	@Test
	public void testExceptionMapping() {

		final WebApplicationExceptionResponseMapper exceptionResponseMapper = new WebApplicationExceptionResponseMapper(new MockProvider<>(PlatformMgr.class));

		for (Response.Status status : Response.Status.values()) {

			// Only test the 4xx family of status codes
			if (Response.Status.Family.CLIENT_ERROR == status.getFamily()) {

				final Response response = exceptionResponseMapper.toResponse(new WebApplicationException(status));
				// Make sure the response code matches what we return
				assertEquals(status.getStatusCode(), response.getStatus());
				// The default message is just the reason phrase from the response status
				assertEquals("{\"message\":\"HTTP " + status.getStatusCode() + " " + status.getReasonPhrase() + "\"}", response.getEntity().toString());

			}
		}

	}

}