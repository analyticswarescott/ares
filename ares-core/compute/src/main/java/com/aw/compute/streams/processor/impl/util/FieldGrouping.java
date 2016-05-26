package com.aw.compute.streams.processor.impl.util;

import org.apache.spark.api.java.function.PairFunction;
import org.codehaus.jettison.json.JSONObject;

import scala.Tuple2;

/**
 * Group by a particular field (or fields) in the json
 *
 *
 *
 */
public class FieldGrouping implements PairFunction<JSONObject, String, JSONObject> {

	/**
	 * default serialization id
	 */
	private static final long serialVersionUID = 1L;

	public FieldGrouping() {
	}

	/**
	 * Constructor for one field
	 *
	 * @param field
	 */
	public FieldGrouping(String... fields) {
		m_fields = fields;
	}

	@Override
	public Tuple2<String, JSONObject> call(JSONObject json) throws Exception {

		//build the key
		String key = buildKey(json);

		//build the tuple2
		Tuple2<String, JSONObject> ret = new Tuple2<String, JSONObject>(key, json);

		//we're done
		return ret;

	}

	/**
	 * Take the field array and build a string key from this json
	 * @param json
	 * @return
	 * @throws Exception
	 */
	private String buildKey(JSONObject json) throws Exception {

		//check some corner cases
		if (m_fields == null) {
			return "null";
		}

		//slight performance tweak
		else if (getFields().length == 1) {
			return json.optString(m_fields[0], "null");
		}

		StringBuilder sb = new StringBuilder();
		for (String field : m_fields) {

			if (sb.length() > 0) {
				sb.append("|");
			}

			sb.append(json.optString(field, "null"));

		}
		return sb.toString();

	}

	public String[] getFields() { return m_fields; }
	public void setFields(String[] fields) { m_fields = fields; }
	private String[] m_fields;

}
