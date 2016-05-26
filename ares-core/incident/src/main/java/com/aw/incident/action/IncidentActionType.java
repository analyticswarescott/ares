package com.aw.incident.action;

import com.aw.action.Action;
import com.aw.action.ActionType;

/**
 * incident actions
 *
 *
 *
 */
public enum IncidentActionType implements ActionType {

	EVENT_ASSOCIATION(EventAssociationAction.class),
	ERROR_ASSOCIATION(ErrorAssociationAction.class),
	ALARM_ASSOCIATION(AlarmAssociationAction.class),

	INCIDENT_SET_STATE(SetStateIncidentAction.class),
	INCIDENT_UPDATE(UpdateIncidentAction.class),
	INCIDENT_ASSIGN(AssignIncidentAction.class),
	INCIDENT_COMMENT(CommentIncidentAction.class),
	INCIDENT_CREATE(CreateIncidentAction.class),
	INCIDENT_ATTACHMENT(AttachIncidentAction.class),

	DOWNLOAD_INCIDENT_ATTACHMENT(DownloadIncidentAttachmentAction.class);

	public static final String INCIDENT_UPDATE_STRING = "incident_update";

	private IncidentActionType(Class<? extends Action> classType) {
		this.classType = classType;
	}

	@Override
	public Class<? extends Action> getClassType() {
		return classType;
	}
	private Class<? extends Action> classType;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
