package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Node service settings
 *
 *
 *
 */
public enum Node implements RoleSetting {

	PORT,

	STAT_COLLECT_INTERVAL;

	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.NODE; }

}
