package com.aw.documents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.util.AWFileUtils;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentListener;
import com.aw.document.DocumentType;
import com.aw.document.exceptions.DocumentIDNotFoundException;
import com.aw.document.exceptions.DocumentNameNotFoundException;
import com.aw.document.jdbc.SequencedDocumentJDBCProvider;


public class TestDocs extends BaseIntegrationTest {

	private String testDataDir;
	private TestDocumentListener docListener;

	@Before
	public void setUp() throws Exception {
		testDataDir = this.getClass().getClassLoader().getResource("com.aw/documents").getFile();
	}

	private Document getTestDoc(String docName) throws Exception {
		File f = new File(testDataDir + File.separator + docName);
		JSONObject o = AWFileUtils.getFileToJSONObject(f);
		if (o == null) {
			throw new RuntimeException("Could not find " + f.getAbsolutePath());
		}
		return new Document(o.toString());
	}

	@Test
	public void integrationTest() throws Exception {

		provisionTenant("1");

		//testConsistency();

		//keep track of changes
		docListener = new TestDocumentListener();

		//listener to document updates, should be for all doc handlers
		TestDependencies.getDocMgr().get().getSysDocHandler().addListener(docListener);

		testGetTagsByDocType();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertEquals(3, docListener.opCountByTenant.get("0").get());

		testDocRetrievalByTags();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertEquals(4, docListener.opCountByTenant.get("0").get());

		testDefaultDocRetrieval();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertTrue(docListener.opCountByTenant.containsKey("1"));
		assertEquals(4, docListener.opCountByTenant.get("0").get());
		assertEquals(1, docListener.opCountByTenant.get("1").get());

		testDocLifecycle();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertTrue(docListener.opCountByTenant.containsKey("1"));
		assertEquals(4, docListener.opCountByTenant.get("0").get());
		assertEquals(4, docListener.opCountByTenant.get("1").get());

		testDocTagging();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertTrue(docListener.opCountByTenant.containsKey("1"));
		assertEquals(4, docListener.opCountByTenant.get("0").get());
		assertEquals(7, docListener.opCountByTenant.get("1").get());

		testDocGrouping();

		//check document updates, should be getting events for all tenants
		assertTrue(docListener.opCountByTenant.containsKey("0"));
		assertTrue(docListener.opCountByTenant.containsKey("1"));
		assertEquals(4, docListener.opCountByTenant.get("0").get());
		assertEquals(11, docListener.opCountByTenant.get("1").get());

		testDocRetrievalByTags();

		testDocIsCurrent();

	}

	public void testDocIsCurrent() throws Exception {

		Impersonation.impersonateTenant("1");

		String username = UUID.randomUUID().toString();
        addUser(username, "1");

        //get datatypes
        for (int x=0; x<100; x++) {
            authPatch(username, "/rest/1.0/user_settings/" + username + "/body", new JSONObject("{ \"test_key\" : \"test_value\" }"), JSONObject.class);
        }

        assertEquals(101, TestDependencies.getDocs().get().getDocument(DocumentType.USER_SETTINGS, username).getVersion());

	}

	public void testDefaultDocRetrieval() throws Exception {

		//get default document for user
		String username = UUID.randomUUID().toString();
		String userAccessToken = fetchAccessToken("1", username, "test");

		// get user prefs
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username);
		get.addHeader("accessToken", userAccessToken);
		HttpResponse response = httpClient.execute(get);
		assertEquals("Expected successful GET user meta request", 200, response.getStatusLine().getStatusCode());

