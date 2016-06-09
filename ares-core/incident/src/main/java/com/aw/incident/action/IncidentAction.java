package com.aw.incident.action;

import com.aw.action.Action;
import com.aw.common.Taggable;
import com.aw.incident.CreationType;
import com.aw.unity.dg.CommonField;
import com.aw.util.Statics;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An action on an incident
 *
 *
 *
 */
public interface IncidentAction extends Action, Taggable {

	//default incident variable for a created incident
	public static final String VAR_INCIDENT_GUID = Statics.VARIABLE_PREFIX + CreateIncidentAction.VAR_INCIDENT + ".dg_guid" + Statics.VARIABLE_SUFFIX;

	/**
	 * @return The incident guid on which we are taking the action
	 */
	@JsonProperty("inc_guid")
	public String getIncidentGuid();

	/**
	 * @return A comment associated with the action, if any
	 */
	@JsonProperty("dg_comment")
	public String getComment();

	/**
	 * @return The unity data type this action represents
	 */
	@JsonProperty(CommonField.EVENT_TYPE_FIELD)
	public String getUnityType();

	/**
	 * @return Whether this action was generated automatically or manually by an actual user action
	 */
	public CreationType getCreationType();

	/**
	 * all incident actions are persistable by default, i.e. they will be written to the incident index
	 */
	@Override
	default boolean isPersistable() {
		return true;
	}

}
