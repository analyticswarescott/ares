package com.aw.incident;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import com.aw.common.AbstractTaggable;
import com.aw.common.auth.DefaultUser;
import com.aw.common.auth.User;
import com.aw.common.rest.security.SecurityAware;
import com.aw.unity.dg.CommonField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default DG incident implementation
 *
 * Note the JsonProperty annotations bind this object to unity json - these property names
 * should match the names in the incident datatype repository json.
 *
 *
 *
 */
public class DefaultIncident extends AbstractTaggable implements Incident, SecurityAware {

	/**
	 * Sets up some basic properties for the incident
	 */
	public DefaultIncident() {

		//set up some default properties
		setState(State.CREATED);
		setCreationTime(new Date());
		setModifiedTime(new Date());
		setGuid(UUID.randomUUID());
		setCreator(new DefaultUser(getUserID()));
		setName("(not set)");

		//automatic unless explicitly set as manual
		setCreationType(CreationType.AUTOMATIC);

	}

	/**
	 * Create a new incident, initializing with properties from the other incident. Note that properties
	 * like the guid, creation time, etc will not be inherited.
	 *
	 * <p>The following properties will be inherited:</p>
	 *
	 * <li>name
	 * <li>description
	 * <li>severity
	 * <li>creation type
	 * <li>state
	 *
 	 * @param template The other incident being used as a partial template
	 */
	public DefaultIncident(Incident template) {

		this();

		//inherit some properties from the other incident
		setName(template.getName());
		setDescription(template.getDescription());
		setSeverity(template.getSeverity());
		setCreationType(template.getCreationType());
		setState(template.getState());
		setTags(new HashSet<>(template.getTags()));

	}
	//generated, use all properties for hashcode
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_assignee == null) ? 0 : m_assignee.hashCode());
		result = prime * result + ((m_closeComment == null) ? 0 : m_closeComment.hashCode());
		result = prime * result + ((m_creationTime == null) ? 0 : m_creationTime.hashCode());
		result = prime * result + ((m_creationType == null) ? 0 : m_creationType.hashCode());
		result = prime * result + ((m_creator == null) ? 0 : m_creator.hashCode());
		result = prime * result + ((m_guid == null) ? 0 : m_guid.hashCode());
		result = prime * result + ((m_modifiedTime == null) ? 0 : m_modifiedTime.hashCode());
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result + ((m_severity == null) ? 0 : m_severity.hashCode());
		result = prime * result + ((m_state == null) ? 0 : m_state.hashCode());
		return result;
	}

	//generated, use all properties for equals
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultIncident other = (DefaultIncident) obj;
		if (m_assignee == null) {
			if (other.m_assignee != null)
				return false;
		} else if (!m_assignee.equals(other.m_assignee))
			return false;
		if (m_closeComment == null) {
			if (other.m_closeComment != null)
				return false;
		} else if (!m_closeComment.equals(other.m_closeComment))
			return false;
		if (m_creationTime == null) {
			if (other.m_creationTime != null)
				return false;
		} else if (!m_creationTime.equals(other.m_creationTime))
			return false;
		if (m_creationType != other.m_creationType)
			return false;
		if (m_creator == null) {
			if (other.m_creator != null)
				return false;
		} else if (!m_creator.equals(other.m_creator))
			return false;
		if (m_guid == null) {
			if (other.m_guid != null)
				return false;
		} else if (!m_guid.equals(other.m_guid))
			return false;
		if (m_modifiedTime == null) {
			if (other.m_modifiedTime != null)
				return false;
		} else if (!m_modifiedTime.equals(other.m_modifiedTime))
			return false;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		if (m_severity != other.m_severity)
			return false;
		if (m_state != other.m_state)
			return false;
		return true;
	}

	@JsonProperty(INCIDENT_ASSIGN)
	public User getAssignee() { return m_assignee; }
	public void setAssignee(User assignee) { m_assignee = assignee; }
	private User m_assignee;

	@JsonProperty(INCIDENT_CREATOR)
	public User getCreator() { return m_creator; }
	public void setCreator(User creator) { m_creator = creator; }
	private User m_creator;

	@JsonProperty(INCIDENT_STATE)
	public State getState() { return m_state; }
	public void setState(State state) { m_state = state; }
	private State m_state;

	@JsonProperty(INCIDENT_SEVERITY)
	public Severity getSeverity() { return m_severity; }

	@JsonProperty(INCIDENT_SEVERITY_SCORE)
	@Override
	public int getSeverityScore() {
		final Severity severity = getSeverity();
		if (severity == null) {
			return 0;
		}
		switch (severity) {
			case CRITICAL:
				return 100;
			case HIGH:
				return 50;
			case MEDIUM:
				return 25;
			case MINOR:
				return 10;
			case INFORMATIONAL:
			default:
				return 1;
		}
	}

	public void setSeverity(Severity severity) { m_severity = severity; }
	private Severity m_severity;

	@JsonProperty(INCIDENT_CLOSE_COMMENT)
	public Comment getCloseComment() { return m_closeComment; }
	public void setCloseComment(Comment closeComment) { m_closeComment = closeComment; }
	private Comment m_closeComment;

	@JsonProperty(CommonField.DG_GUID_STRING)
	public UUID getGuid() { return m_guid; }
	public void setGuid(UUID guid) { m_guid = guid; }
	private UUID m_guid;

	@JsonProperty(CommonField.DG_NAME_STRING)
	public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	@JsonProperty(CommonField.DG_DESCRIPTION_STRING)
	public String getDescription() { return m_description; }
	public void setDescription(String description) { m_description = description; }
	private String m_description;

	@JsonProperty(CommonField.DG_TIME_STRING)
	public Date getCreationTime() { return m_creationTime; }
	public void setCreationTime(Date creationTime) { m_creationTime = creationTime; }
	private Date m_creationTime;

	@JsonProperty(INCIDENT_CREATION_TYPE)
	public CreationType getCreationType() { return m_creationType; }
	public void setCreationType(CreationType creationType) { m_creationType = creationType; }
	private CreationType m_creationType;

	@JsonProperty(INCIDENT_MODIFIED_TIME)
	public Date getModifiedTime() { return m_modifiedTime; }
	public void setModifiedTime(Date modifiedTime) { m_modifiedTime = modifiedTime; }
	private Date m_modifiedTime;

}
