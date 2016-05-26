package com.aw.common.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author jhaight
 */
public class DefaultModule extends SimpleModule {

	public DefaultModule() {
		addSerializer(JSONObject.class, new DefaultSerializer<>(JSONObject.class));
	}
}
