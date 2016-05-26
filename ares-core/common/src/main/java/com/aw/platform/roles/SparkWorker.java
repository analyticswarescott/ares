package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Spark worker platform settings
 *
 *
 *
 */
public enum SparkWorker implements RoleSetting {

	/**
	 * the number of cores per node to be used for executors: default is all avaliable cores
	 */
	SPARK_ENV,

	WEB_UI_PORT,

	SPARK_DAEMON_JAVA_OPTS;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.SPARK_WORKER; }



}
