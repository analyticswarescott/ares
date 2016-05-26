package com.aw.platform.monitoring;

import java.util.Collection;

import com.aw.platform.NodeRole;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Platform-level role health
 *
 *
 *
 */
@JsonDeserialize(as=DefaultRoleStatus.class)
public interface RoleStatus extends Status {

	public enum Health {
		GOOD, //fully functional, no failures
		DEGRADED, //functional with failures, resiliency not 100%
		FAILED //failed completely, platform down
	}

	/**
	 * @return The role whose status this object is reporting
	 */
	public NodeRole getRole();

	/**
	 * @return The health of this role
	 */
	public Health getHealth();

	/**
	 * @return The status of the role on each node
	 */
	public Collection<NodeRoleStatus> getNodeStatus();

}
