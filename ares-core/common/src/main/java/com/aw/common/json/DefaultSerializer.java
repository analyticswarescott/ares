package com.aw.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

/**
 * @author jhaightdigitalguardian.com.
 */
public class DefaultSerializer<T extends JSONObject> extends StdSerializer<T> {

	public DefaultSerializer(Class<T> type) {
		super(type);
	}

	@Override
	public void serialize(JSONObject jsonObject, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeStartObject();
		String jsonString = jsonObject.toString();
		jsonGenerator.writeRaw(jsonString.substring(1, jsonString.length() - 1));
		jsonGenerator.writeEndObject();
	}
}
