package com.aw.platform;

import java.util.Collection;

/**
 * Base class for platform node which provides some default implementation.
 *
 *
 */
public abstract class AbstractPlatformNode extends PlatformSettings implements PlatformNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Get the roles for this node
	 */
	public abstract Collection<NodeRole> getRoles();

	@Override
	public NodeRoleSettings getSettings(NodeRole role) {
		return (NodeRoleSettings)get(role);
	}

}
