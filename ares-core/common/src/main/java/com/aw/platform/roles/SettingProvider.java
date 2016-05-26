package com.aw.platform.roles;

import com.aw.platform.PlatformNode.RoleSetting;

/**
 * A provider of settings
 *
 *
 *
 */
public interface SettingProvider {

	/**
	 * Provide the setting given its string name
	 *
	 * @param str The string name of the setting
	 * @return The setting object (not the value but the key)
	 */
	public RoleSetting settingValueOf(String str);

}
