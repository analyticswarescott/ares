package com.aw.unity.json;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.dg.CommonField;
import com.google.common.base.Preconditions;

/**
 * Builder for data
 *
 * TODO: rename to JSONDataBuilder?
 *
 *
 *
 */
public class DataBuilder {

	/**
	 * @param type The data type for the builder
	 * @return The data builder with the given type
	 */
	public DataBuilder withType(DataType type) {
		this.type = type;
		return this;
	}

	/**
	 * @param field A field with a value in the resulting data
	 * @param value The value for the field
	 * @return A data builder with the given field=value set on it
	 */
	public DataBuilder withField(Field field, Object value) {
		this.values.put(field, value);
		return this;
	}

	/**
	 * Build data with this builder
	 *
	 * @return The data based on the state of the builder
	 */
	public Data build() throws Exception {

		Preconditions.checkNotNull(this.type, "data type cannot be null");

		//build the json
		JSONObject json = new JSONObject();

		//set the type
		json.put(CommonField.DG_UTYPE.asField().getName(), this.type.getName());

		//add the field=value pairs using the field refs for the proper json paths
		for (JSONFieldRef ref : ((JSONDataType)this.type).getFieldRefs()) {

			//get the field/value
			Field field = this.type.getField(ref.getName());
			Object value = this.values.get(field);

			//if we have a value for this field, put the value into the json structure based on the field ref's path
			if (value != null) {
				put(json, ref.getPath(), value);
			}

		}

		//return the new data
		JSONData ret = new JSONData(this.type, json);

		//reset
		reset();

		return ret;

	}

	private void reset() {
		this.type = null;
		this.values.clear();
	}

	private void put(JSONObject json, String[] path, Object value) throws Exception {

		//put json objects up to last path entry
		for (int index=0; index<path.length-1; index++) {

			String key = path[index];

			//create the next json object in the path if not already there
			JSONObject next = json.optJSONObject(key);
			if (next == null) {
				next = new JSONObject();
				json.put(key, next);
			}

			json = next;

		}

		//now put the value using the last path entry
		json.put(path[path.length-1], value);

	}

	//the new data's type
	private DataType type;

	//the new data's fields
	private Map<Field, Object> values = new HashMap<Field, Object>();

}
