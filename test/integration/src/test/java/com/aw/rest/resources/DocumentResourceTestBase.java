package com.aw.rest.resources;

import com.aw.BaseIntegrationTest;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Extend this class to test basic CRUD operations for a particular doc type.
 *
 * @author jlehmann
 *
 */
public abstract class DocumentResourceTestBase extends BaseIntegrationTest {

	@Test
	public void test() throws Exception {

        String tenantID = "1";
        provisionTenant(tenantID);

        String username = UUID.randomUUID().toString();
        addUser(username, tenantID);

        //test get all
        JSONArray list = authGet(username, getBasePath(), JSONArray.class);
        assertTrue(getBasePath() + " list empty", list.length() > 0);

        //test get specific
        JSONObject doc = authGet(username, getBasePath() + "/" + list.getJSONObject(0).getString("name"), JSONObject.class);
        assertTrue(getBasePath() + " body missing", doc.getJSONObject("body") != null);

        //change the name, can't modify official versions
        doc.put("name", (String)null);

        //test doc creation
        JSONObject object1 = authPost(username, getBasePath(), doc, JSONObject.class);
        assertNotNull(getBasePath() + " name wrong", object1.get("name"));
        assertEquals(getBasePath() + " version wrong", 1, object1.getInt("version"));
        assertEquals(getBasePath() + " author wrong", username, object1.getString("author"));

        //test doc update
    	doc.put("display_name", "my cool changed doc");
    	JSONObject object2 = authPut(username, getBasePath() + "/" + object1.getString("name"), doc, JSONObject.class);
        assertEquals(getBasePath() + " modified name wrong", object1.get("name"), object2.get("name"));
        assertEquals(getBasePath() + " modified display_name wrong", "my cool changed doc", object2.get("display_name"));
        assertEquals(getBasePath() + " modified version wrong", 2, object2.getInt("version"));
        assertEquals(getBasePath() + " modified author wrong", username, object2.getString("author"));

		//test doc patch
		JSONObject patchData = new JSONObject("{\"field\":\"abc\"}");
		JSONObject object3 = authPatch(username, getBasePath() + "/" + object2.getString("name") + "/body?replace=true", patchData, JSONObject.class);
		assertEquals(getBasePath() + " failed to patch grouping", patchData.toString(), object3.getJSONObject("body").toString());

		//test deep doc patch
		JSONObject object4 = authPatch(username, getBasePath() + "/" + object3.getString("name") + "/body/body_level_1/body_level_2/object?replace=true", patchData, JSONObject.class);
		assertEquals(getBasePath() + " failed to deep patch grouping", patchData.toString(), object4.getJSONObject("body")
			.getJSONObject("body_level_1").getJSONObject("body_level_2").getJSONObject("object").toString());

        //test delete - start by getting doc list before delete
        JSONArray list1 = authGet(username, getBasePath(), JSONArray.class);

        //now do the delete
        HttpResponse ret = authDelete(username, getBasePath() + "/" + object1.getString("name"), HttpResponse.class);
        assertTrue(getBasePath() + " delete unsuccessful", HttpStatus.isSuccess(ret.getStatusLine().getStatusCode()));

        //now make sure the deleted document no longer exists
        JSONArray list2 = authGet(username, getBasePath(), JSONArray.class);
        assertTrue(getBasePath() + " single document should have been deleted from list", list1.length() == list2.length() + 1);
        for (int curDoc=0; curDoc<list2.length(); curDoc++) {
        	assertNotEquals(getBasePath() + " document should no longer be in the list after delete", list2.getJSONObject(curDoc).get("name"), object1.get("name"));
        }

    }

	/**
	 * @return The base path for this document test
	 */
	protected abstract String getBasePath();

}
