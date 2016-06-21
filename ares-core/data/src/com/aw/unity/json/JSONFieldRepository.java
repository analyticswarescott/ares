package com.aw.unity.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.unity.Field;
import com.aw.unity.FieldRepository;
import com.aw.unity.FieldType;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidConfigurationException;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.exceptions.InvalidFieldException;
import com.aw.util.SetMap;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A list of fields and their types. This is a semantic definition of a field, not a physical definition of where
 * that field exists. Data types define the physical details. A field exists once across the entire system.
 *
 * Example:
 * <code><pre>[
	{
		"name" : "field1",
 		"type" : "float"
	},
	{
		"name" : "field2",
		"type" : "ip_address"
 	}
	{
		"name" : "field3",
		"type" : "string"
 	}
	{
		"name" : "field4",
		"type" : "object",
		"object_type" : "unity_data_type"
 	}
	{
		"name" : "field5",
		"type" : "long"
 	}
	{
		"name" : "field6",
		"type" : "ip_address",
		"array" : true
 	}
]</pre></code>
 *
 *
 */
public class JSONFieldRepository implements FieldRepository {


	/**
	 * properties used in the count field - should not be accessed outside of this class
	 */
	private final Field COUNT_FIELD = new Field("*", FieldType.INT);

	/**
	 * Builds a field repository from a collection of fields.
	 *
	 * TODO: add this to a more generic factory interface
	 *
	 * @param fields The fields to use
	 * @return The repository
	 */
	public static <T extends FieldRepository> T fromCollection(UnityInstance unity, Collection<Field> fields) {
		return fromCollection(unity, fields, true);
	}
	public static <T extends FieldRepository> T fromCollection(UnityInstance unity, Collection<Field> fields, boolean checkForDuplicates) {
		JSONFieldRepository ret = new JSONFieldRepository();
		ret.unity = unity;
		ret.m_fields = fields.toArray(new Field[fields.size()]);
		ret.initialize(checkForDuplicates);
		return (T)ret;
	}

	/**
	 * @param json
	 * @return Read the fields array from a json string
	 */
	public static JSONFieldRepository fromString(UnityInstance unity, String strJson) {

		try {

			JSONObject json = new JSONObject(strJson);
			JSONFieldRepository ret = new JSONFieldRepository();
			ret.m_fields = JSONUtils.objectFromString(json.getJSONArray("fields").toString(), Field[].class);
			ret.unity = unity;
			ret.initialize(true);

			return ret;

		} catch (Exception e) {
			throw new InvalidConfigurationException("field json error", e);
		}

	}

	@Override
	public Field getField(String name) {

		Field ret = m_fieldMap.get(name);

		if (ret == null) {
			throw new InvalidFieldException("Field " + name + " not found");
		}

		return ret;

	}

	/**
	 * Build the master field map
	 */
	private void initialize(boolean checkForDuplicates) {

		//enforce no duplicate display names
		SetMap<String, Field> duplicates = new SetMap<String, Field>();
		SetMap<String, Field> displayNames = new SetMap<String, Field>();

		for (Field field : m_fields) {

			field.setUnity(unity);

			//add this field to our tracking map for duplicate display names
			displayNames.add(field.getDisplayName(), field);

			//check duplicates if this field's display name is not null
			if (field.getDisplayName() != null) {

				//if a duplicate, add it to the duplicate set
				if (displayNames.get(field.getDisplayName()).size() > 1) {
					duplicates.put(field.getDisplayName(), displayNames.get(field.getDisplayName()));
				}

			}

			m_fieldMap.put(field.getName(), field);

		}

		//throw an exception if there are duplicate display names - this should never happen
		if (checkForDuplicates && duplicates.size() > 0) {

			throw new InvalidFieldException("duplicate fields detected: " + duplicates);

		}

	}
	private Map<String, Field> m_fieldMap = new HashMap<String, Field>();

	/**
	 * Convert the repository to json
	 *
	 * @return
	 */
	public JSONObject toJSON() {

		try {
			return new JSONObject(JSONUtils.objectToString(this, false));
		} catch (JSONException e) {
			throw new InvalidDataException("Error serializing field repository as JSON", e);
		}

	}

	@Override
	public boolean hasField(String name) {
		return m_fieldMap.containsKey(name);
	}

	/**
	 * @return All fields defined
	 */
	public Field[] getFields() { return m_fields; }
	public void setFields(Field[] fields) { m_fields = fields; }
	private Field[] m_fields;

	/**
	 * @return The unity instance this field repository belongs to
	 */
	@JsonIgnore
	public UnityInstance getUnity() { return unity; }
	private UnityInstance unity;

	public synchronized Field getCountField() {

		if (countField == null) {
			countField = new Field(COUNT_FIELD.getName(), COUNT_FIELD.getType());
			countField.setUnity(unity);
		}

		return countField;

	}
	private Field countField;

}
