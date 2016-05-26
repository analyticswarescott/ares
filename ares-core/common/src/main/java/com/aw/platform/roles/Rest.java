package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * REST platform settings
 *
 *
 *
 */
public enum Rest implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

	/**
	 * configuration DB JDBC URL (could point to a local file, could be a remote host, we don't know or care)
	 */
	CONF_DB_URL,

	/**
	 * number of seconds to wait for running drivers to report
	 */
	DRIVER_REPORT_TIMEOUT,

	/**
	 * number of seconds to wait for submitted drivers to register
	 */
	DRIVER_SUBMIT_TIMEOUT,

	/**
	 * is this service member a task container
	 */
	TASK_CONTAINER,

	/**
	 * is this service member a aw api handler (bundles / edr scans)
	 */
	DG_HANDLER,

	/**
	 * the file system path of our HDFS local cache
	 */
	LOCAL_FILE_CACHE_PATH;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.REST; }

}
