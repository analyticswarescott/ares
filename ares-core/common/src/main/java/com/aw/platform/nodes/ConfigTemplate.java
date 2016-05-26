package com.aw.platform.nodes;

import java.io.IOException;

/**
 * Holds a configuration template file for a 3rd party technology. If file_name attribute is set, it will map to
 * a file in the conf/templates/{config_name} directory, where config_name is the name of the document holding this
 * configuration data.
 *
 *
 *
 */
public class ConfigTemplate {

	public ConfigTemplate() {
	}

	/**
	 * @return the file contents directly injected into this object, or the content of the file referenced by the filePath property.
	 */
	public String getFileContents() throws IOException {
		return this.fileContents;

	}
	public void setFileContents(String fileContents) { this.fileContents = fileContents; }
	private String fileContents;

	public String getFilePath() { return this.filePath;  }
	public void setFilePath(String filePath) { this.filePath = filePath; }
	private String filePath;

}
