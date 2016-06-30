package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Settings for config DB, including defaults for single-node operation
 */
public enum ConfigDbMaster implements RoleSetting {

	/**
	 * replication factor provided enough nodes are defined -- otherwise = num worker nodes
	 */
	MASTER_DB_PORT,
	TARGET_REPLICATION,
	IS_OFF_PLATFORM,
	CONF_DIR;

	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.CONFIG_DB_MASTER; }
}
