package com.aw.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;

import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.document.DocumentType;
import com.aw.document.security.DocumentPermission;
import com.aw.rest.form.DocumentsBulkEditForm;
import com.aw.util.Statics;

/**
 * Test the RESTful document handler
 *
 * @author jlehmann
 *
 */
public class DocumentResourceTest extends BaseIntegrationTest {

	@Test
	public void test() throws Exception {

		provisionTenant("1");
		provisionTenant("2");

		visibility();

		getDocumentsOfType();

		getTagsForDocumentsOfType();

		getAllTenants();

		updateDocument();

		testDocumentGrouping();

		testBulkUpdateDocumentTags();

	}

	public void visibility() throws Exception {

		//create 2 users
		String user1 = UUID.randomUUID().toString();
		addUser(user1, "1");
		String user2 = UUID.randomUUID().toString();
		addUser(user2, "1");

		//get documents of a type
		setThreadSecurity("1", user1, user1);

		//get our document handler
		DocumentHandler docs = TestDependencies.getDocs().get();

		List<Document> docListOriginal = docs.getDocumentsOfType(DocumentType.WORKSPACE);

		//make sure we have one
		assertTrue("need at least one document to test with", docListOriginal.size() > 0);

		//save one just for us
		Document workspace = docListOriginal.get(0);
		workspace.setName(null);
		workspace.setReadPerm(DocumentPermission.AUTHOR); //make it private
		TestDependencies.getDocs().get().createDocument(workspace);

		//now get our list
		List<Document> docListUser1 = docs.getDocumentsOfType(DocumentType.WORKSPACE);

		//make sure we have another one now
		assertEquals("document not created for user1", docListOriginal.size() + 1, docListUser1.size());

		//now get user 2's docs of this type
		setThreadSecurity("1", user2, user2);
		List<Document> docListUser2 = docs.getDocumentsOfType(DocumentType.WORKSPACE);

		//make sure the new doc isn't visible here
		assertNotEquals("document created by user1 should not be visible by user2", docListUser1.size(), docListUser2.size());

	}

	public void getDocumentsOfType() throws Exception {

        setThreadSystemAccess();
		DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, TestDependencies.getPlatform());

		String username = UUID.randomUUID().toString();
        addUser(username, "1");

		//get documents, make sure they have bodies
		List<Document> list = docs.getDocumentsOfType(DocumentType.USER_SETTINGS);
		assertEquals(DocumentType.USER_SETTINGS + " document count wrong from REST", getFileCount(DocumentType.USER_SETTINGS), list.size());
		assertTrue("no body in document", list.get(0).getBody() != null && list.get(0).getBody().length() > 0);

		//get envelopes, make sure they don't have bodies
		List<DocumentEnvelope> envelopeList = docs.getEnvelopesOfType(DocumentType.USER_SETTINGS);
		assertEquals(DocumentType.USER_SETTINGS + " document count wrong from REST", getFileCount(DocumentType.USER_SETTINGS), envelopeList.size());
		assertFalse("body present in envelope", new JSONObject(envelopeList.get(0).toJSON()).has("body"));

		//get a single doc, make sure it's correct
		DocumentEnvelope envelope = envelopeList.get(0);
		Document document = docs.getDocument(envelope.getDocumentType(), envelope.getName());
		assertTrue("document by name was null", document != null);
		assertTrue("document by name body was empty", document.getBody() != null && document.getBody().length() > 0);

		//test impersonation

		//impersonate tenant 1, delete all stream documents from it
		boolean unimpersonate = Impersonation.impersonateTenant("1");
		DocumentHandler doc1 = new DocumentHandlerRest("1", TestDependencies.getPlatform());
		List<Document> list1 = doc1.getDocumentsOfType(DocumentType.USER_SETTINGS);
		TestDependencies.getDocs().get().deleteDocument(DocumentType.USER_SETTINGS, list1.get(0).getName());

