package com.aw.incident.action;

import java.util.UUID;

import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.auth.DefaultUser;
import com.aw.common.auth.User;
import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Assign an incident to a user
 *
 *
 *
 */
public class AssignIncidentAction extends AbstractUpdateIncidentAction {

	public static final String UNITY_TYPE = "incident_assign";

	//the assignee property for incident actions and incidents
	private static final String PROPERTY = "inc_assign";

	public AssignIncidentAction() {
	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		//update the assignee property for the incident - if anything goes wrong an exception will be thrown here
		update(ctx, getIncidentGuid(), PROPERTY, m_assignee.getUsername());

	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_ASSIGN;
	}

	public AssignIncidentAction(UUID guid, User assignee) {
		super(guid.toString());
		setAssignee(assignee);
	}

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		if (getAssignee() == null) {
			throw new ActionPreparationException("assign action cannot have a null assignee");
		}

		setAssignee(new DefaultUser(replaceVariablesNotNull(getAssignee().getUsername(), ctx, "assignee username cannot be null")));

	}

	@Override
	public <T> T copy() throws ActionCopyException {
		AssignIncidentAction ret = (AssignIncidentAction)super.copy();

		if (m_assignee != null) {
			ret.m_assignee = new DefaultUser(m_assignee.getUsername());
		}

		return (T)ret;
	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	@JsonProperty(PROPERTY)
	public User getAssignee() { return m_assignee; }
	public void setAssignee(User assignee) { m_assignee = assignee; }
	private User m_assignee;

}
