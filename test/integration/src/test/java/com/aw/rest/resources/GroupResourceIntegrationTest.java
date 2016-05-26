/**
 *
 */
package com.aw.rest.resources;

import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.rest.security.Impersonation;
import com.aw.document.*;
import com.aw.util.Statics;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sun.tools.javac.comp.Todo;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.aw.BaseIntegrationTest;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

import static org.junit.Assert.*;

/**
 * @author sjeanbaptiste
 *
 */
public class GroupResourceIntegrationTest extends BaseIntegrationTest {

	private String REMOVABLE_PATTERN = "\"version_date[^,]+,";

	public final String baseURI = com.aw.util.Statics.VERSIONED_REST_PREFIX;
	public final String baseDocumentURI = Statics.VERSIONED_REST_PREFIX + "/documents";
	private String username;

	@Test
	public void test() throws Exception {

		String tenantID = "1";
		provisionTenant(tenantID);
		username = UUID.randomUUID().toString();
		addUser(username, tenantID);

		createDocumentsForTesting(tenantID);

		testGetDocumentTree();
		testPatchDocument();

		//testDeleteDocumentGroup();
	}

	public void testGetDocumentTree() throws Exception {

		String tenantID = "1";

		String userOne = UUID.randomUUID().toString();
		String userOneAccessToken = fetchAccessToken("1", userOne, "test");

		HttpResponse response = get(baseURI + "/workspaces/tree", userOneAccessToken);

		JSONObject documentTree = (JSONObject) getJSONEntity(response);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertNotNull(documentTree);

		String expectedJSONFile =  "./workspace_groups_with_subgroups_result.json";
		String expectedJSON = readFileFromResourcePath(expectedJSONFile).replaceAll(REMOVABLE_PATTERN, "");

	}

	private void createDocumentsForTesting(String tenantID) throws Exception {

		//be the system user to add the docs
		setThreadSecurity("1", username, username);

		String inputFile = "./workspace_groups_with_subgroups.json";
		String nestedDocument = readFileFromResourcePath(inputFile);
		JSONArray docs = new JSONArray(nestedDocument);

		//create documents as a user
		for (int i = 0; i < docs.length(); i++) {
			final Document document = new Document(docs.getJSONObject(i));
			DocumentEnvelope created = TestDependencies.getDocs().get().createDocument(document);
		}

	}

	public void testPatchDocument() throws Exception {

		String tenantID = "1";
		setThreadSecurity("1", username, username);

		//should be at least one document_group
		Document updatedDocument = TestDependencies.getDocs().get().getDocumentsOfType(DocumentType.DOCUMENT_GROUP).get(0);

		// get document 32 then patch it
		String userOne = UUID.randomUUID().toString();
		String userOneAccessToken = fetchAccessToken("1", userOne, "test");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		Set<Tag> tags = new HashSet<>();
		tags.add(Tag.valueOf("Coolio"));
		tags.add(Tag.valueOf("Intense"));
		tags.add(Tag.valueOf("Playground"));
		updatedDocument.setDisplayName("NEWEST, COOLEST DISPLAYNAME");
		updatedDocument.setAuthor("MEAOW");
		updatedDocument.setTags(tags);
		updatedDocument.setBody(new JSONObject());

		HttpPatch patch = patch(baseURI + "/groups/" + updatedDocument.getName(), new JSONObject(updatedDocument.toJSON()));
		patch.addHeader("accessToken", userOneAccessToken);
		HttpResponse response = httpClient.execute(patch);
		JSONObject documentTree = (JSONObject) getJSONEntity(response);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertNotNull(documentTree);
	}

	public void testDeleteDocumentGroup() throws Exception {
		String tenant = "1";
		String NAME = "document_group_1";

		String userOneAccessToken = fetchAccessToken("1", username, "test");

		HttpResponse ret = authDelete(username, baseURI + "/groups/" + NAME, HttpResponse.class);
		assertTrue(baseURI + " delete unsuccessful", HttpStatus.isSuccess(ret.getStatusLine().getStatusCode()));

	}


	public String readFileFromResourcePath(String inputFile) throws IOException {
		URL url = this.getClass().getResource(inputFile);

		File file = new File(url.getPath());
		String nestedDocument = FileUtils.readFileToString(file);
		return nestedDocument;
	}
}
