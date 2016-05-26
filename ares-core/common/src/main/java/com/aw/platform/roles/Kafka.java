package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Kafka platform settings
 *
 *
 *
 */
public enum Kafka implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

 	BROKER_ID,

	DEFAULT_REPLICATION,

	CONFIGURATION;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.KAFKA; }

}
