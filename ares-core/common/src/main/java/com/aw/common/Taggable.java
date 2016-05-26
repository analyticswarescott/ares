package com.aw.common;

import java.util.Set;

/**
 * Anything that can be tagged.
 *
 *
 */
public interface Taggable {

	/**
	 * @return The tags on this entity
	 */
	public Set<Tag> getTags();

	/**
	 * @param tag The tag to add
	 */
	public void addTag(Tag tag);

	/**
	 * @param tag The tag to remove
	 */
	public void removeTag(Tag tag);

	/**
	 * @param tags All tags to tag this taggable with
	 */
	public void addAll(Tag... tags);
}
