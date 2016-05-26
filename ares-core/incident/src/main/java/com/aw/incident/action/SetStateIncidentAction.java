package com.aw.incident.action;

import java.util.UUID;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.incident.Incident.State;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Set the state of an incident
 *
 *
 *
 */
public class SetStateIncidentAction extends AbstractUpdateIncidentAction {

	public static final String UNITY_TYPE = "incident_set_state";

	//the property that gets updated when the state changes
	public static final String PROPERTY = "inc_state";

	public SetStateIncidentAction() {
	}

	public SetStateIncidentAction(UUID guid, State newState) {
		super(guid.toString());
		setState(newState);
	}

	@Override
	public <T> T copy() throws ActionCopyException {
		SetStateIncidentAction ret = (SetStateIncidentAction)super.copy();
		ret.m_state = m_state;
		return (T)ret;
	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		update(ctx, getIncidentGuid(), PROPERTY, m_state == null ? null : m_state.toString());

	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_SET_STATE;
	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	/**
	 * @return The new state for the incident
	 */
	@JsonProperty(PROPERTY)
	public State getState() { return m_state; }
	public void setState(State state) { m_state = state; }
	private State m_state;

}
