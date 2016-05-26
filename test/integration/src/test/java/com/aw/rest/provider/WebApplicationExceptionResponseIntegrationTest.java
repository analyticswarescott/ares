package com.aw.rest.provider;

import com.aw.BaseIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Integration test for the exception mapping.
 */
public class WebApplicationExceptionResponseIntegrationTest extends BaseIntegrationTest {

    @Test
    public void test() throws Exception {
    	testResponseWithoutAuthenticating();
    	testResponseWithAuthenticating();
    }



    public void testResponseWithoutAuthenticating() throws Exception {

		// Make a request to a path that doesn't exist, however, since we're not authenticated, we should get a 401 response
    	final HttpResponse response = client().execute(get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/doesnotexist"));
    	assertEquals("Call should have been authenticated", HttpStatus.UNAUTHORIZED_401, response.getStatusLine().getStatusCode());
		assertEquals("{\"message\":\"HTTP 401 Unauthorized\"}", IOUtils.toString(response.getEntity().getContent(), "UTF-8"));

    }

    public void testResponseWithAuthenticating() throws Exception {

		// Provision a tenant so that we can add a user
		String tenantID = "1";
		provisionTenant(tenantID);

		// Add a user so we can perform an authenticated request
		String user = UUID.randomUUID().toString();
		String accessToken = fetchAccessToken("1", user, "test");

		// Verify that we got a 404 Not Found when we request a resource that does not exist
		try (final CloseableHttpResponse response = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/doesnotexist", accessToken)) {
			assertEquals("Path should not have resolved!", HttpStatus.NOT_FOUND_404, response.getStatusLine().getStatusCode());
			assertEquals("{\"message\":\"HTTP 404 Not Found\"}", IOUtils.toString(response.getEntity().getContent(), "UTF-8"));
		}

    }

}
