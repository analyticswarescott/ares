package com.aw.security;

import com.aw.BaseIntegrationTest;
import com.aw.document.Document;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;

public class SecuritySmokeTest extends BaseIntegrationTest {

	@Test
	public void integrationTest() throws Exception {
		testUnauthorizedRequest();
		testLogin();
		testAuthorizedRequest();
		testBadTenant();
	}

    public void testUnauthorizedRequest() throws Exception {
    	//TODO: get access to status code in the framework
        HttpResponse response = unAuthGet(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/bob-0", HttpResponse.class);
        Assert.assertEquals("Expected unauthorized request", 401, response.getStatusLine().getStatusCode());
    }

    public void testLogin() throws Exception {
        String accessToken = addUser("joe", "0");
        Assert.assertNotNull(accessToken);
    }


    public void testAuthorizedRequest() throws Exception {
		String tenantID = "1";
		provisionTenant(tenantID);
		String user = UUID.randomUUID().toString();
		addUser(user, tenantID);
		JSONObject prefs = authGet(user, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + user, JSONObject.class);

		Document doc = new Document(prefs.toString());
		Assert.assertEquals("Expect return to be pre-created for this user from default doc " , doc.getName(), user  );
		Assert.assertNotNull("Expected authorized request", prefs);
	}

	public void testBadTenant() throws Exception {
		String tenantID = "12345";
		//provisionTenant(tenantID);
		String user = UUID.randomUUID().toString();
		addUser(user, tenantID);

		try {
			JSONObject prefs = authGet(user, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + user, JSONObject.class);
		}
		catch (RuntimeException ex) {
			Assert.assertEquals("GET /rest/1.0/user_settings/" + user + ": Unexpected http response: 500 Internal Server Error (content={\"message\":\"Error validating tenant ID 12345\"})",
				ex.getMessage());
				return;
		}

		Assert.fail(" expected exception to be thrown before this");
	}

}