		JSONObject docJSON = (JSONObject) getJSONEntity(response);
		Document defaultNamed = new Document(docJSON);
		assertEquals("Expected doc name to be user ID", username, defaultNamed.getName());
		assertEquals("Expected doc author to be user ID", username, defaultNamed.getAuthor());
		assertEquals("Expected doc version to be 1 ", 1, defaultNamed.getVersion());

	}

	public void testDocLifecycle() throws Exception {

		//reset file writer/reader for this test
		TestDependencies.getPlatformMgr().get().setTenantFileWriter(null);
		TestDependencies.getPlatformMgr().get().setTenantFileReader(null);

		Impersonation.impersonateTenant("1");
		DocumentHandler docs = TestDependencies.getDocs().get();

		//SEMANTIC inserts
		//insert doc with name
		Document doc = getTestDoc("test_doc.json");
		DocumentEnvelope ret = docs.createDocument(doc);
		assertEquals("Expected version to be 1 ", 1, ret.getVersion());
		assertEquals("Expected semantic name to be the same when name supplied ", doc.getName(), ret.getName());

		//insert doc with NO name
		Document doc2 = getTestDoc("test_doc_noname.json");

		DocumentEnvelope ret2 = docs.createDocument(doc2);
		assertEquals("Expected version to be 1 ", 1, ret.getVersion());
		assertEquals("Expected name to be formulated correctly ", doc2.getDocumentType().toString() + SequencedDocumentJDBCProvider.DOCUMENT_NAME_DELIMITER + doc2.getAuthor() + SequencedDocumentJDBCProvider.DOCUMENT_NAME_DELIMITER + "2", ret2.getName());

		//fire the second doc back in as an update
		Document input = docs.getDocument(ret2.getID());
		DocumentEnvelope ret3 = docs.updateDocument(input);
		assertEquals("Expected version to be 2 ", 2, ret3.getVersion());
		assertEquals("Expected version to be current ", true, ret3.getIsCurrent());

		//get latest version via type
		Document semanticGet = docs.getDocument(ret3.getDocumentType(), ret3.getName());
		assertEquals("Expected version to be 2 ", 2, semanticGet.getVersion());
		assertEquals("Expected version to be current ", true, semanticGet.getIsCurrent());

		// ensure GetDocumentsOfType returns latest versions
		List<Document> list = docs.getDocumentsOfType(DocumentType.TEST_TYPE);
		assertEquals("expect 2 docs", 2, list.size());
		for (Document d : list) {
			assertEquals(d.getIsCurrent(), true);
		}

	}

	public void testDocTagging() throws Exception {

    	//reset file writer/reader for this test
    	TestDependencies.getPlatformMgr().get().setTenantFileWriter(null);
    	TestDependencies.getPlatformMgr().get().setTenantFileReader(null);

		final int numTags = 10;

		final Document doc = new Document(DocumentType.TEST_TYPE, "test123", "testing", "0", "test", new JSONObject());
		for (int i = 0; i < numTags; i++) {
			doc.addTag(Tag.valueOf("testing" + i));
		}
		final DocumentHandler docHandler = TestDependencies.getDocs().get();
		docHandler.createDocument(doc);

		// Make sure that we have the appropriate number of tags from the retrieve operations
		verifyTagCount(docHandler, doc, numTags);

		// Now update one of the tags and make sure that we get the right number of doc tags
		doc.removeTag(Tag.valueOf("testing9"));    // remove the last tag
		assertEquals(numTags - 1, doc.getTags().size());
		docHandler.updateDocument(doc);

		// Make sure that we have the appropriate number of tags from the retrieve operations
		verifyTagCount(docHandler, doc, numTags - 1);

		List<Document> docsBeforeDelete = docHandler.getDocumentsOfType(doc.getDocumentType());

		// Finally, remove the doc altogether
		docHandler.deleteDocument(doc.getDocumentType(), doc.getName());

		// Make sure the docs are gone
		try {
			docHandler.getDocument(doc.getDocumentType(), doc.getName());
			fail("Should not have been able to find the document");
		} catch (DocumentNameNotFoundException nfe) {
			// expected
		}
		try {
			Document old = docHandler.getDocument(doc.getID());
			assertEquals("this doc should not be current", false, old.getIsCurrent());
		} catch (DocumentIDNotFoundException e) {
			// expected
		}
		assertEquals(docsBeforeDelete.size() - 1, docHandler.getDocumentsOfType(doc.getDocumentType()).size());

	}

	public void testDocGrouping() throws Exception {

    	//reset file writer/reader for this test
    	TestDependencies.getPlatformMgr().get().setTenantFileWriter(null);
    	TestDependencies.getPlatformMgr().get().setTenantFileReader(null);

		final DocumentHandler docHandler = TestDependencies.getDocs().get();

		// Create a new document group
		final Document docGroup = new Document(DocumentType.DOCUMENT_GROUP, "test_group", "Test Group", "0", "test", new JSONObject());
		docHandler.createDocument(docGroup);

		// Create a document with no grouping at all
		final Document doc = new Document(DocumentType.TEST_TYPE, "test", "testing", "0", "test", new JSONObject());
		docHandler.createDocument(doc);
		assertNull(doc.getGrouping());

		doc.setGrouping(docGroup.getName());
		docHandler.updateDocument(doc);

		// Make sure we've persisted the doc grouping and can retrieve it correctly
		assertTrue("Doc grouping should not be blank!", StringUtils.isNotBlank(docHandler.getDocument(doc.getDocumentType(), doc.getName()).getGrouping()));

		// Remove the doc grouping, make sure that we aren't referencing the grouping on our original doc
		docHandler.deleteDocument(docGroup.getDocumentType(), docGroup.getName());
		Document orig = docHandler.getDocument(doc.getDocumentType(), doc.getName());

		//TODO: need to decide best way to manage references when deleting a "grouping" document -- an action is likely best
		assertEquals("", orig.getGrouping());

	}

	public void testDocRetrievalByTags() throws Exception {

		//reset file writer/reader for this test
		TestDependencies.getPlatformMgr().get().setTenantFileWriter(null);
		TestDependencies.getPlatformMgr().get().setTenantFileReader(null);

		final int numTags = 10;

		final Document doc = new Document(DocumentType.TEST_TYPE, "testByTags", "testing", "0", "test", new JSONObject());
		for (int i = 0; i < numTags; i++) {
			doc.addTag(Tag.valueOf("testing" + i));
		}
		final DocumentHandler docHandler = TestDependencies.getDocs().get();
		docHandler.createDocument(doc);

		// Make sure we don't get any docs, as none have a testing10 tag
		assertTrue(docHandler.getEnvelopesOfTypeWithTags(doc.getDocumentType(), Collections.singletonList(Tag.valueOf("testing10"))).isEmpty());
		assertTrue(docHandler.getDocumentsOfTypeWithTags(doc.getDocumentType(), Collections.singletonList(Tag.valueOf("testing10"))).isEmpty());

		// Make sure we get the doc that matches all of the different tags
		List<Tag> tagList = new ArrayList<>();
		for (int i = 0; i < numTags; i++) {
			tagList.add(Tag.valueOf("testing" + i));
			final List<DocumentEnvelope> envelopesOfTypeAndTag = docHandler.getEnvelopesOfTypeWithTags(doc.getDocumentType(), tagList);
			assertEquals(1, envelopesOfTypeAndTag.size());
			verifyTags(envelopesOfTypeAndTag.get(0), numTags);
			final List<Document> docsOfTypeAndTag = docHandler.getDocumentsOfTypeWithTags(doc.getDocumentType(), tagList);
			assertEquals(1, docsOfTypeAndTag.size());
			verifyTags(docsOfTypeAndTag.get(0), numTags);
		}

	}

	public void testGetTagsByDocType() throws Exception {

		//reset file writer/reader for this test
		TestDependencies.getPlatformMgr().get().setTenantFileWriter(null);
		TestDependencies.getPlatformMgr().get().setTenantFileReader(null);

		final DocumentHandler docHandler = TestDependencies.getDocs().get();

		// shouldn't have any tags yet
		assertTrue(docHandler.getTagsForDocumentType(DocumentType.TEST_TYPE).isEmpty());

		final Document doc = new Document(DocumentType.TEST_TYPE, "test", "testing", "0", "test", new JSONObject());
		docHandler.createDocument(doc);

		// still no tags
		assertTrue(docHandler.getTagsForDocumentType(DocumentType.TEST_TYPE).isEmpty());

		// Make sure we get the single doc tag back
		doc.addTag(Tag.valueOf("test1"));
		docHandler.updateDocument(doc);

		Collection<Tag> tagsForDocumentType = docHandler.getTagsForDocumentType(DocumentType.TEST_TYPE);
		assertEquals(1, tagsForDocumentType.size());
		assertTrue(tagsForDocumentType.containsAll(Collections.singletonList(Tag.valueOf("test1"))));

		// Make sure we get the two doc tags back
		doc.addTag(Tag.valueOf("test2"));
		docHandler.updateDocument(doc);

		tagsForDocumentType = docHandler.getTagsForDocumentType(DocumentType.TEST_TYPE);
		assertEquals(2, tagsForDocumentType.size());
		assertTrue(tagsForDocumentType.containsAll(Arrays.asList(Tag.valueOf("test1"), Tag.valueOf("test2"))));

	}


	//TODO: can we simulate multiple REST nodes in an integration test?
