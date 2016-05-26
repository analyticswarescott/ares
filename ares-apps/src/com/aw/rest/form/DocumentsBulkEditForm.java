package com.aw.rest.form;

import com.aw.document.security.DocumentPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Holds the information submitted when editing multiple {@link com.aw.document.DocumentEnvelope}s.
 */
public class DocumentsBulkEditForm {

	@JsonProperty("document_names")
	private List<String> documentNames;

	/*
	 *	The various properties that can be edited in bulk
	 */

	// The new grouping to set
	@JsonProperty("grouping")
	private String grouping;

	// If we should remove the grouping from the documents
	@JsonProperty("remove_grouping")
	private boolean removeGrouping;

	// List of tags to add to each document - leave existing doc tags in place
	@JsonProperty("tags_to_add")
	private List<String> tags;

	// List of tags to remove from each document
	@JsonProperty("tags_to_remove")
	private List<String> tagsToRemove;

	@JsonProperty("read_permission")
	private DocumentPermission readPermission;

	@JsonProperty("write_permission")
	private DocumentPermission writePermission;

	public List<String> getDocumentNames() {
		return documentNames;
	}

	public void setDocumentNames(List<String> documentNames) {
		this.documentNames = documentNames;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	public boolean isRemoveGrouping() {
		return removeGrouping;
	}

	public void setRemoveGrouping(boolean removeGrouping) {
		this.removeGrouping = removeGrouping;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getTagsToRemove() {
		return tagsToRemove;
	}

	public void setTagsToRemove(List<String> tagsToRemove) {
		this.tagsToRemove = tagsToRemove;
	}

	public DocumentPermission getReadPermission() {
		return readPermission;
	}

	public void setReadPermission(DocumentPermission readPermission) {
		this.readPermission = readPermission;
	}

	public DocumentPermission getWritePermission() {
		return writePermission;
	}

	public void setWritePermission(DocumentPermission writePermission) {
		this.writePermission = writePermission;
	}
}
