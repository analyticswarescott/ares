package com.aw.platform;

import com.aw.common.exceptions.InitializationException;
import com.aw.platform.PlatformNode.RoleSetting;
import com.google.common.base.Preconditions;

/**
 * Builder for platforms
 *
 *
 *
 */
public class PlatformBuilder {

	DefaultPlatform m_platform;

	public PlatformBuilder() {
		m_platform = new DefaultPlatform();
	}

	/**
	 * Returns the built platform. Resets the state of the builder.
	 *
	 * @return The built platform
	 */
	public Platform build() {
		DefaultPlatform ret = m_platform;
		m_platform = new DefaultPlatform();
		return ret;
	}

	/**
	 * Add a platform-wide setting for a role
	 * @param setting
	 * @param value
	 *
	 * @return
	 */
	public PlatformBuilder withSetting(RoleSetting setting, Object value) {

		//add the setting
		m_platform.getSettings().addSetting(setting.getRole(), setting, value);

		//return this builder
		return this;

	}

	/**
	 * Add a setting for a particular node
	 *
	 * @param host
	 * @param setting
	 * @param value
	 * @return
	 * @throws InitializationException
	 */
	public PlatformBuilder withSetting(String host, RoleSetting setting, Object value) {

		//get the node
		DefaultPlatformNode node = (DefaultPlatformNode)m_platform.getNode(host);

		//make sure we have the node
		Preconditions.checkState(node != null, "node must be present before setting is added");

		//add the setting
		node.addSetting(setting.getRole(), setting, value);

		//return this builder
		return this;

	}

	public PlatformBuilder withNode(String host, NodeRole... roles) {

		Preconditions.checkArgument(roles != null, "roles cannot be null");

		DefaultPlatformNode node = new DefaultPlatformNode();
		node.setPlatform(m_platform);
		node.setHost(host);

		for (NodeRole role : roles) {

			//add the role
			node.addRole(role);

		}

		//add the node
		m_platform.add(host, node);

		//return this builder
		return this;

	}

}
