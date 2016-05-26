package com.aw.incident.action;

import java.util.ArrayList;
import java.util.Collections;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.alarm.action.AlarmAction;
import com.aw.util.Statics;

/**
 * associate one or more alarms to an incident
 *
 *
 */
public class AlarmAssociationAction extends AssociateIncidentAction {

	//association type
	public static final String TYPE_NAME = "alarm_association";

	//the alarm guid
	public static final String VAR_ALARM_GUID = Statics.VARIABLE_PREFIX + AlarmAction.VAR_ALARM + ".guid" + Statics.VARIABLE_SUFFIX;

	public AlarmAssociationAction() {

		//use the registered incident by default for auto associations
		setIncidentGuid(VAR_INCIDENT_GUID);

		//use the registered alarm by default for auto associations
		setAssociatedIds(new ArrayList<>(Collections.singletonList(VAR_ALARM_GUID)));

	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		//it will be persisted, associating the alarm(s)

	}

	@Override
	public ActionType getType() {
		return IncidentActionType.valueOf(TYPE_NAME.toUpperCase());
	}

	/**
	 * the associations between alarms and incidents should be persisted
	 */
	@Override
	public boolean isPersistable() {
		return true;
	}


}
