package com.aw.common;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Convenience base implementation for a taggable object
 *
 *
 */
public class AbstractTaggable implements Taggable {

	@Override
	public void addTag(Tag tag) { m_tags.add(tag); }
	@Override
	public void removeTag(Tag tag) { m_tags.remove(tag); }

	public void addAll(Tag... tags) {
		if (tags != null) {
			for (Tag tag : tags) {
				addTag(tag);
			}
		}
	}

	/**
	 * TODO: CommonField needs to move to common or this needs to move to have awareness of dg_tags common field
	 */
	@JsonProperty("dg_tags")
	public Set<Tag> getTags() { return m_tags; }
	protected void setTags(Set<Tag> tags) { m_tags = tags; }
	private Set<Tag> m_tags = new HashSet<Tag>();

}
