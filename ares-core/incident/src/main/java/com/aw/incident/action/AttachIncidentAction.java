package com.aw.incident.action;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.unity.dg.CommonField;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.hadoop.fs.Path;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

/**
 * An action that attaches a file to an incident
 *
 *
 *
 */
public class AttachIncidentAction extends AbstractUpdateIncidentAction {

	/**
	 * The unity type of the incident attachment action type
	 */
	public static final String UNITY_TYPE = "incident_attachment";

	/**
	 * The file to attach
	 */
	private InputStream attachment;
	/**
	 * The display name of file
	 */
	private String fileName;
	/**
	 * The location the file is actually stored in hadoop
	 */
	private String hadoopFilePath = null;

	public AttachIncidentAction() { }

	public AttachIncidentAction(InputStream attachment, String fileName, UUID guid) {
		super(guid.toString());
		this.attachment = attachment;
		this.fileName = fileName;
	}

	@JsonProperty(CommonField.DG_FILE_NAME_STRING)
	public String getFileName() {
		return fileName;
	}

	@JsonProperty(CommonField.DG_FILE_PATH_STRING)
	public String getFilePath() {
		return hadoopFilePath;
	}
	public void setFilePath(String filePath) {
		hadoopFilePath = filePath;
	}

	@Override
	public String getUnityType() {
		return UNITY_TYPE;
	}

	@Override
	public ActionType getType() {
		return IncidentActionType.INCIDENT_ATTACHMENT;
	}

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {
		try {

			String path = Path.SEPARATOR + getIncidentGuid();
			String uniqueFileName = String.valueOf(Instant.now().toEpochMilli());
			setFilePath(path + Path.SEPARATOR + uniqueFileName);

			ctx.getPlatformMgr().getTenantFileWriter().writeStreamToFile(HadoopPurpose.INCIDENT, new Path(path), uniqueFileName, attachment);

		} catch (Exception e) {
			throw new ActionExecutionException(e);
		}
	}
}
