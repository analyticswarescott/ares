package com.aw.rest.resources;

import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.common.util.JSONUtils;
import com.aw.util.Statics;

/**
 * Tests the ability to save/edit/retrieve a user's settings.
 */
public class UserSettingsResourceTest extends BaseIntegrationTest {

	@Test
	public void testUserSettings() throws Exception {


		final String tenantID = "1";
		provisionTenant(tenantID);
		final String userID = "test";
		addUser(userID, tenantID);
		setThreadSecurity(tenantID, userID, userID);

		final String endpointPath = Statics.VERSIONED_REST_PREFIX + "/user_settings/" + userID;

		// Default settings
		JSONObject newSetting = new JSONObject();
		newSetting.put("default_workspace_name", "default");

		JSONObject response = authGet(userID, endpointPath, JSONObject.class).getJSONObject("body");

		// Make sure the default settings are retrieved properly
		assertTrue(JSONUtils.jsonEquals(newSetting, response));

		// Add a couple of settings
		newSetting.put("test_setting", true);
		final JSONObject nestedSetting = new JSONObject();
		nestedSetting.put("nested_value1", 42);
		nestedSetting.put("nested_value2", "testing1");
		newSetting.put("nested_setting", nestedSetting);

		JSONObject patched = authPatch(userID, endpointPath + "/body", newSetting, JSONObject.class).getJSONObject("body");
		response = authGet(userID, endpointPath, JSONObject.class).getJSONObject("body");

		// Make sure the user settings were stored correctly
		assertTrue(JSONUtils.jsonEquals(patched, response));

		// Make a change to one property in the nested_setting, the other properties should remain the same
		JSONObject nestedChange = new JSONObject();
		final JSONObject nestedValueChange = new JSONObject();
		nestedValueChange.put("nested_value2", "testing2");
		nestedChange.put("nested_setting", nestedValueChange);

		patched = authPatch(userID, endpointPath, newSetting, JSONObject.class).getJSONObject("body");
		response = authGet(userID, endpointPath, JSONObject.class).getJSONObject("body");
		// Make sure the user settings were stored correctly
		assertTrue(JSONUtils.jsonEquals(response, patched));

	}
}