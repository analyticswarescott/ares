package com.aw.rest.resources;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.aw.TestDependencies;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.util.Statics;

public class WorkspaceResourceTest extends DocumentResourceTestBase {

	public final String baseURI = com.aw.util.Statics.VERSIONED_REST_PREFIX + "/workspaces/";

	private static final String TENANT = "2";

	@Override
	protected String getBasePath() {
		return com.aw.util.Statics.VERSIONED_REST_PREFIX + "/workspaces";
	}

	@Test
	@Override
	public void test() throws Exception {

		//test basic workspace document functionality
		super.test();

		//now test things specific to workspaces

		provisionTenant(TENANT);

		testSwagger();

		validateDefaultWorkspace();

		testPostWorkspace();

		testPutWorkspace();

		newDgWorkspace();

		testFetchWorkspaceHudDocs();

	}

	public void testSwagger() throws Exception {

		String tenantID = TENANT;

		//make sure we get something
		String username = UUID.randomUUID().toString();
        addUser(username, tenantID);

        HttpResponse response = authGet(username, Statics.REST_PREFIX + "/swagger.json", HttpResponse.class);
        assertTrue(HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode()));

	}

	public void validateDefaultWorkspace() throws Exception {

		String username = UUID.randomUUID().toString();
		String accessToken = fetchAccessToken(TENANT, username, "test");

		// asserts that a workspace can be retrieved for the user who is owner
		// of the access token
		Document workspace = assertWorkspaceFor(accessToken, username);
		Assert.assertEquals("aw", workspace.getAuthor());
	}

	public void testPostWorkspace() throws Exception {
		String tenantID = TENANT;

		// setup users and their access tokens
		String userOne = UUID.randomUUID().toString();
		String userOneAccessToken = fetchAccessToken(TENANT, userOne, "test");
		String userTwo = UUID.randomUUID().toString();
		String userTwoAccessToken = fetchAccessToken(TENANT, userTwo, "test");

		// fetch both users' workspace
		Document userOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);
		Document userTwoWorkspace = assertWorkspaceFor(userTwoAccessToken, userTwo);

		// assert that they are *identical* because both users have not
		// called save on the workspace, and therefore have both been
		// assigned the default tenant workspace

		// 1. the docs should be identical (the tenant's doc)
		Assert.assertEquals("Expected the two users to have the same workspace doc -- the shared tenant one", userOneWorkspace.getBody().toString(),
				userTwoWorkspace.getBody().toString());
		// 2. the owning user should be identical (the tenant)
		Assert.assertEquals("Expected the two users to have the same USER_ID in the envelope -- the shared tenant one", userOneWorkspace.getAuthor(), userTwoWorkspace.getAuthor());

		// save the first user's workspace: POST /workspaces
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		userOneWorkspace.setName(null);
		HttpPost uri = post(baseURI, userOneWorkspace.toJSON());
		uri.addHeader("accessToken", userOneAccessToken);
		HttpResponse response = httpClient.execute(uri);
		Assert.assertEquals("Expected successful workspace POST", 200, response.getStatusLine().getStatusCode());

		Document updatedUserOneWorkspace = new Document((JSONObject) getJSONEntity(response));
		Assert.assertEquals(userOne, updatedUserOneWorkspace.getAuthor());

		// change the user's default workspace: PATCH /users/me/prefs
		JSONObject changeDefaultWorkspace = new JSONObject();
		JSONObject changeDefaultWorkspaceBody = new JSONObject();
		changeDefaultWorkspace.put("body", changeDefaultWorkspaceBody);
		changeDefaultWorkspaceBody.put("default_workspace_name", updatedUserOneWorkspace.getName());
		HttpPatch patch = patch(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + userOne, changeDefaultWorkspace);
		patch.addHeader("accessToken", userOneAccessToken);
		response = httpClient.execute(patch);
		Assert.assertEquals("Expected successful PATCH user meta request", 200, response.getStatusLine().getStatusCode());

		Document reloadedUserOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);
		Assert.assertEquals(userOne, reloadedUserOneWorkspace.getAuthor());

		// calling for the updated workspace back again should cause the same
		// updated
		// entity to be returned
		Assert.assertEquals(updatedUserOneWorkspace.getBody().toString(), reloadedUserOneWorkspace.getBody().toString());

		// show that the updated workspace is NOT the same as the user's
		// original workspace
		// 1. the docs should be identical (the tenant's doc should be at this
		// point same as the user's doc)
		Assert.assertEquals("Expected the two users to have the same workspace doc BODY ", userOneWorkspace.getBody().toString(), updatedUserOneWorkspace.getBody().toString());
		// 2. the owning user should be differnet
		Assert.assertNotSame("Expected the two users to NOT have the same USER_ID in the envelope", userOneWorkspace.getAuthor(), updatedUserOneWorkspace.getAuthor());

		// show that user one's workspace is NOT the same as user two's
		// workspace
		// because user one's workspace is now custom and not the same as user
		// two's (which should
		// still be the tenant workspace)
		Document reloadedUserTwoWorkspace = assertWorkspaceFor(userTwoAccessToken, userTwo);

		Assert.assertEquals("Expected user two's workspace to remain the same", userTwoWorkspace.toJSON().toString(), reloadedUserTwoWorkspace.toJSON().toString());
		// 2. the owning user should be different
		Assert.assertNotSame("Expected the two users to NOT have the same USER_ID in the envelope", updatedUserOneWorkspace.getAuthor(), reloadedUserTwoWorkspace.getAuthor());

	}

	public void testPutWorkspace() throws Exception {
		String tenantID = TENANT;


		String userOne = UUID.randomUUID().toString();
		String userOneAccessToken = fetchAccessToken(TENANT, userOne, "test");
		Document userOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);

		// save the user's workspace: POST /workspaces
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		userOneWorkspace.setName(null);
		HttpPost post = post(baseURI, userOneWorkspace.toJSON());
		post.addHeader("accessToken", userOneAccessToken);
		HttpResponse response = httpClient.execute(post);
		Assert.assertEquals("Expected successful workspace POST", 200, response.getStatusLine().getStatusCode());
		// validate returned entity
		Document updatedUserOneWorkspace = new Document((JSONObject) getJSONEntity(response));
		Assert.assertEquals(userOne, updatedUserOneWorkspace.getAuthor());

		// change the user's default workspace: PATCH /users/me/prefs
		JSONObject changeDefaultWorkspace = new JSONObject();
		JSONObject changeDefaultWorkspaceBody = new JSONObject();
		changeDefaultWorkspace.put("body", changeDefaultWorkspaceBody);
		changeDefaultWorkspaceBody.put("default_workspace_name", updatedUserOneWorkspace.getName());
		HttpPatch patch = patch(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + userOne + "/", changeDefaultWorkspace);
		patch.addHeader("accessToken", userOneAccessToken);
		Assert.assertEquals("Expected successful PATCH user meta request", 200, httpClient.execute(patch).getStatusLine().getStatusCode());

		// validate the latest saved workspace
		Document reloadedUserOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);
		Assert.assertEquals(userOne, reloadedUserOneWorkspace.getAuthor());

		String workspaceID = updatedUserOneWorkspace.getID();
		int version = updatedUserOneWorkspace.getVersion();
		String name = updatedUserOneWorkspace.getName();

		// update the user's workspace: PUT /workspaces/{name}
		// change the description and name
		String description = "New description";
		String displayName = "New display name";
		updatedUserOneWorkspace.setDescription(description);
		updatedUserOneWorkspace.setDisplayName(displayName);
		HttpPut put = put(baseURI + updatedUserOneWorkspace.getName(), updatedUserOneWorkspace.toJSON());
		put.addHeader("accessToken", userOneAccessToken);
		response = httpClient.execute(put);
		Assert.assertEquals("Expected successful workspace update", 200, response.getStatusLine().getStatusCode());

		// validate returned entity:
		// doesn't have the same id and version (because new version)
		updatedUserOneWorkspace = new Document((JSONObject) getJSONEntity(response));
		try {
			Assert.assertNotEquals("values " + workspaceID + " == " + updatedUserOneWorkspace.getID(), workspaceID, updatedUserOneWorkspace.getID());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Assert.assertNotEquals(version, updatedUserOneWorkspace.getVersion());
		// has same name, new description and display name
		Assert.assertEquals(name, updatedUserOneWorkspace.getName());
		Assert.assertEquals(description, updatedUserOneWorkspace.getDescription());
		Assert.assertEquals(displayName, updatedUserOneWorkspace.getDisplayName());

		// validate persisted entity:
		// doesn't have the same id and version (because new version)
		reloadedUserOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);
		Assert.assertNotEquals(workspaceID, reloadedUserOneWorkspace.getID());
		Assert.assertNotEquals(version, reloadedUserOneWorkspace.getVersion());
		// has same name, new description and display name
		Assert.assertEquals(name, reloadedUserOneWorkspace.getName());
		Assert.assertEquals(description, reloadedUserOneWorkspace.getDescription());
		Assert.assertEquals(displayName, reloadedUserOneWorkspace.getDisplayName());

	}

	public void newDgWorkspace() throws Exception {


		String username = UUID.randomUUID().toString();
		addUser(username, TENANT);

		DocumentHandler docs = TestDependencies.getDocs().get();
		Document workspace = docs.getDocument(DocumentType.WORKSPACE, "default");

		// create a new version of this document, it shouldn't be allowed
		HttpResponse response = authPost(username, baseURI, workspace.toJSON(), HttpResponse.class);
		Assert.assertEquals("error code wrong for duplicate document", 400, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());

		response = authPut(username, baseURI + workspace.getName(), workspace.toJSON(), HttpResponse.class);
		Assert.assertEquals("error code wrong for aw security on put of document", 400, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());

	}

	public void testFetchWorkspaceHudDocs() throws Exception {

		// just start it for this one test - it's quicker
		startElasticSearch(); // TODO: do we really need to test this here?

		try {

			String tenantID = TENANT;

			populateIndexes(tenantID);

			String userOne = UUID.randomUUID().toString();
			String userOneAccessToken = fetchAccessToken(TENANT, userOne, "test");
			Document userOneWorkspace = assertWorkspaceFor(userOneAccessToken, userOne);

			JSONObject workspaceBody = userOneWorkspace.getBody();
			JSONObject hudJSON = JSONUtils.getLeafValue(workspaceBody, "elements[0].body");
			Assert.assertNotNull(hudJSON);

		} finally {
			stopElasticsearch();
		}

		// TODO: actual queries from Unity deferred to Sprint 3
		/*
		 * CloseableHttpClient httpClient = client(); HttpPost post =
		 * post(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/Unity/query",
		 * queryJSON); post.addHeader("accessToken", userOneAccessToken);
		 * CloseableHttpResponse postQuery = httpClient.execute(post);
		 * Assert.assertEquals("Expected successful unity query", 200,
		 * postQuery.getStatusLine().getStatusCode()); JSONObject jsonEntity =
		 * (JSONObject) getJSONEntity(postQuery);
		 *
		 * JSONArray results = (JSONArray) jsonEntity.get("data");
		 * Assert.assertEquals(2, results.size());
		 *
		 * { JSONObject result = (JSONObject) results.get(0);
		 * Assert.assertEquals(10.0, result.get("c0"));
		 * Assert.assertEquals("chrome.exe", result.get("c1") ); } { JSONObject
		 * result = (JSONObject) results.get(1); Assert.assertEquals(5.0,
		 * result.get("c0")); Assert.assertEquals("firefox.exe",
		 * result.get("c1") ); }
		 */

	}

	private Document assertWorkspaceFor(String userAccessToken, String username) throws Exception {
		// fetch user meta
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet uri = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username);
		uri.addHeader("accessToken", userAccessToken);
		HttpResponse response = httpClient.execute(uri);
		Assert.assertEquals("Expected successful user meta request", 200, response.getStatusLine().getStatusCode());

		// expect a non-null workspace ID in the user meta
		JSONObject userMetaDoc = (JSONObject) getJSONEntity(response);
		Document userMeta = new Document(userMetaDoc);
		Assert.assertNotNull("Expected user meta to be returned", userMeta);

		// expect a workspace ID
		JSONObject userPrefs = userMeta.getBody();
		String workspaceID = (String) userPrefs.get("default_workspace_name");
		Assert.assertNotNull("Expected user meta to contain a default_workspace_name", workspaceID);

		// expect a non-null response from the server when fetching the
		// workspace
		httpClient = HttpClientBuilder.create().build();
		uri = get(baseURI + workspaceID);
		uri.addHeader("accessToken", userAccessToken);

		response = httpClient.execute(uri);
		int statusCode = response.getStatusLine().getStatusCode();
		Assert.assertEquals("Expected successful workspace fetch response", 200, statusCode);

		JSONObject workspaceMeta = (JSONObject) getJSONEntity(response);
		Assert.assertNotNull("Expected to have fetched a workspace successfully by ID", workspaceMeta);

		return new Document(workspaceMeta);
	}
}