/*	public void testConsistency() throws Exception {
		//spin up a new REST server
		ReportingServiceWrapper guy2 = ReportingServiceWrapper.create(8989);
		guy2.start();


		//write to it

		DefaultPlatformNode node2 = new DefaultPlatformNode();
		node2.setHost("localhost");
		node2.addSetting(NodeRole.REST, Rest.PORT, 8989);

		DocumentHandlerRest rest2 = new DocumentHandlerRest("0", node2);
		Document test = getDefaultPlatformDocAsNewName(Platform.LOCAL);
		rest2.updateDocument(test);


	}*/

	private void verifyTagCount(DocumentHandler docHandler, Document doc, int numTags) throws Exception {

		// Get by doc type
		final Document docByType = docHandler.getDocument(doc.getDocumentType(), doc.getName());
		verifyTags(docByType, numTags);

		// Get by ID
		final Document docById = docHandler.getDocument(doc.getID());
		verifyTags(docById, numTags);

		// Get docs by type - should get back all of the docs that were created to this point
		final List<Document> documentsOfType = docHandler.getDocumentsOfType(DocumentType.TEST_TYPE);
		boolean found = false;
		for (Document curDoc : documentsOfType) {
			if (StringUtils.equals(doc.getName(), curDoc.getName())) {
				found = true;
				verifyTags(curDoc, numTags);
			}
		}
		assertTrue(found);

		// Get envelopes by type
		final List<DocumentEnvelope> envelopesOfType = docHandler.getEnvelopesOfType(DocumentType.TEST_TYPE);
		found = false;
		for (DocumentEnvelope curDoc : envelopesOfType) {
			if (StringUtils.equals(doc.getName(), curDoc.getName())) {
				found = true;
				verifyTags(curDoc, numTags);
			}
		}
		assertTrue(found);

	}

	private void verifyTags(DocumentEnvelope doc, int numTags) {
		assertEquals(numTags, doc.getTags().size());

		for (int i = 0; i < numTags; i++) {
			assertTrue(doc.getTags().contains(Tag.valueOf("testing" + i)));
		}

	}

	private class TestDocumentListener implements DocumentListener {

		Map<String, AtomicInteger> opCountByTenant = new HashMap<String, AtomicInteger>();

		@Override
		public void onDocumentCreated(Document document) throws Exception {
			increment(document.getTenantID());
		}

		@Override
		public void onDocumentDeleted(Document document) throws Exception {
			increment(document.getTenantID());
		}

		@Override
		public void onDocumentUpdated(Document document) throws Exception {
			increment(document.getTenantID());
		}

		private void increment(String id) {

			AtomicInteger counter = opCountByTenant.get(id);
			if (counter == null) {
				opCountByTenant.put(id, counter = new AtomicInteger());
			}

			counter.incrementAndGet();

		}
	}
}
