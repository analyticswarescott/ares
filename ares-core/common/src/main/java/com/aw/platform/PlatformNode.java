package com.aw.platform;

import java.util.Collection;

import com.aw.platform.PlatformNode.RoleSetting;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A node in the platform. Nodes have one or more types, each of which have one or
 * more settings.
 *
 *
 */
@JsonDeserialize(as=DefaultPlatformNode.class)
public interface PlatformNode extends Settings<RoleSetting> {

	/**
	 * Available settings for a node type - should be enums defined for each platform node role
	 */
	public interface RoleSetting extends Setting {

		/**
		 * @return The role with which this setting is associated
		 */
		public NodeRole getRole();

	}

	/**
	 * @return The roles for this node
	 */
	public Collection<NodeRole> getRoles();

	/**
	 * @return The network address of this node
	 */
	public String getHost();

	/**
	 * To support platform level defaults, all nodes must be aware of the platform they belong to
	 *
	 * @param platform The platform this node belongs to
	 */
	public void setPlatform(Platform platform);

	/**
	 * Get role settings for this node
	 *
	 * @param role
	 * @return
	 */
	public NodeRoleSettings getSettings(NodeRole role);

}
