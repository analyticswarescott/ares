package com.aw.compute.streams.processor.impl.util;

import org.apache.spark.api.java.function.Function;
import org.codehaus.jettison.json.JSONObject;

/**
 * Parse json
 *
 *
 */
public class JsonMapping implements Function<String, JSONObject> {

	@Override
	public JSONObject call(String data) throws Exception {
		return new JSONObject(data);
	}

}
