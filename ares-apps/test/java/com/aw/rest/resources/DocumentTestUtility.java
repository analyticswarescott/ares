package com.aw.rest.resources;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class DocumentTestUtility {

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
