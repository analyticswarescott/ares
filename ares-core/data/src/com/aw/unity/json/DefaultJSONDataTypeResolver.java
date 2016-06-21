package com.aw.unity.json;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.dg.CommonField;
import com.aw.unity.exceptions.InvalidDataException;

/**
 * Default data type resolver, uses json values to resolve data types.
 *
 * Example configuration:
 * <code><pre>
 * {
 * 		"type" : "com.aw.unity.json.DefaultJSONDataTypeResolver",
 * 		"mappings" : [
 *     		{
 * 		   		"path" : [ "path", "to", "field" ],
 *     			"value" : "value",
 *     			"data_type" : "type_name"
 *     		}
 * 		]
 * }
 * </pre></code>
 *
 */
public class DefaultJSONDataTypeResolver implements JSONDataTypeResolver {

	/**
	 * The unity type json key that must be present to resolve to a data type
	 */
	private static final String MAPPINGS = "mappings";
	private static final String PATH = "path";
	private static final String DATA_TYPE = "data_type";
	private static final String VALUE = "value";

	public DefaultJSONDataTypeResolver() {
	}

	public DefaultJSONDataTypeResolver(Mapping[] mappings) {
		m_mappings = mappings;
	}

	public void initialize(DataTypeRepository repository, JSONObject cfg) throws Exception {

		m_repository = repository;

		//build a map of mappings - make sure only one mapping instance per path
		Map<String, Mapping> mappings = new HashMap<String, Mapping>();

		//get the mappings array - this is required
		JSONArray array = cfg.getJSONArray(MAPPINGS);

		for (int x=0; x<array.length(); x++) {

			//build the path array
			JSONObject mappingJson = array.getJSONObject(x);
			JSONArray path = mappingJson.getJSONArray(PATH);
			String[] pathAry = new String[path.length()];
			for (int curP=0; curP<pathAry.length; curP++) {
				pathAry[curP] = path.getString(curP);
			}

			//get the data type
			String strDataType = mappingJson.getString(DATA_TYPE);
			DataType dataType = repository.getDataType(strDataType);

			//get the value
			String value = mappingJson.getString(VALUE);

			//build a key
			String key = Arrays.toString(pathAry);

			//make sure we have a data type
			if (dataType == null) {
				throw new InvalidDataException("Data type mapping contains an unknown data type: " + strDataType);
			}

			//add the mapping if we need to
			Mapping mapping = mappings.get(key);
			if (mapping == null) {
				mapping = new Mapping();
				mapping.m_path = pathAry;
				mappings.put(key, mapping);
			}
			mapping.m_valueMap.put(value, dataType);

			//store by datatype as well
			m_dataTypeMap.put(dataType, new SimpleEntry<String[], String>(pathAry, value));

		}

		if (cfg.has("exists")) {
			m_exists = JSONUtils.objectFromString(cfg.getJSONArray("exists").toString(), String[].class);
		}

		m_mappings = mappings.values().toArray(new Mapping[mappings.size()]);

	}

	/**
	 * @return Provide the type for serialization
	 */
	public String getType() {
		return getClass().getName();
	}

	/**
	 * @return The mappings for this resolver
	 */
	public Mapping[] getMappings() { return m_mappings; }
	private Mapping[] m_mappings;

	/**
	 * Resolve a datatype to a json property path and value, which is how this resolver resolves
	 * datatypes. If value is null, just the existence of the key identifies the datatype.
	 *
	 * @param datatype The datatype whose mapping is sought
	 * @return The mapping of json attribute/value for this datatype- the key is the json path, the value is the json value
	 */
	public SimpleEntry<String[], String> getMapping(DataType datatype) {

		return m_dataTypeMap.get(datatype);

	}

	private boolean pathExists(JSONObject json) throws Exception {

		//no path means the exist constraint passes
		if (m_exists == null) {
			return true;
		}

		else {
			return pathExists(json, m_exists, 0);
		}

	}

	private boolean pathExists(JSONObject json, String[] exists, int index) throws Exception {

		if (index < exists.length - 1) {
			return pathExists(json.getJSONObject(exists[index]), exists, index+1);
		}

		else {
			return json.has(exists[index]);
		}

	}

	public DataType resolve(Object rawData) {

		try {

			Object arg = rawData;
			JSONObject json = null;
			DataType ret = null;

			//if it's a string convert it to json
			if (rawData instanceof String) {
				json = new JSONObject(rawData.toString());
			}

			//else it better be a json object already
			else {
				json = (JSONObject)arg;
			}

			//check for common aw unity type property
			String type = json.optString(CommonField.EVENT_TYPE_FIELD.toString(), null);
			if (type != null) {
				return m_repository.getDataType(type);
			}

			//if we don't have the path required, return null
			if (!pathExists(json)) {
				return null;
			}

			//for each path, check if we have a data type mapped
			for (Mapping m : m_mappings) {

				String value = getValue(m, json);
				ret = m.m_valueMap.get(value);

				//short circuit here when found
				if (ret != null) {
					break;
				}

			}

			return ret;

		} catch (Exception e) {
			throw new InvalidDataException("Could not resolve data type from json", e);
		}

	}

	private String getValue(Mapping m, JSONObject o) throws Exception {

		//all entries in the array are keys along the path to the value, excluding the last entry in the path
		for (int x=0; x<m.m_path.length - 1; x++) {
			o = o.optJSONObject(m.m_path[x]);
			if (o == null) {
				return null;
			}
		}

		//last enter in the path array is the value key
		return o.optString(m.m_path[m.m_path.length - 1]);

	}

	/**
	 * @return The key for which existence should be checked - if true, the datatype resolution can be attempted
	 */
	public String[] getExists() { return m_exists; }
	public void setExists(String[] exists) { m_exists = exists; }
	private String[] m_exists;

	public static class Mapping {

		public Mapping() {
		}

		public Mapping(String[] exists, Map<String, DataType> valueMap, String... path) {
			m_valueMap = valueMap;
			m_path = path;
		}

		public String[] getPath() { return m_path; }
		public void setPath(String[] path) { m_path = path; }
		private String[] m_path;

		public Map<String, DataType> getValueMap() { return m_valueMap; }
		private Map<String, DataType> m_valueMap = new HashMap<>();

	}

	//map datatypes to their json path and values for easy retrieval
	private Map<DataType, SimpleEntry<String[], String>> m_dataTypeMap = new HashMap<>();
	private DataTypeRepository m_repository;

}
