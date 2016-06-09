package com.aw.incident;

import java.util.Date;
import java.util.UUID;

import com.aw.common.Taggable;
import com.aw.common.auth.User;
import com.aw.unity.dg.CommonField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An incident is something that may be actionable in the system by an operator of the system.
 *
 *
 *
 */
@JsonDeserialize(as=DefaultIncident.class) //deserialize as default
public interface Incident extends Taggable {

	/**
	 * The unity data type for incidents
	 */
	public static final String UNITY_TYPE = "incident";

	public static final String INCIDENT_ASSIGN = "inc_assign";
	public static final String INCIDENT_CREATOR = "inc_creator";
	public static final String INCIDENT_STATE = "inc_state";
	public static final String INCIDENT_SEVERITY = "inc_sev";
	public static final String INCIDENT_SEVERITY_SCORE = "inc_sev_score";
	public static final String INCIDENT_CLOSE_COMMENT = "inc_ccomment";
	public static final String INCIDENT_CREATION_TYPE = "inc_ctype";
	public static final String INCIDENT_MODIFIED_TIME = "inc_mtime";

	/**
	 * Possible states of an incident
	 */
	public enum State {

		/**
		 * not visible to anyone in particular, has to be queried to be seen
 		 */
		CREATED,

		/**
		 * open, assigned to someone
		 */
		OPEN,

		/**
		 * escalated to someone else (of higher authority)
		 */
		ESCALATED,

		/**
		 * Resolved in some way
		 */
		CLOSED;

		public String toString() { return name().toLowerCase(); }
		@JsonCreator
		public static State forValue(String val) { return State.valueOf(val.toUpperCase()); }
	}

	public enum Severity {

		/**
		 *
		 */
		INFORMATIONAL,

		/**
		 *
		 */
		MINOR,

		/**
		 *
		 */
		MEDIUM,

		/**
		 *
		 */
		HIGH,

		/**
		 *
		 */
		CRITICAL;

		public String toString() { return name().toLowerCase(); }
		@JsonCreator
		public static Severity forValue(String val) { return Severity.valueOf(val.toUpperCase()); }
	}

	/**
	 * @return The globally unique identifier for this incident
	 */
	public UUID getGuid();

	/**
	 * @return The name of this incident
	 */
	public String getName();
	public void setName(String name);

	/**
	 * @return The description of this incident
	 */
	public String getDescription();
	public void setDescription(String description);

	/**
	 * @return The user assigned the incident
	 */
	public User getAssignee();

	/**
	 * @return The user who created the incident
	 */
	public User getCreator();

	/**
	 * @return The timestamp this incident was created
	 */
	public Date getCreationTime();

	/**
	 * @return How the incident was created
	 */
	public CreationType getCreationType();
	public void setCreationType(CreationType creationType);

	/**
	 * @return The timestamp this incident was last modified
	 */
	public Date getModifiedTime();

	/**
	 * @return The severity of the incident
	 */
	public Severity getSeverity();

	/**
	 * Defines a numeric value for an incident's severity, used for sorting.
	 * @return int representation of the severity
     */
	public int getSeverityScore();

	/**
	 * @return The reason this incident was closed - null if not closed
	 */
	public Comment getCloseComment();

	/**
	 * @return The state of the incident
	 */
	public State getState();

	/**
	 * @return Statically set the unity type
	 */
	@JsonProperty(CommonField.EVENT_TYPE_FIELD)
	default public String getUnityType() { return Incident.UNITY_TYPE; }

	/**
	 * Set the guid on an incident
	 *
	 * @param uuid The new guid
	 */
	public void setGuid(UUID uuid);

}
