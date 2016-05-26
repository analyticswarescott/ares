package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Zookeeper platform settings
 *
 *
 *
 */
public enum Zookeeper implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

	CONFIGURATION,

	SERVER_ID,
	PEER_PORT,
	LEADER_ELECT_PORT,

	LOG4J_OVERRIDES,
	DG_ROOT_NODE;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.ZOOKEEPER; }

}
