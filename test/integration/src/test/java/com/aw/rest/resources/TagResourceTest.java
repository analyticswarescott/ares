package com.aw.rest.resources;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.document.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jhaight
 */
public class TagResourceTest  extends BaseIntegrationTest {

	@Test
	public void test() throws Exception {
		String tenantId = "1";
		provisionTenant(tenantId);

		String username = UUID.randomUUID().toString();
		addUser(username, tenantId);

		// test get all
		JSONArray list = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tags", JSONArray.class);
		assertTrue("tags list should be empty", list.length() == 0);

		final Document testDoc = new Document(DocumentType.TEST_TYPE, "testing", "Testing", "0", "test", new JSONObject());
		for (int i = 0; i < 10; i++) {
			testDoc.addTag(Tag.valueOf("tag" + i));
		}

		// Insert a test doc with another 5tags of a different type
		final Document testDoc2 = new Document(DocumentType.SIMPLE_RULE, "testing", "Testing", "0", "test", new JSONObject());
		for (int i = 0; i < 5; i++) {
			testDoc2.addTag(Tag.valueOf("differentTag" + i));
		}
		Impersonation.impersonateTenant(tenantId);
		TestDependencies.getDocs().get().createDocument(testDoc);
		TestDependencies.getDocs().get().createDocument(testDoc2);
		Impersonation.unImpersonate();

		// test get all again
		list = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tags", JSONArray.class);
		assertTrue("tags list should have 15 items", list.length() == 15);
		String listString1 = list.toString();

		// test get one
		list = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tags/simple_rule", JSONArray.class);
		assertTrue("tags list should have 5 items", list.length() == 5);
		String listString2 = list.toString();

		// test get one
		list = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tags/test_type", JSONArray.class);
		assertTrue("tags list should have 10 items", list.length() == 10);
		String listString3 = list.toString();

		// check that returned arrays are correct
		for (int i = 0; i < 10; i++) {
			assertTrue(listString1.contains("tag" + i));
			assertTrue(listString3.contains("tag" + i));
			assertFalse(listString2.contains("tag" + i));
		}
		for (int i = 0; i < 5; i++) {
			assertTrue(listString1.contains("differentTag" + i));
			assertTrue(listString2.contains("differentTag" + i));
			assertFalse(listString3.contains("differentTag" + i));
		}
	}
}
