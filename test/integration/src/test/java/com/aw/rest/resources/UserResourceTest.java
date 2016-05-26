package com.aw.rest.resources;

import com.aw.BaseIntegrationTest;
import com.aw.util.Statics;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author jhaight
 */
public class UserResourceTest extends BaseIntegrationTest {

	@Test
	public void testUserResource() throws Exception {
		String testUser1 = "bob-1";
		String testUser2 = "bob-0";
		provisionTenant("1");
		provisionTenant("2");
		provisionTenant("5");
		setThreadSecurity("1", "1", testUser1);
		addUser(testUser1, "1");
		addUser(testUser2, "0");


		HttpResponse response = authGet(testUser1, Statics.VERSIONED_REST_PREFIX + "/users", HttpResponse.class);
		String json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		JSONArray users = new JSONArray(json);
		assertEquals("bob-1", users.getString(0));
		assertEquals("jane-1", users.getString(1));
		assertEquals("jack-1", users.getString(2));
		assertEquals("joe-1", users.getString(3));

		response = authGet(testUser2, Statics.VERSIONED_REST_PREFIX + "/users/" + testUser2 + "/tenants", HttpResponse.class);
		assertEquals(200, response.getStatusLine().getStatusCode());
		json = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		JSONArray tenants = new JSONArray(json);
		assertEquals(4, tenants.length());
	}
}
