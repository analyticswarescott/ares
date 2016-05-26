package com.aw.platform.roles;

import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * DGMC platform settings
 *
 *
 *
 */
public enum DGMC implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT;

	//TODO: figure out jackson stuff so that it honors doing this at the Setting interface level
	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.DGMC; }

}

