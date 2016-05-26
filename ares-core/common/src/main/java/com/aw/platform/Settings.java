package com.aw.platform;

import java.util.Map;

import com.aw.platform.exceptions.PlatformConfigurationException;

public interface Settings<T extends Setting> {

	/**
	 * Required setting
	 * @param setting The setting requested for the given node type
	 *
	 * @return The setting
	 */
	public String getSetting(T setting);

	/**
	 * Optional setting
	 * @param setting The setting requested for the given node type
	 * @param def
	 *
	 * @return The setting, or default if not defined
	 */
	public String getSetting(T setting, String def);

	/**
	 * Required integer setting
	 * @param setting The setting requested
	 *
	 * @return The setting, or default if not defined
	 */
	public int getSettingInt(T setting);

	/**
	 *
	 * Optional integer setting
	 * @param setting The setting requested
	 * @param def The default setting if any
	 *
	 * @return The setting, or default if not defined
	 */
	public int getSettingInt(T setting, int def);


	/**
	 * Get a raw object given the setting
	 *
	 * @param setting
	 * @return
	 */
	public Object get(T setting);

	/**
	 * Get a map of settings given the setting
	 *
	 * @param setting
	 * @return
	 * @throws PlatformConfigurationException
	 */
	public Map<String, Object> getSettingMap(T setting) throws PlatformConfigurationException;

}
