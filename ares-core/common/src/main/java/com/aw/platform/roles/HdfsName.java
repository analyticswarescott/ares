package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * HDFS platform settings
 *
 *
 *
 */
public enum HdfsName implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

	WEB_UI_PORT,

	/**
	 * the hdfs:// URI to use to interact with HDFS
	 */
	HDFS_ROOT_PATH,

	/**
	 * replication factor for files written to HDFS
	 */
	DFS_REPLICATION,

	/**
	 * Cluster name to support use of logical URI as clients when hadoop is in HA (default)
	 */
	HA_CLUSTER_NAME,

	/**
	 * Marks this node as the standby to support initial NameNode formatting requirements of HDFS HA mode
	 */
	STANDBY;



	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.HDFS_NAME; }

}
