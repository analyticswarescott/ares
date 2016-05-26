package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Spark master platform settings
 *
 *
 *
 */
public enum SparkMaster implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

	/**
	 * the master port
	 */
	SPARK_MASTER_PORT,

	/**
	 * the Web UI port for the spark master
	 */
	SPARK_MASTER_UI_PORT,

	/**
	 * the rest submission port for the spark master
	 */
	SPARK_REST_PORT,

	LOG4J_OVERRIDES,

	SPARK_DAEMON_JAVA_OPTS;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.SPARK_MASTER; }

}
