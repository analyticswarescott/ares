package com.aw.incident.action;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.es.ESKnownIndices;
import com.aw.incident.Incident;

/**
 * Update properties on an incident
 *
 *
 */
public class UpdateIncidentAction extends CreateIncidentAction {

	public UpdateIncidentAction() {
		super();
	}

	public UpdateIncidentAction(Incident incident) {
		super(incident);
	}

	@Override
	public synchronized void execute(ActionContext ctx) throws ActionExecutionException {

		try {

			//update the incident
			getClient(ctx).update(ESKnownIndices.INCIDENTS, Incident.UNITY_TYPE, getIncidentGuid(), JSONUtils.objectToString(getIncident()), getIncident().getCreationTime().toInstant());

		} catch (Exception e) {
			throw new ActionExecutionException("error updating incident", this, e);
		}

	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_UPDATE;
	}
}
