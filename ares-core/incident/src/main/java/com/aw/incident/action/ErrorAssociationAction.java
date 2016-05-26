package com.aw.incident.action;

import java.util.ArrayList;
import java.util.Collections;

import com.aw.action.ActionType;

/**
 * Associate an event to an incident using standard variable replacement
 *
 *
 *
 */
public class ErrorAssociationAction extends AssociateIncidentAction {

	public static final String TYPE_NAME = "error_association";

	public ErrorAssociationAction() {
		setIncidentGuid(VAR_INCIDENT_GUID);
		setAssociatedIds(new ArrayList<String>(Collections.singleton(VAR_EVENT_GUID)));
	}

	@Override
	public ActionType getType() {
		return IncidentActionType.valueOf(TYPE_NAME.toUpperCase()); //bind static string to enum - static string needed for jackson
	}


}
