package com.aw.incident.action;

import java.util.ArrayList;
import java.util.Collections;

import com.aw.action.ActionType;
import com.aw.util.Statics;

/**
 * Associate an event to an incident using standard variable replacement
 *
 *
 *
 */
public class EventAssociationAction extends AssociateIncidentAction {

	//variable name for associated event
	public static final String VAR_EVENT = "event";

	//association type
	public static final String TYPE_NAME = "event_association";

	//default incident variable for a created incident
	public static final String VAR_INCIDENT_GUID = Statics.VARIABLE_PREFIX + CreateIncidentAction.VAR_INCIDENT + ".dg_guid" + Statics.VARIABLE_SUFFIX;

	//event guid for an event associated with an incident
	public static final String VAR_EVENT_GUID = Statics.VARIABLE_PREFIX + "event.dg_guid" + Statics.VARIABLE_SUFFIX;

	//event type for the associated event
	public static final String VAR_EVENT_TYPE = Statics.VARIABLE_PREFIX + "event.type.name" + Statics.VARIABLE_SUFFIX;

	public EventAssociationAction() {
		setIncidentGuid(VAR_INCIDENT_GUID);
		setAssociatedIds(new ArrayList<>(Collections.singleton(VAR_EVENT_GUID)));
	}

	@Override
	public ActionType getType() {
		return IncidentActionType.valueOf(TYPE_NAME.toUpperCase()); //bind static string to enum - static string needed for jackson
	}

}
