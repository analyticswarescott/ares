package com.aw.common.system;

import java.util.ArrayList;
import java.util.List;

import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.util.JSONUtils;

/**
 * Metadata related to an input file to the system (bundle, edr scan, etc). This metadata can be serialized
 * onto a topic queue as a message to the system that a new file needs to be processed.
 *
 *
 *
 */
public class FileInputMetadata {

	public FileInputMetadata() {
	}

	public FileInputMetadata(HadoopPurpose purpose, String tenantID, String subPath, String path, String filename, String guid, boolean hasParts) {
		m_purpose = purpose;
		m_tenantID = tenantID;
		m_machineID = subPath;
		m_filename = filename;
		m_path = path;
		m_guid = guid;
		m_hasParts = hasParts;
	}

	public String toString() {
		return JSONUtils.objectToString(this);
	}

	/**
	 * @return The purpose of this data
	 */
	public HadoopPurpose getPurpose() { return m_purpose; }
	public void setPurpose(HadoopPurpose purpose) { m_purpose = purpose; }
	private HadoopPurpose m_purpose;

	public String getFilename() { return m_filename; }
	public void setFilename(String filename) { m_filename = filename; }
	private String m_filename;

	public String getMachineID() { return m_machineID; }
	public void setMachineID(String machineID) { m_machineID = machineID; }
	private String m_machineID;

	public String getTenantID() { return m_tenantID; }
	public void setTenantID(String tenantID) { m_tenantID = tenantID; }
	private String m_tenantID;

	public String getGuid() { return m_guid; }
	public void setGuid(String guid) { m_guid = guid; }
	private String m_guid;

	public String getPath() { return m_path; }
	public void setPath(String path) { m_path = path; }
	private String m_path;

	public List<String> getErrors() { return m_errors; }
	public void setErrors(List<String> errors) { m_errors = errors; }
	private List<String> m_errors = new ArrayList<String>();

	/**
	 * @return Whether the file is in parts - if so, it should be combined first
	 */
	public boolean getHasParts() { return m_hasParts; }
	public void setHasParts(boolean hasParts) { m_hasParts = hasParts; }
	private boolean m_hasParts;

}
