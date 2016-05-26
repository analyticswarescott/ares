package com.aw.platform;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Default implementation of a platform node.
 *
 *
 */
public class DefaultPlatformNode extends AbstractPlatformNode {

	private static final long serialVersionUID = 1L;

	public DefaultPlatformNode() {
	}

	public DefaultPlatformNode(String host, RoleSetting setting, Object value) {
		this.m_host = host;
		addRole(setting.getRole());
		addSetting(setting.getRole(), setting, value);
	}

	public void initialize(Platform platform, Map<String, Map<String, Object>> data) throws JSONException {

		//set our platform
		m_platform = platform;

		Iterator<String> roles = data.keySet().iterator();

		while (roles.hasNext()) {

			//initialize the settings map for each role
			String strRole = roles.next();
			Map<String, Object> rawSettings = data.get(strRole);
			NodeRole role = NodeRole.valueOf(strRole.toUpperCase());
			NodeRoleSettings settings = new NodeRoleSettings(m_platform, role, rawSettings);
			addRole(role, settings);

		}

	}

	@Override
	public String getSetting(RoleSetting setting) {
		return getSettings(setting.getRole()).getSetting(setting);
	}

	public void addSetting(NodeRole role, RoleSetting setting, Object value) {

		//add settings if missing
		if (get(role) == null) {
			addRole(role);
		}

		//add the setting
		((NodeRoleSettings)get(role)).put(setting, value);

	}

	/**
	 * Add role with empty settings
	 *
	 * @param role The role to add
	 */
	public void addRole(NodeRole role) {
		addRole(role, new NodeRoleSettings(m_platform, role));
	}

	/**
	 * Add role with given settings - will override any existing settings for this role
	 *
	 * @param role The role to add
	 * @param settings The settings
	 */
	public void addRole(NodeRole role, NodeRoleSettings settings) {
		put(role, settings);
	}

	@Override
	public Collection<NodeRole> getRoles() {
		return keySet();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_host == null) ? 0 : m_host.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultPlatformNode other = (DefaultPlatformNode) obj;
		if (m_host == null) {
			if (other.m_host != null)
				return false;
		} else if (!m_host.equals(other.m_host))
			return false;
		return true;
	}

	/**
	 * @return The platform this node belongs to
	 */
	@JsonIgnore
	public Platform getPlatform() { return m_platform; }
	@JsonIgnore
	public void setPlatform(Platform platform) { m_platform = platform; }
	private Platform m_platform;

	/**
	 * @return The host address of this node.
	 */
	@JsonIgnore
	public String getHost() { return m_host; }
	@JsonIgnore
	public void setHost(String host) { m_host = host; }
	private String m_host;

}