		//now request docs for this user we should get only 1
		List<Document> list2 = doc1.getDocumentsOfType(DocumentType.USER_SETTINGS);
		assertEquals("after deleting, tenant 1 stream_global docs list should be empty", list1.size() - 1, list2.size());
		if (unimpersonate) {
			Impersonation.unImpersonate();
		}


	}

	public void getTagsForDocumentsOfType() throws Exception {

		setThreadSystemAccess();
		DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, TestDependencies.getPlatform());

		// No tags in the system yet for the test type
		Collection<Tag> list = docs.getTagsForDocumentType(DocumentType.TEST_TYPE);
		assertTrue(list.isEmpty());

		// Insert a test doc with 10 tags
		final Document testDoc = new Document(DocumentType.TEST_TYPE, "testing", "Testing", "0", "test", new JSONObject());
		for (int i = 0; i < 10; i++) {
			testDoc.addTag(Tag.valueOf("tag" + i));
		}
		TestDependencies.getDocs().get().createDocument(testDoc);

		// Insert a test doc with another 10 tags of a different type
		final Document testDoc2 = new Document(DocumentType.SIMPLE_RULE, "testing", "Testing", "0", "test", new JSONObject());
		for (int i = 0; i < 10; i++) {
			testDoc2.addTag(Tag.valueOf("differentTag" + i));
		}
		TestDependencies.getDocs().get().createDocument(testDoc2);

		// Now make sure we get the right tags
		list = docs.getTagsForDocumentType(DocumentType.TEST_TYPE);
		assertTrue(list.containsAll(testDoc.getTags()));

		list = docs.getTags();
		Collection<Tag> expectedTags = new ArrayList<>();
		expectedTags.addAll(testDoc.getTags());
		expectedTags.addAll(testDoc2.getTags());
		assertTrue(list.containsAll(expectedTags));
	}

	public void getAllTenants() throws Exception {


		DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, TestDependencies.getPlatform());

		Collection<Document> tenants = docs.getAllTenants();
		assertEquals("tenant list should have 3 tenants in it", 3, tenants.size());

	}

	public void updateDocument() throws Exception {

		DocumentHandler docs = new DocumentHandlerRest("1", TestDependencies.getPlatform());

		Document doc = docs.getDocument(DocumentType.WORKSPACE, "default");
		DocumentEnvelope envelope = docs.updateDocument(doc);

		assertEquals("after update, document version should have increased", doc.getVersion() + 1, envelope.getVersion());

		//get the document
		setThreadSystemAccess();
		Impersonation.impersonateTenant("1");
		doc = TestDependencies.getDocs().get().getDocument(doc.getDocumentType(), doc.getName());
		assertEquals("doc version should have changed after update", envelope.getVersion(), doc.getVersion());

	}

	public void testDocumentGrouping() throws Exception {

		final String tenantID = "1";

		final DocumentHandler docs = new DocumentHandlerRest(tenantID, TestDependencies.getPlatform());

		// Create a new test doc with no grouping
		final String testDocName = "test_doc";
		docs.createDocument(new Document(DocumentType.TEST_TYPE, testDocName, "Test Doc", tenantID, "aw", new JSONObject()));
		final Document testDoc = docs.getDocument(DocumentType.TEST_TYPE, testDocName);
		assertEquals("", testDoc.getGrouping());

		// Create a simple doc group document and assign to the test doc
		final DocumentEnvelope groupDoc = docs.createDocument(new Document(DocumentType.DOCUMENT_GROUP, "test_group", "Test Group", tenantID, "aw", new JSONObject()));
		testDoc.setGrouping(groupDoc.getName());
		final DocumentEnvelope updateDocument = docs.updateDocument(testDoc);
		assertEquals(groupDoc.getName(), updateDocument.getGrouping());

		// Now delete the group doc, make sure it is removed from the test doc
		docs.deleteDocument(groupDoc.getDocumentType(), groupDoc.getName());
		assertEquals("", docs.getDocument(DocumentType.TEST_TYPE, testDocName).getGrouping());

	}
	public void testBulkUpdateDocumentTags() throws Exception {

		final String tenantID = "1";
		final String userID = "aw";
		addUser(userID, tenantID);
		setThreadSecurity(tenantID, userID, userID);

		DocumentHandler docs = new DocumentHandlerRest(tenantID, TestDependencies.getPlatform());

		final DocumentsBulkEditForm bulkEditForm = new DocumentsBulkEditForm();

		List<String> docNames = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			final Document document = new Document(DocumentType.TEST_TYPE, "test" + i, "test_bulk" + i, tenantID, userID, new JSONObject());
			document.setReadPerm(DocumentPermission.AUTHOR);
			document.setWritePerm(DocumentPermission.AUTHOR);
			docs.createDocument(document);
			docNames.add(document.getName());
		}
		bulkEditForm.setDocumentNames(docNames);

		// Add a couple of tags to each document
		final List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
		bulkEditForm.setTags(tags);

		// Set read/write to ALL
		bulkEditForm.setReadPermission(DocumentPermission.ALL);
		bulkEditForm.setWritePermission(DocumentPermission.ALL);

		String json = JSONUtils.objectToString(bulkEditForm);
		HttpResponse response = authPatch(userID, Statics.VERSIONED_REST_PREFIX + "/documents/test_type", new JSONObject(json), HttpResponse.class);
		assertEquals(200, response.getStatusLine().getStatusCode());

		// Make sure all the test docs have the three new tags
		List<DocumentEnvelope> testDocs = docs.getEnvelopesOfType(DocumentType.TEST_TYPE);
		for (DocumentEnvelope testDoc : testDocs) {

			if (testDoc.getName().startsWith("test_bulk")) {
				assertEquals(3, testDoc.getTags().size());
				assertTrue(testDoc.getTags().containsAll(tags.stream().map(Tag::valueOf).collect(Collectors.toList())));
				assertEquals(DocumentPermission.ALL, testDoc.getReadPerm());
				assertEquals(DocumentPermission.ALL, testDoc.getWritePerm());
			}

		}

		// Now, remove all the tags we just added
		bulkEditForm.setTagsToRemove(bulkEditForm.getTags());
		bulkEditForm.setTags(Collections.emptyList());
		// Set read/write to back to AUTHOR
		bulkEditForm.setReadPermission(DocumentPermission.AUTHOR);
		bulkEditForm.setWritePermission(DocumentPermission.AUTHOR);

		json = JSONUtils.objectToString(bulkEditForm);
		response = authPatch(userID, Statics.VERSIONED_REST_PREFIX + "/documents/test_type", new JSONObject(json), HttpResponse.class);
		assertEquals(200, response.getStatusLine().getStatusCode());

		// Make sure we have no tags tied to the docs
		testDocs = docs.getEnvelopesOfType(DocumentType.TEST_TYPE);
		for (DocumentEnvelope testDoc : testDocs) {
			if (testDoc.getName().startsWith("test_bulk")) {
				assertTrue(testDoc.getTags().isEmpty());
				assertEquals(DocumentPermission.AUTHOR, testDoc.getReadPerm());
				assertEquals(DocumentPermission.AUTHOR, testDoc.getWritePerm());
			}
		}

	}

	private int getFileCount(DocumentType type) {

		File[] list = new File("conf/defaults/" + type.name().toLowerCase()).listFiles();
		return list == null ? 0 : list.length;
	}

}
