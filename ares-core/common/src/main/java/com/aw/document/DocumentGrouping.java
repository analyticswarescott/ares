package com.aw.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows representing the information for a document and any documents tied to it via its grouping property.
 */
public class DocumentGrouping {

	private final DocumentEnvelope document;
	private final List<DocumentEnvelope> groupDocuments = new ArrayList<>();

	public DocumentGrouping(DocumentEnvelope document) {
		this.document = document;
	}

	@JsonProperty("document")
	public DocumentEnvelope getDocument() {
		return document;
	}

	@JsonProperty("group_documents")
	public List<DocumentEnvelope> getGroupDocuments() {
		return Collections.unmodifiableList(groupDocuments);
	}

	public boolean addGroupDocument(DocumentEnvelope document) {
		return this.groupDocuments.add(document);
	}

}
