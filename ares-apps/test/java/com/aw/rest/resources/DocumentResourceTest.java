package com.aw.rest.resources;

import com.aw.common.Tag;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.document.*;
import com.aw.document.exceptions.DocumentNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author jhaightdigitalguardian.com.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Provider<DocumentHandler> documentHandlerProvider;
	@Mock
	private Provider<DocumentMgr> documentMgrProvider;

	@Mock
	private DocumentMgr documentMgr;
	@Mock
	private DocumentHandler documentHandler;

	private DocumentResource documentResource;
	private String tenantId = "666";

	@Before
	public void setup() {
		PlatformSecurityContext context = mock(PlatformSecurityContext.class);
		when(context.getTenantID()).thenReturn("0");
		ThreadLocalStore.set(context);
		when(documentHandlerProvider.get()).thenReturn(documentHandler);
		when(documentMgrProvider.get()).thenReturn(documentMgr);
		documentResource = new DocumentResource(documentHandlerProvider, documentMgrProvider);
	}

	@Test
	public void testGetDocumentListWithoutBody() throws Exception {
		List<DocumentEnvelope> docs = new ArrayList<>();
		docs.add(new DocumentEnvelope());
		when(documentHandler.getEnvelopesOfType(DocumentType.TEST_TYPE)).thenReturn(docs);
		List<? extends DocumentEnvelope> returnDocs = documentResource.get("test_type", false, tenantId);
		assertEquals(docs, returnDocs);
	}

	@Test
	public void testGetDocumentListWithBody() throws Exception {
		List<Document> docs = new ArrayList<>();
		docs.add(new Document());
		when(documentHandler.getDocumentsOfType(DocumentType.TEST_TYPE)).thenReturn(docs);
		List<? extends DocumentEnvelope> returnDocs = documentResource.get("test_type", true, tenantId);
		assertEquals(docs, returnDocs);
	}

	@Test
	public void testGetDocument() throws Exception {
		String documentName = "catch me if you can";
		Document document = new Document();
		document.setName(documentName);
		when(documentHandler.getDocument(DocumentType.TEST_TYPE, documentName)).thenReturn(document);
		Document returnDoc = documentResource.get("test_type", documentName, tenantId);
		assertEquals(document, returnDoc);
	}

	@Test
	public void testGetDocumentNotFound() throws Exception {
		String documentName = "i don't exist!";
		when(documentHandler.getDocument(DocumentType.TEST_TYPE, documentName)).thenThrow(new DocumentNotFoundException(documentName));
		expectNotFoundException();
		documentResource.get("test_type", documentName, tenantId);
	}

	@Test
	public void getTagsTest() throws Exception {
		Collection<Tag> tags = new ArrayList<>();
		tags.add(Tag.valueOf("tag1"));
		tags.add(Tag.valueOf("tag2"));
		tags.add(Tag.valueOf("tag3"));
		when(documentHandler.getTags()).thenReturn(tags);
		Collection<Tag> returnTags = documentResource.getDocumentTypeTags("all");
		Assert.assertSame(tags, returnTags);
	}

	@Test
	public void getAllTagsTest() throws Exception {
		Collection<Tag> tags = new ArrayList<>();
		tags.add(Tag.valueOf("tag1"));
		tags.add(Tag.valueOf("tag2"));
		tags.add(Tag.valueOf("tag3"));
		when(documentHandler.getTagsForDocumentType(DocumentType.SIMPLE_RULE)).thenReturn(tags);
		Collection<Tag> returnTags = documentResource.getDocumentTypeTags("simple_rule");
		Assert.assertSame(tags, returnTags);
	}
	@Test
	public void testGetTagsNullType() throws Exception {
		Collection<Tag> returnTags = documentResource.getDocumentTypeTags(null);
		Assert.assertSame(Collections.EMPTY_LIST, returnTags);
	}

	@Test
	public void testGetTagsEmptyType() throws Exception {
		Collection<Tag> returnTags = documentResource.getDocumentTypeTags("");
		Assert.assertSame(Collections.EMPTY_LIST, returnTags);
	}

	@Test
	public void testGetDocumentTree() throws Exception {
		String name = "make like a tree";
		DocumentTree tree = new DocumentTree(name);
		when(documentHandler.getDocumentTree(DocumentType.TEST_TYPE)).thenReturn(tree);
		DocumentTree returnTree = documentResource.getDocumentTree("test_type", tenantId);
		assertEquals(tree, returnTree);
	}

	@Test
	public void testGetTenants() throws Exception {
		List<Document> docs = new ArrayList<>();
		docs.add(new Document());
		when(documentHandler.getAllTenants()).thenReturn(docs);
	}

	private void expectNotFoundException() {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("HTTP 404 Not Found");
	}
}
