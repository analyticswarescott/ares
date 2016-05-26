package com.aw.document;

import java.util.*;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONException;

import com.aw.common.util.JSONUtils;
import com.aw.common.util.JSONable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * Purpose: Create a logical grouping of workspaces based on those that belong
 * to a group and those that do not
 *
 * @author sjeanbaptiste
 */
public class DocumentTree implements JSONable {

	private List<DocumentEnvelope> documentGroups;
	@JsonIgnore
	private List<DocumentEnvelope> documentEnvelopes;

	@JsonIgnore
	private Set<String> parents;

	@JsonIgnore
	private List<DocumentEnvelope> allDocumentEnvelopes = null;

	@JsonProperty
	private String name;

	@JsonProperty
	private DocumentEnvelope document = null;

	@JsonIgnore
	private DocumentType documentType;

	public DocumentEnvelope getDocument() {
		return document;
	}

	public void setDocument(DocumentEnvelope document) {
		this.document = document;
	}

	public String getName() {
		return name;
	}

	public void setName(String treeName) {
		this.name = treeName;
	}

	@JsonProperty
	private List<DocumentEnvelope> documents = new ArrayList<>();

	public List<DocumentEnvelope> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DocumentEnvelope> documents) {
		this.documents = documents;
	}

	@JsonProperty
	private List<DocumentTree> groups = new ArrayList<>();

	public List<DocumentTree> getGroups() {
		return groups;
	}

	public void setGroups(List<DocumentTree> groups) {
		this.groups = groups;
	}

	public DocumentTree(DocumentType documentType) {
		this.documentType = documentType;
		this.name = documentType.name().toLowerCase() + "_tree";
	}

	public DocumentTree(DocumentType documentType, List<DocumentEnvelope> fromEnvelopes) {
		this(documentType);
		this.allDocumentEnvelopes = fromEnvelopes;
	}

	public DocumentTree(DocumentType documentType, List<DocumentEnvelope> documentEnvelopes, List<DocumentEnvelope> documentGroups) {
		this(documentType);
		this.documentEnvelopes = documentEnvelopes;
		this.documentGroups = documentGroups;
	}

	public DocumentTree(String name) {
		this.name = name;
	}

	public void buildTree() throws JSONException, Exception {
		buildTreeStructure();
	}

	private void buildTreeStructure() {

		populateTopNodeWithChildlessNodes();

		Map<String, Set<DocumentEnvelope>> groupingDocumentsMap = allDocumentEnvelopes.stream().collect(
			Collectors.groupingBy(de -> de.getGrouping(), Collectors.mapping(de -> de, Collectors.toSet())));
		groupingDocumentsMap.remove("");

		Set<DocumentEnvelope> children = Sets.newHashSet(Iterables.concat(groupingDocumentsMap.values()));

		// validate that there are no cyclic relationships
		if (hasCyclicRelationships(children)) {
			throw new RuntimeException("Document tree has cyclic relationship");
		}

		// parents are all documents with the document_group document type, where its name is the grouping of another
		// document
		Set<DocumentEnvelope> parentEnvelopes = allDocumentEnvelopes.stream()
			.filter(de ->
				groupingDocumentsMap.keySet().contains(de.getName())
					&& de.getDocumentType() == DocumentType.DOCUMENT_GROUP
			).collect(Collectors.toSet());

		// top level parents
		parentEnvelopes.removeAll(children);

		// populate tree list
		this.setGroups(buildTreeNodes(parentEnvelopes, groupingDocumentsMap));
	}

	private void populateTopNodeWithChildlessNodes() {
		// create first level leaf nodes
		List<DocumentEnvelope> documentsWithNoGrouping = allDocumentEnvelopes.stream()
			.filter(de -> de.getDocumentType() != DocumentType.DOCUMENT_GROUP
				&& (de.getDocumentType() == this.documentType
				&& (de.getGrouping() == null || de.getGrouping().trim().equals(""))))
			.collect(Collectors.toList());
		this.setDocuments(documentsWithNoGrouping);
	}

	public List<DocumentTree> buildTreeNodes(Set<DocumentEnvelope> parentEnvelopes,
											 Map<String, Set<DocumentEnvelope>> documentsGroupedByGrouping) {
		List<DocumentTree> documentTrees = new ArrayList<>();
		for (DocumentEnvelope parent : parentEnvelopes) {
			DocumentTree documentTree = new DocumentTree(parent.getName());
			documentTree.setDocument(parent);

			Set<DocumentEnvelope> children = documentsGroupedByGrouping.get(parent.getName());
			for (DocumentEnvelope child : children) {
				if(child.getDocumentType().equals(DocumentType.DOCUMENT_GROUP)) {
					if(documentsGroupedByGrouping.keySet().contains(child.getName())) {
						Set<DocumentEnvelope> parents = new HashSet<>();
						parents.add(child);
						documentTree.setGroups(buildTreeNodes(parents, documentsGroupedByGrouping));
					} else {
						DocumentTree tree = new DocumentTree(child.getName());
						tree.setDocument(child);
						documentTree.getGroups().add(tree);
					}
				} else {
					documentTree.addDocument(child);
				}
			}
			documentTrees.add(documentTree);
		}
		return documentTrees;
	}

	// There should not be an intersection between the set of name-grouping pair
	// and the reverse set, otherwise there would exist a cycle. This also covers the case where the
	// document has its name and grouping attributes as the same.
	private boolean hasCyclicRelationships(Set<DocumentEnvelope> parentGroupingPairs) {
		Set<String> nameGroupingPairs = parentGroupingPairs.stream()
			.filter(de -> de.getGrouping() != null && !de.getGrouping().trim().equals(""))
			.map(de -> de.getName().concat(de.getGrouping())).collect(Collectors.toSet());

		Set<String> groupingNamePairs = parentGroupingPairs.stream()
			.filter(de -> de.getGrouping() != null && !de.getGrouping().trim().equals(""))
			.map(de -> de.getGrouping().concat(de.getName())).collect(Collectors.toSet());

		SetView<String> result = Sets.intersection(nameGroupingPairs, groupingNamePairs);
		return (result != null && !result.isEmpty());
	}

	public void addDocument(DocumentEnvelope document) {
		this.documents.add(document);
	}

	public String toJSON() {
		try {
			return JSONUtils.objectToString(this, false, true);
		} catch (Exception e) {
			throw new RuntimeException("Error serializing document " + this.name + " to json", e);
		}
	}

	public String toString() {
		return toJSON();
	}
}
