package com.aw.incident.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aw.action.ActionContext;
import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.Tag;
import com.aw.unity.UnityInstance;
import com.aw.unity.query.Filter;
import com.aw.util.Statics;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Associate an incident with something
 *
 *
 *
 */
@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
		property=AssociateIncidentAction.ASSOCIATION_TYPE)
@JsonSubTypes({
		@Type(value = EventAssociationAction.class, name=EventAssociationAction.TYPE_NAME),
		@Type(value = ErrorAssociationAction.class, name=ErrorAssociationAction.TYPE_NAME),
		@Type(value = AlarmAssociationAction.class, name=AlarmAssociationAction.TYPE_NAME) })
public abstract class AssociateIncidentAction extends AbstractIncidentAction {

	public static final String ASSOCIATION_TYPE = "inc_assoc_type";

	public static final String UNITY_TYPE = "incident_association";

	//default incident variable for a created incident
	public static final String VAR_INCIDENT_GUID = Statics.VARIABLE_PREFIX + CreateIncidentAction.VAR_INCIDENT + ".dg_guid" + Statics.VARIABLE_SUFFIX;

	//event guid for an event associated with an incident
	public static final String VAR_EVENT_GUID = Statics.VARIABLE_PREFIX + "event.dg_guid" + Statics.VARIABLE_SUFFIX;

	//event type for the associated event
	public static final String VAR_EVENT_TYPE = Statics.VARIABLE_PREFIX + "event.type.name" + Statics.VARIABLE_SUFFIX;

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		//error out if we are missing anything

		//the incident guid
		String guid = replaceVariablesNotNull(getIncidentGuid(), ctx, "incident guid cannot be null");
		m_incidentGuid = guid;

		//set associated object guid
		for (int x=0; x<m_associatedIds.size(); x++) {
			String assocGuid = replaceVariablesNotNull(getAssociatedIds().get(x), ctx, "associated id cannot be null");
			m_associatedIds.set(x, assocGuid);
		}

	}

	@Override
	public <T> T copy() throws ActionCopyException {
		AssociateIncidentAction ret = (AssociateIncidentAction)super.copy();

		ret.m_incidentGuid = m_incidentGuid;
		ret.m_associatedIds = new ArrayList<String>(m_associatedIds);

		return (T)ret;
	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		//the storing of the association in elasticsearch will handle the association itself, which is taken care of by the framework

	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	/**
	 * @return The filter as a Unity filter object
	 */
	@JsonIgnore
	public Filter getFilterObject(UnityInstance unity) throws Exception {
		Filter ret = null;
		if (getFilter() != null) {
			return unity.newFilter(getFilter());
		}
		return ret;
	}

	/**
	 * The incident being associated with something
	 */
	@JsonProperty("inc_guid")
	public String getIncidentGuid() { return m_incidentGuid; }
	public void setIncidentGuid(String incidentGuid) { m_incidentGuid = incidentGuid; }
	private String m_incidentGuid = null;

	/**
	 * @return type of association, needed in the association data type
	 */
	@JsonProperty(TYPE)
	public String getAssociationType() { return getType().toString(); }

	/**
	 * @return The ID(s) of the associated entity or entities
	 */
	@JsonProperty("inc_assoc_id")
	public List<String> getAssociatedIds() { return m_associatedIds; }
	public void setAssociatedIds(List<String> associatedIds) { m_associatedIds = associatedIds; }
	private List<String> m_associatedIds = null;

	/**
	 * Tags assigned to this association
	 */
	@JsonProperty("dg_tags")
	public Set<Tag> getTags() { return m_tags; }
	public void setTags(Set<Tag> tags) { m_tags = tags; }
	private Set<Tag> m_tags = new HashSet<Tag>();

	/**
	 * @return The json unity filter for this association, if any
	 */
	@JsonProperty("inc_assoc_filter")
	public String getFilter() { return m_filter; }
	public void setFilter(String filter) { m_filter = filter; }
	private String m_filter = null;

}
