package com.aw.platform;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aw.platform.PlatformNode.RoleSetting;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * Settings for a node role
 *
 *
 *
 */
public class NodeRoleSettings extends AbstractSettings<RoleSetting, Object> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(NodeRoleSettings.class);

	public NodeRoleSettings() {
	}

	public NodeRoleSettings(Platform platform, NodeRole role) {
		m_platform = platform;
		m_role = role;
	}

	public NodeRoleSettings(Platform platform, NodeRole role, Map<String, Object> rawSettings) {

		//used for defaults
		m_platform = platform;

		setRole(role);

		Iterator<String> settings = rawSettings.keySet().iterator();
		while (settings.hasNext()) {

			String strSetting = settings.next();
			RoleSetting setting = role.settingValueOf(strSetting.toUpperCase());
			put(setting, rawSettings.get(strSetting));

		}

	}

	@Override
	protected Object getSettingObject(RoleSetting setting) {
		Object ret = super.getSettingObject(setting);

		//if we don't have specific settings set on this role, try the platform level settings if we have them
		if (ret == null && m_platform != null) {

			//try platform settings
			NodeRoleSettings platformSettings = m_platform.getSettings(m_role);
			if (platformSettings != null) {
				ret = platformSettings.get(setting);
			}

		}

		return ret;
	}

	@JsonIgnore
	public NodeRole getRole() { return m_role; }
	@JsonIgnore
	public void setRole(NodeRole role) { m_role = role; }
	private NodeRole m_role;

	@JsonIgnore
	public Platform getPlatform() { return m_platform; }
	@JsonIgnore
	public void setPlatform(Platform platform) { m_platform = platform; }
	private Platform m_platform;

}
