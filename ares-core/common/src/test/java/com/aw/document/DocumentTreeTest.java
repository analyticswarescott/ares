package com.aw.document;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class DocumentTreeTest {

	@Rule
    public ExpectedException thrown= ExpectedException.none();

	private String REMOVABLE_PATTERN = "\"version_date[^,]+,";

	@Test
	public void itShouldBePossibleToCreateADocumentTreeWithNestedGroups() throws Exception {

		String inputFile = "com/aw/document/document_groups.json";
		DocumentTree tree = buildDocumentTreeFromFile(inputFile);
		assertTrue(tree.getDocuments().size() == 28);
		assertTrue(tree.getGroups().size() == 7);
	}

	@Test
	public void itShouldBePossibleToCreateATreeWithNoGroups() throws Exception {

		String inputFile = "com/aw/document/workspace_document_with_no_grouping.json";
		DocumentTree tree = buildDocumentTreeFromFile(inputFile);

		assertTrue(tree.getGroups().size() == 0);
	}

	@Test
	public void itShouldNotBePossibleToCreateADocumentTreeWithCycles() throws Exception {
		thrown.expect(RuntimeException.class);
	    thrown.expectMessage("Document tree has cyclic relationship");
		String inputFile = "com/aw/document/workspace_document_with_cycles.json";
		buildDocumentTreeFromFile(inputFile);
	}

	@Test
	public void itShouldNotBePossibleToCreateADocumentTreeWithNameAndGroupingAreSame() throws Exception {
		thrown.expect(RuntimeException.class);
	    thrown.expectMessage("Document tree has cyclic relationship");
		String inputFile = "com/aw/document/workspace_same_name_and_grouping.json";
		DocumentTree tree = buildDocumentTreeFromFile(inputFile);
		assertTrue(tree == null);
	}

	public static String readFileFromResourcePath(String inputFile) throws IOException {

		URL url = Resources.getResource(inputFile);
		String nestedDocument = Resources.toString(url, Charsets.UTF_8);
		return nestedDocument;
	}

	public static DocumentTree buildDocumentTreeFromFile(String inputFile) throws IOException, JSONException, Exception {
		String nestedDocument = readFileFromResourcePath(inputFile);

		JSONArray docs = new JSONArray(nestedDocument);

		List<DocumentEnvelope> fromEnvelopes = new ArrayList<>();

		for (int i = 0; i < docs.length(); i++) {
			final DocumentEnvelope documentEnvelope = new DocumentEnvelope(docs.getJSONObject(i));
			fromEnvelopes.add(documentEnvelope);
		}

		DocumentTree tree = new DocumentTree(DocumentType.WORKSPACE, fromEnvelopes);
		tree.buildTree();
		return tree;
	}

}
