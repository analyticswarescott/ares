package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * HDFS platform settings
 *
 *
 *
 */
public enum HdfsData implements RoleSetting {

	/**
	 * data directory
	 */
	DATA_DIR;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.HDFS_DATA; }



}
