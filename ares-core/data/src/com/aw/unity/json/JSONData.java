package com.aw.unity.json;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.query.datatypes.IpAddress;
import com.aw.util.Statics;
import com.google.common.net.InetAddresses;

/**
 * Data defined in JSON - decouples us from JSON in the framework
 *
 *
 *
 */
public class JSONData implements Data {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * If a value is null in the data, it will be set to this in the value array to indicate that. This is done to
	 * differentiate values we haven't parsed yet from values that are null in the underlying json.
	 */
	public static final Object NULL = new Object();

	public JSONData(DataType type, JSONObject json) {
		setType((JSONDataType)type); //we require a JSON data type, which will include information on location of fields
		setJson(json);

		//initialize empty values array
		if (m_type != null) {
			m_values = new Object[m_type.getFields().length];
		}

		else {
			throw new InvalidDataException("Missing data type for data");
		}
	}

	public Object getValue(Field field) {

		try {

			//get the field index
			int fieldIndex = m_type.getOrdinal(field);

			//if we've already gotten the value, just return it (for performance)
			if (m_values[fieldIndex] == null) {
				String[] path = m_type.getFieldRefs()[fieldIndex].getPath();
				m_values[fieldIndex] = getValue(field, path);
			}

			//return the value
			return m_values[fieldIndex] == NULL ? null : m_values[fieldIndex];

		} catch (Exception e) {
			throw new RuntimeException(e); //TODO: type the exception
		}

	}

	private Object getValue(Field field, String... path) throws Exception {

		JSONObject object = JSONUtils.getObject(m_json, Arrays.copyOf(path, path.length-1));

		//if it's an array return the array of component values
		if (field.isArray()) {
			return toArray(field, object.getJSONArray(path[path.length-1]));
		}

		//support null values (?)
		String lastPath = path[path.length-1];
		Object value = object.opt(lastPath);
		if (value == null) {
			return NULL;
		}

		//use the json api's already parsed value to reduce overhead here (rather than reparsing with the DataType)
		switch (field.getType()) {
			case DOUBLE: return (Double)object.getDouble(lastPath);
			case FLOAT: return (float)object.getDouble(lastPath);
			case GEO_LOCATION: return object.getString(lastPath); //TODO: have a geo location type
			case INT: return object.getInt(lastPath);
			case IP_ADDRESS: return new IpAddress(object.getString(lastPath));
			case LONG: return object.getLong(lastPath);
			case MAC_ADDRESS: return object.getString(lastPath);
			case STRING: return object.getString(lastPath);
			case GUID: return object.getString(lastPath); //GUIDs are string for now
			case BOOLEAN: return object.getBoolean(lastPath);
			case OBJECT: return field.getType().parse(getType(), field, object.getJSONObject(lastPath));
			case TIMESTAMP: return readDate(object.get(lastPath));
			default: throw new Exception("Unrecognized field type " + field.getType());
		}

	}

	//return a date given a raw value - may be epoch millis or a formatted time
	private Instant readDate(Object raw) throws Exception {

		if (raw == null) {
			return null;
		}

		else if (raw instanceof Number) {
			return Instant.ofEpochMilli(((Number)raw).longValue());
		}

		else {
			return JSONUtils.parseDate(raw.toString());
		}

	}

	private Object[] toArray(Field field, JSONArray array) throws JSONException {

		Object[] ret = null;

		//if it's a sub data object, create a data array
		if (field.getType() == FieldType.OBJECT) {
			ret = new Data[array.length()];
		}

		else {
			ret = new Object[array.length()];
		}

		for (int x=0; x<array.length(); x++) {
			switch (field.getType()) {
				case DOUBLE:
					ret[x] = array.getDouble(x);
					break;
				case FLOAT:
					ret[x] = (float)array.getDouble(x);
					break;
				case INT:
					ret[x] = array.getInt(x);
					break;
				case IP_ADDRESS:
					ret[x] = InetAddresses.forString(array.getString(x));
					break;
				case LONG:
					ret[x] = array.getLong(x);
					break;
				case GEO_LOCATION:
				case MAC_ADDRESS:
				case STRING:
					ret[x] = array.getString(x);
					break;
				case TIMESTAMP:
					ret[x] = array.getLong(x); //TODO: timestamp formats?
					break;
				case BOOLEAN:
					ret[x] = array.getBoolean(x);
					break;
				case OBJECT:
					ret[x] = field.getType().parse(m_type, field, array.getJSONObject(x));
					break;
				case GUID:
					ret[x] = array.getString(x);
					break;
				default:
					throw new InvalidDataException("Urecognized type: " + field.getType());
			}
		}
		return ret;
	}

