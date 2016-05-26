package com.aw.platform;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.util.JSONUtils;
import com.aw.platform.exceptions.PlatformConfigurationException;

public class AbstractSettings<S extends Setting, V> extends HashMap<S, V> implements Settings<S> {

	private static final long serialVersionUID = 1L;

	@Override
	public V get(S setting) {
		return super.get(setting);
	}

	public String getSetting(S setting) {
		Object ret = getSettingObject(setting);
		return ret == null ? null : ret.toString();
	}

	public String getSetting(S setting, String def) {
		Object ret = getSettingObject(setting);
		return ret == null ? def : ret.toString();
	}

	public <T extends Enum<?>> T getSetting(S setting, Class<T> type, T def) throws ConfigurationException {
		Object value = get(setting);

		try {

			if (value == null) {
				return def;
			}

			T ret = (T)type.getDeclaredMethod("valueOf", String.class).invoke(null, String.valueOf(value));
			return ret == null ? def : ret;

		} catch (Exception e) {
			throw new ConfigurationException("error getting setting " + setting, e);
		}

	}

	public Map<String, Object> getSettingMap(S setting) throws PlatformConfigurationException {
		try {
			Object ret = getSettingObject(setting);
			String s = ret == null ? null : ret.toString();
			return JSONUtils.jsonToMap(s);
		} catch (JSONException e) {
			throw new PlatformConfigurationException("error building setting map", e);
		}
	}


	public int getSettingInt(S setting) {
		Object ret = getSettingObject(setting);
		return ret == null ? -1 : Integer.parseInt(ret.toString());
	}

	public int getSettingInt(S setting, int def) {
		Object ret = getSettingObject(setting);
		return ret == null ? def : Integer.parseInt(ret.toString());
	}

	/**
	 * @param role The role
	 * @param setting The setting
	 * @return The setting object or null if not found
	 */
	protected Object getSettingObject(S setting) {
		Object ret = get(setting);
		return ret;
	}


}
