package com.aw.platform;

import java.io.InputStream;

import javax.inject.Provider;

import com.aw.common.hadoop.structure.HadoopPurpose;
import org.apache.hadoop.fs.Path;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.system.structure.PathResolverSystem;
import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Upgrade information for the platform
 *
 *
 *
 */
public class PlatformUpgrade {

	private Provider<Platform> platform;
	private PlatformMgr platformMgr;

	public PlatformUpgrade() {
	}

	public PlatformUpgrade(String json) { //TODO: why is this needed?
		try {
			JSONObject o = new JSONObject(json);
			setFilename(o.getString("filename"));
			setVersionId(o.getString("version_id"));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	public PlatformUpgrade(Provider<Platform> platform) {
		this.platform = platform;
	}

	public PlatformUpgrade(PlatformMgr platformMgr, String versionId, InputStream file) throws Exception {

		this.platformMgr = platformMgr;

		setFilename("patch" + versionId + ".tar.gz");
		setVersionId(versionId);
		setStream(file);


		this.platformMgr.addSystemFile(HadoopPurpose.PATCH, "/", getFilename(), file);

	}


	public JSONObject toJSON()  {
		try {
			JSONObject jsonObject = new JSONObject(JSONUtils.objectToString(this, false, true));
			return jsonObject;
		} catch (Exception e) {
			throw new RuntimeException("Error serializing patch  to json", e);
		}
	}

	@JsonIgnore
	public FileWrapper getPatchFile(Provider<Platform> platformProvider) throws Exception{
		FileReader reader = new FileReader(platformProvider);
		reader.initialize(new PathResolverSystem());
		return reader.read(HadoopPurpose.PATCH, new Path("/"), getFilename());
	}

	@JsonIgnore
	public InputStream getStream() { return m_stream; }
	public void setStream(InputStream stream) { m_stream = stream; }
	private InputStream m_stream;

	public String getVersionId() { return m_versionId; }
	public void setVersionId(String versionId) { m_versionId = versionId; }
	private String m_versionId;

	public String getFilename() { return m_filename; }
	public void setFilename(String filename) { m_filename = filename; }
	private String m_filename;

}
