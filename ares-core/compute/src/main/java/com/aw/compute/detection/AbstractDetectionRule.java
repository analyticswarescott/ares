package com.aw.compute.detection;

import com.aw.common.AbstractTaggable;
import com.aw.document.DocDisplayName;
import com.aw.document.DocName;

/**
 * Abstract implementation for DetectionRule
 *
 *
 *
 */
public abstract class AbstractDetectionRule extends AbstractTaggable implements DetectionRule {

	/**
	 * Human-readable name of this rule
	 */
	@DocDisplayName
    public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	/**
	 * globally unique id for this rule across all rules of this type for this tenant
	 */
	@DocName
	public String getId() { return this.id; }
	public void setId(String id) { this.id = id; }
	private String id;

}
