package com.aw.platform;

import java.util.HashMap;
import java.util.Map;

import com.aw.platform.PlatformNode.RoleSetting;
import com.aw.platform.exceptions.PlatformConfigurationException;

/**
 * Settings for a platform, keyed on NodeRole
 *
 *
 *
 */
public class PlatformSettings extends HashMap<NodeRole, NodeRoleSettings> implements Settings<RoleSetting> {

	private static final long serialVersionUID = 1L;

	public PlatformSettings() {
	}

	public PlatformSettings(Map<String, Map<String, Object>> settings) {
		initialize(settings);
	}

	private void initialize(Map<String, Map<String, Object>> settings) {

		for (String strRole : settings.keySet()) {

			//get the role
			NodeRole role = NodeRole.fromString(strRole);

			//get the NodeRoleSettings
			NodeRoleSettings roleSettings = new NodeRoleSettings();
			Map<String, Object> map = settings.get(strRole);
			for (String strRoleSetting : map.keySet()) {
				RoleSetting roleSetting = role.settingValueOf(strRoleSetting);
				roleSettings.put(roleSetting, map.get(strRoleSetting));
			}

			//add them
			put(role, roleSettings);

		}
	}

	public String getSetting(RoleSetting setting) {
		NodeRoleSettings settings = getSettings().get(setting.getRole());
		return settings == null ? null : settings.getSetting(setting);
	}

	public String getSetting(RoleSetting setting, String def) {
		NodeRoleSettings settings = getSettings().get(setting.getRole());
		return settings == null ? null : settings.getSetting(setting);
	}

	public Map<String, Object> getSettingMap(RoleSetting setting) throws PlatformConfigurationException {
		NodeRoleSettings settings = getSettings().get(setting.getRole());
		return settings == null ? null : settings.getSettingMap(setting);
	}

	public int getSettingInt(RoleSetting setting) {
		return getSettingInt(setting, -1);
	}

	public int getSettingInt(RoleSetting setting, int def) {
		NodeRoleSettings settings = getSettings().get(setting.getRole());
		return settings == null ? -1 : settings.getSettingInt(setting, def);
	}

	public Object get(RoleSetting setting) {
		return super.get(setting);
	}

	/**
	 * @return The map of settings
	 */
	protected Map<NodeRole, NodeRoleSettings> getSettings() { return this; }

}
