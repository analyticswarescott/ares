package com.aw.rest.resources;

import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.exceptions.DocumentNotFoundException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
/**
 * @author jhaight
 */

@RunWith(MockitoJUnitRunner.class)
public abstract class DocumentResourceTestBase {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	protected Provider<DocumentHandler> documentHandlerProvider;
	@Mock
	protected DocumentHandler documentHandler;

	@Before
	public void setup() {
		when(documentHandlerProvider.get()).thenReturn(documentHandler);
		initializeDocumentResource();
	}

	@Test
	public void getAllTest() throws Exception {
		List<DocumentEnvelope> fakeDocumentEnvelopes = new ArrayList<DocumentEnvelope>();
		fakeDocumentEnvelopes.add(new DocumentEnvelope());
		when(documentHandler.getEnvelopesOfType(getDocumentResource().getDocumentType())).thenReturn(fakeDocumentEnvelopes);
		List<DocumentEnvelope> returnList = getDocumentResource().get();
		Assert.assertSame(fakeDocumentEnvelopes, returnList);
	}

	@Test
	public void getOneTest() throws Exception {
		String documentName = "WHERE ARE THEY!";
		Document document = new Document();
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), documentName)).thenReturn(document);
		Document returnDocument = getDocumentResource().get(documentName);
		Assert.assertSame(document, returnDocument);
	}

	@Test
	public void getOneDocumentNotFoundTest() throws Exception {
		String documentName = "Gotham, the city is yours!";
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), documentName)).thenThrow(DocumentNotFoundException.class);
		setupNotFoundExpection();
		getDocumentResource().get(documentName);
	}

	@Test
	public void postDocumentTest() throws Exception {
		String documentJson = createValidDocument().toJSON();
		DocumentEnvelope envelope = new DocumentEnvelope();
		when(documentHandler.createDocument(Matchers.eq(new Document(documentJson)))).thenReturn(envelope);
		DocumentEnvelope returnedEnvelope = getDocumentResource().add(documentJson);
		Assert.assertSame(envelope, returnedEnvelope);
	}

	@Test
	public void postNullDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().add(null);
	}

	@Test
	public void postEmptyDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().add("");
	}

	@Test
	public void postBadJsonDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().add("{I'm not wearing hockey pads.}");
	}

	@Test
	public void postBadDocumentTypeTest() throws Exception {
		Document badDocument = createValidDocument();
		setupBadRequestExpectation();
		JSONObject documentJson = new JSONObject(badDocument.toJSON());
		documentJson.put("type", "SWEAR TO ME!");
		getDocumentResource().add(documentJson.toString());
	}

	@Test
	public void postIncorrectDocumentTypeTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setType(getDocumentResource().getDocumentType() == DocumentType.TEST_TYPE ? DocumentType.SIMPLE_RULE : DocumentType.TEST_TYPE);
		setupBadRequestExpectation();
		getDocumentResource().add(badDocument.toJSON());
	}

	@Test
	public void postMissingDocumentTypeTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setType(null);
		setupBadRequestExpectation();
		getDocumentResource().add(badDocument.toJSON());
	}

	@Test
	public void postNullBodyTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setBody(null);
		setupBadRequestExpectation();
		getDocumentResource().add(badDocument.toJSON());
	}

	@Test
	public void putDocumentTest() throws Exception {
		String name = "Wayne Manor";
		Document document = createValidDocument();
		String documentJson = document.toJSON();
		DocumentEnvelope envelope = new DocumentEnvelope();
		document.setName(name);
		when(documentHandler.updateDocument(eq(document))).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().update(name, documentJson);
		Assert.assertSame(envelope, returnEnvelope);
	}

	@Test
	public void putNotFoundDocumentTest() throws Exception {
		setupNotFoundExpection();
		when(documentHandler.updateDocument(any(Document.class))).thenThrow(DocumentNotFoundException.class);
		getDocumentResource().update("And here we go", createValidDocument().toJSON());
	}

	@Test
	public void putNullDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().update("Want to know why I have these scars?", null);
	}

	@Test
	public void putEmptyDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().update("I'm not going to kill you", "");
	}

	@Test
	public void putBadJsonDocumentTest() throws Exception {
		setupBadRequestExpectation();
		getDocumentResource().update("but that doesn't mean I have to save you", "{}");
	}

	@Test
	public void putBadDocumentTypeTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setType(getDocumentResource().getDocumentType() == DocumentType.TEST_TYPE ? DocumentType.SIMPLE_RULE : DocumentType.TEST_TYPE);
		setupBadRequestExpectation();
		getDocumentResource().update("I'm Batman", badDocument.toJSON());
	}

	@Test
	public void putMissingDocumentTypeTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setType(null);
		setupBadRequestExpectation();
		getDocumentResource().update("I'm Batman", badDocument.toJSON());
	}

	@Test
	public void putNullBodyTest() throws Exception {
		Document badDocument = createValidDocument();
		badDocument.setBody(null);
		setupBadRequestExpectation();
		getDocumentResource().update("Some other batman quote", badDocument.toJSON());
	}

	@Test
	public void deleteDocumentTest() throws Exception {
		String name = "I'm a document";
		DocumentEnvelope envelope = new DocumentEnvelope();
		when(documentHandler.deleteDocument(getDocumentResource().getDocumentType(), name)).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().delete(name);
		Assert.assertSame(envelope, returnEnvelope);
	}

	@Test
	public void deleteNotFoundDocumentTest() throws Exception {
		setupNotFoundExpection();
		String name = "I'm not a document";
		when(documentHandler.deleteDocument(getDocumentResource().getDocumentType(), name)).thenThrow(DocumentNotFoundException.class);
		getDocumentResource().delete(name);
	}

	@Test
	public void patchDocumentTest() throws Exception {
		String patchData = "\"Raz Al Ghul\"";
		String name = "League of Assasins";
		Document document = createValidDocument(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
		DocumentEnvelope envelope = new DocumentEnvelope();
		when(documentHandler.updateDocument(docCaptor.capture())).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().patch(name, "display_name", true, patchData);
		Assert.assertSame(envelope, returnEnvelope);
		assertEquals("Raz Al Ghul", docCaptor.getValue().getDisplayName());
	}

	@Test
	public void patchOverlayDocumentTest() throws Exception {
		String patchData = "{\"display_name\":\"Raz Al Ghul\", \"grouping\":\"Harley\"}";
		String name = "League of Assasins";
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
		Document document = createValidDocument(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		DocumentEnvelope envelope = new DocumentEnvelope();
		when(documentHandler.updateDocument(docCaptor.capture())).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().patch(name, false, patchData);
		Assert.assertSame(envelope, returnEnvelope);
		assertEquals("Raz Al Ghul", docCaptor.getValue().getDisplayName());
		assertEquals("Harley", docCaptor.getValue().getGrouping());
	}

	@Test
	public void patchNotFoundDocumentTest() throws Exception {
		setupNotFoundExpection();
		String name = "I'm not a document";
		String patchData = "\"Raz Al Ghul\"";
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenThrow(DocumentNotFoundException.class);
		getDocumentResource().patch(name, "author", true, patchData);
	}

	@Test
	public void patchOverlayNotFoundDocumentTest() throws Exception {
		setupNotFoundExpection();
		String name = "I'm not a document";
		String patchData = "\"Raz Al Ghul\"";
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenThrow(DocumentNotFoundException.class);
		getDocumentResource().patch(name, false, patchData);
	}

	@Test
	public void patchFieldNotFoundDocumentTest() throws Exception {
		String name = "I am a document";
		String patchData = "\"value\"";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, "invalid_field", true, patchData);
	}

	@Test
	public void patchNullPath() throws Exception {
		setupBadRequestExpectation();
		String name = "I am a document";
		String patchData = "\"it's a thing\"";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, null, true, patchData);
	}

	@Test
	public void patchFieldInvalidJson() throws Exception {
		setupBadRequestExpectation();
		String name = "I am a jelly filled donut";
		String patchData = "{\"}";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, "display_name", true, patchData);
	}

	@Test
	public void patchFieldEmptyJsonObject() throws Exception {
		setupBadRequestExpectation();
		String name = "Ich van ein Berliner";
		String patchData = "{}";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, "", true, patchData);
	}

	@Test
	public void patchEnvelopeFieldEmptyJsonObject() throws Exception {
		setupBadRequestExpectation();
		String name = "Ich van ein Berliner";
		String patchData = "{}";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, "display_name", true, patchData);
	}

	@Test
	public void patchFieldArray() throws Exception {
		String name = "Ich van ein Berliner";
		String patchData = "[\"it's an array\", \"of stuff!\"]";
		Document document = createValidDocument();
		document.setName(name);
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		getDocumentResource().patch(name, "body/array", true, patchData);
	}


	@Test
	public void deepPatchDocumentTestWithEndingSlash() throws Exception {
		String name = "League of Assasins";
		Document document = createValidDocument(name);
		document.setBody(new JSONObject("{\"body_prop\":{\"some_property\":\"some value\"}}"));
		testDeepPatchExpectNoErrors("body/body_prop/", document, name);
	}

	@Test
	public void deepPatchOverlayDocumentTestWithEndingSlash() throws Exception {
		String name = "League of Assasins";
		Document document = createValidDocument(name);
		document.setBody(new JSONObject("{\"body_prop\":{\"some_property\":\"some value\", \"prop2\":\"value2\"}}"));
		testDeepPatchOverlayExpectNoErrors("body/body_prop/", document, name);
	}

	@Test
	public void deepPatchDocumentTestWithoutEndingSlash() throws Exception {
		String name = "Justice League";
		Document document = createValidDocument(name);
		document.setBody(new JSONObject("{\"body_prop\":{\"some_property\":\"some value\"}}"));
		testDeepPatchExpectNoErrors("body/body_prop", document, name);
	}

	@Test
	public void deepPatchDocumentTestWithoutExistingPath() throws Exception {
		String name = "Super Friends";
		Document document = createValidDocument(name);
		testDeepPatchExpectNoErrors("body/body_prop/", document, name);
	}


	private void setupBadRequestExpectation() {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("HTTP 400 Bad Request");
	}

	private void setupNotFoundExpection() {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("HTTP 404 Not Found");
	}

	private Document createValidDocument() {
		Document document = new Document();
		document.setType(getDocumentResource().getDocumentType());
		document.setBody(new JSONObject());
		return document;
	}

	private Document createValidDocument(String name) {
		Document document = createValidDocument();
		document.setName(name);
		return document;
	}

	private void testDeepPatchExpectNoErrors(String structure, Document document, String name) throws Exception {
		String patchData = "{\"some_property\":\"another value\"}";
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		Document expectedDocument = createValidDocument(name);
		expectedDocument.setBody(new JSONObject("{\"body_prop\":{\"some_property\":\"another value\"}}"));
		DocumentEnvelope envelope = new DocumentEnvelope();
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
		when(documentHandler.updateDocument(docCaptor.capture())).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().patch(name, structure, true, patchData);
		assertEquals(expectedDocument, docCaptor.getValue());
		assertEquals(expectedDocument.getBody().toString(), docCaptor.getValue().getBody().toString());
		Assert.assertSame(envelope, returnEnvelope);
	}

	private void testDeepPatchOverlayExpectNoErrors(String structure, Document document, String name) throws Exception {
		String patchData = "{\"some_property\":\"another value\"}";
		when(documentHandler.getDocument(getDocumentResource().getDocumentType(), name)).thenReturn(document);
		Document expectedDocument = createValidDocument(name);
		expectedDocument.setBody(new JSONObject("{\"body_prop\":{\"some_property\":\"another value\", \"prop2\":\"value2\"}}"));
		DocumentEnvelope envelope = new DocumentEnvelope();
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
		when(documentHandler.updateDocument(docCaptor.capture())).thenReturn(envelope);
		DocumentEnvelope returnEnvelope = getDocumentResource().patch(name, structure, false, patchData);
		assertEquals(expectedDocument, docCaptor.getValue());
		assertEquals(expectedDocument.getBody().toString(), docCaptor.getValue().getBody().toString());
		Assert.assertSame(envelope, returnEnvelope);
	}

	protected abstract void initializeDocumentResource();
	protected abstract DocumentResourceBase getDocumentResource();
}