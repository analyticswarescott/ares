package com.aw.incident.action;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.auth.DefaultUser;
import com.aw.common.messaging.Topic;
import com.aw.incident.DefaultIncident;
import com.aw.incident.Incident;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Action that creates an incident. It will register the incident as a variable using the VAR_INCIDENT
 * static property of this class.
 */
public class CreateIncidentAction extends AbstractIncidentAction {

	/**
	 * The unity type for this action
	 */
	public static final String UNITY_TYPE = "incident_create";

	/**
	 * The incident variable name registered in the action context
	 */
	public static final String VAR_INCIDENT = "incident";

	public CreateIncidentAction() {
	}

	@Override
	public <T> T copy() throws ActionCopyException {
		CreateIncidentAction ret = (CreateIncidentAction)super.copy();
		ret.m_incident = new DefaultIncident(getIncident());
		return (T)ret;
	}

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		super.prepare(ctx);

		//replace variables on incident
		m_incident.setName(replaceVariables(m_incident.getName(), ctx));
		m_incident.setDescription(replaceVariables(m_incident.getDescription(), ctx));

	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_CREATE;
	}

	@Override
	public synchronized void execute(ActionContext ctx) throws ActionExecutionException {

		//add the incident to the incident topic

		//register it as a variable
		ctx.registerVariable(VAR_INCIDENT, getIncident());

		//send it along
		sendMessage(ctx.getPlatform(), Topic.INCIDENT, getIncident());

	}

	/**
	 * Builds a create incident action created by the current user
	 *
	 * @param incident The incident being created
	 */
	public CreateIncidentAction(Incident incident) {
		setIncident(incident);
		setUser(new DefaultUser(getUserID()));
	}

	@Override
	public String getIncidentGuid() {
		return String.valueOf(m_incident.getGuid());
	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	/**
	 * @return The incident being created
	 */
	@JsonProperty("inc_incident")
	public Incident getIncident() { return m_incident; }
	public void setIncident(Incident incident) { m_incident = incident; }
	private Incident m_incident;

}
