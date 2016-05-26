package com.aw.platform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PlatformSettingsDeserializer extends JsonDeserializer<PlatformSettings> {

	@Override
	public PlatformSettings deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		//read the settings
		String json = p.readValueAsTree().toString();
		Map<String, Map<String, Object>> map = new HashMap<>();
        JSONUtils.updateFromString(json, map);
        return new PlatformSettings(map);

	}

}
