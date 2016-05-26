package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Settings for config DB, including defaults for single-node operation
 */
public enum ConfigDbWorker implements RoleSetting {

	/**
	 * no real configuration at this point
	 */
	WORKER_DB_PORT;

	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.CONFIG_DB_WORKER; }
}