	@Override
	public Instant getTime() {

		Instant ret = null;

		//if defined, set the time for this data - will be null otherwise
		if (getType().getTimeField() != null) {
			Object timeValue = getValue(getType().getTimeField());
			if (timeValue instanceof Instant) {
				ret = (Instant)timeValue;
			} else {
				ret = Instant.ofEpochMilli((Long) timeValue);
			}
		}

		return ret;

	}

	@Override
	public String getGuid() {

		String ret = null;

		//check for null
		if (getType().getIDField() == null) {
			ret = null;
		}

		//else we have an ID field, get the id
		else {
			Object value = getValue(getType().getIDField());
			if (value != null) {
				ret = value.toString();
			} //else return null
		}

		return ret;

	}

	/**
	 * Returns the unity json format of this object. Note this implementation will guarantee that
	 * all json properties map to unity fields if strict=true
	 */
	@Override
	public String toJsonString(boolean pretty, boolean epochTime, boolean strict) {

		try {

			//only clean the json once
			if (!m_cleaned && strict) {

				clean(m_json, epochTime);
				m_cleaned = true;

			}

			//format based on the pretty flag
			if (pretty) {
				return m_json.toString(Statics.JSON_INDENT_FACTOR);
			}

			//else just single line json
			else {
				return m_json.toString();
			}

		} catch (Exception e) {
			throw new InvalidDataException("error converting to json", e);
		}

	}

	/**
	 * Cleans this json object. Cleaning will prepare the json object to be fully
	 * compatible with unity's type fields for this object. This means keys in the json
	 * that are not in the unity type will be removed.
	 *
	 * @param json The raw json
	 * @param epochTime Whether timestamps should be epoch time
	 * @throws Exception If anything goes wrong
	 */
	private void clean(JSONObject json, boolean epochTime) throws Exception {

		//remove any non-unity keys
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {

			String key = keys.next();

			Field field = m_type.getFieldFromPath(key);

			//if there is a field, clean it as necessary
			if (field != null) {
				clean(json, epochTime, field, key, keys);
			}

			else {
				//remove fields that don't exist in the type
				keys.remove();
			}

		}

	}

	//clean an existing field in the json
	private void clean(JSONObject json, boolean epochTime, Field field, String key, Iterator<String> keys) throws Exception {

		switch (field.getType()) {

			case TIMESTAMP:

				//format timestamp if needed
				if (epochTime) {
					Instant date = readDate(json.get(key));
					json.put(key, date.toEpochMilli());
				}
				break;

			case OBJECT:

				//recurse into subobjects
				Object value = getValue(field);
				if (value != null) {

					//clean each child object
					if (field.isArray()) {
						for (Data data : ((Data[])value)) {
							cleanChild(data, epochTime);
						}
					}

					//clean the single child object
					else {
						cleanChild((Data)value, epochTime);
					}

				}
				break;

			default:

				//for all other types nothing needs to be done, it's an existing field with a value

		}

	}

	//clean a child data object
	private void cleanChild(Data data, boolean epochTime) throws Exception {
		if (data instanceof JSONData) {
			JSONData jsonData = (JSONData)data;
			jsonData.clean(jsonData.m_json, epochTime);
		}
	}

	//if true, no non-unity properties exist in the json
	private boolean m_cleaned = false;

	public Object[] getValues() { return m_values; }
	public void setValues(Object[] values) { m_values = values; }
	private transient Object[] m_values;

	public JSONObject getJson() { return m_json; }
	public void setJson(JSONObject json) { m_json = json; }
	private JSONObject m_json;

	public JSONDataType getType() { return m_type; }
	public void setType(JSONDataType type) { m_type = type; }
	private JSONDataType m_type;

	public UnityInstance getUnity() { return this.unity;  }
	public void setUnity(UnityInstance unity) { this.unity = unity; }
	private UnityInstance unity;

}
