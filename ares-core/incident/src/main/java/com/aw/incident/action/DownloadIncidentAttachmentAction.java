package com.aw.incident.action;

import com.aw.action.Action;
import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.unity.dg.CommonField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.hadoop.fs.Path;

import java.io.InputStream;

/**
 * @author jhaight
 */
@SuppressWarnings("unused")
public class DownloadIncidentAttachmentAction extends AbstractIncidentAction {

	public static final String UNITY_TYPE = "download_incident_attachment";

	private AttachIncidentAction attachmentData;
	private InputStream inputStream;
	private String fileName;
	private String actionGuid;

	public DownloadIncidentAttachmentAction() { }

	public DownloadIncidentAttachmentAction(AttachIncidentAction attachmentData) {
		setIncidentGuid(attachmentData.getIncidentGuid());
		setActionFileName(attachmentData.getFileName());
		setAttachmentActionGuid(attachmentData.getGuid().toString());
		this.attachmentData = attachmentData;
	}

	@Override
	@JsonProperty(INCIDENT_GUID)
	public String getIncidentGuid() { return m_incidentGuid; }
	public void setIncidentGuid(String incidentGuid) { m_incidentGuid = incidentGuid; }
	private String m_incidentGuid = null;

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	@Override
	public ActionType getType() {
		return IncidentActionType.DOWNLOAD_INCIDENT_ATTACHMENT;
	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {
		try {
			FileWrapper file = ctx.getPlatformMgr().getTenantFileReader().read(HadoopPurpose.INCIDENT, getPath(), getFileName());
			inputStream = file.getInputStream();
		}
		catch (Exception e) {
			throw new ActionExecutionException("Download failed", this, e);
		}
	}

	@JsonProperty(Action.ACTION_GUID)
	public String getAttachmentActionGuid() {
		return actionGuid;
	}
	public void setAttachmentActionGuid(String attachmentActionGuid) {
		actionGuid = attachmentActionGuid;
	}

	@JsonProperty(CommonField.DG_FILE_NAME_STRING)
	public String getActionFileName() {
		return fileName;
	}
	public void setActionFileName(String actionFileName) {
		fileName = actionFileName;
	}

	@JsonIgnore
	public InputStream getDownloadedFile() {
		return inputStream;
	}

	private String getFileName() {
		String[] sections = getPathSections();
		return sections[sections.length - 1];
	}

	private Path getPath() {
		String[] sections = getPathSections();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < sections.length - 1; i++) {
			if (sections[i].length() != 0) {
				builder.append("/");
			}
			builder.append(sections[i]);
		}
		return new Path(builder.toString());
	}

	private String[] getPathSections() {
		return attachmentData.getFilePath().split(Path.SEPARATOR);
	}
}
