package com.aw.incident.action;

import java.util.UUID;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.incident.Incident;
import com.aw.unity.DataType;

/**
 * Make a comment on an incident
 *
 *
 *
 */
public class CommentIncidentAction extends AbstractUpdateIncidentAction implements TenantAware {

	public static final String UNITY_TYPE = "incident_comment";

	//default timeout - TODO: make timeouts configurable
	private static final long TIMEOUT = 300000L;

	//maximum tries to try to update atomically before we give up
	private static final int MAX_UPDATE_TRIES = 10;

	//don't let the comment list grow beyond this for now - comments will always be recorded in the action log
	private static final int DEFAULT_MAX_COMMENTS = 100;

	//settings used to be patient and wait for an incident
	private static final int INCIDENT_RETRIES = 3;
	private static final long INCIDENT_RETRY_WAIT = 1000L;

	public CommentIncidentAction(UUID guid, String comment) {
		super(guid.toString());
		setComment(comment);
	}

	public CommentIncidentAction() {
	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_COMMENT;
	}

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		//build the comment given the action context
		String comment = replaceVariables(getComment(), ctx);
		setComment(comment);

		//and the incident guid
		String incidentGuid = getIncidentGuidString(ctx);
		setIncidentGuid(incidentGuid);

	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		try {

			//get the incident we need
			DataType incidentType = ctx.getUnity().getMetadata().getDataType(Incident.UNITY_TYPE);

			if (incidentType == null) {
				throw new ActionExecutionException("no incident type from unity", this);
			}

		} catch (Exception e) {
			throw new ActionExecutionException("error adding comment to incident " + getIncidentGuid(), this, e);
		}

	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	/**
	 * @return The maximum comments on the incident being modified - TODO: common place for this
	 */
	public int getMaxComments() { return m_maxComments; }
	public void setMaxComments(int maxComments) { m_maxComments = maxComments; }
	private int m_maxComments = DEFAULT_MAX_COMMENTS;

}
