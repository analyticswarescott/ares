package com.aw.unity.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.AbstractTaggable;
import com.aw.common.Tag;
import com.aw.common.exceptions.InvalidRequestException;
import com.aw.common.util.JSONUtils;
import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.Field;
import com.aw.unity.FieldRepository;
import com.aw.unity.FieldType;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.FieldNotFoundException;
import com.aw.unity.exceptions.InvalidDataException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data type defined in JSON. Contains additional information over what a standard JSON schema
 * would contain, including specific primitive types for ip address, mac address, etc.
 *
 * Example format (TODO: display_name likely resource bundle ID):
 *
 * <code>
 * {
 * 		"name" : "network_connections",
 *      "display_name" : "<resource id> or One source and many destinations",
 *      "description" : "<resource id> or string literal"
 *      "filter" : {
 *      	"ot" : blah blah equals 3
 *      }
 *      "field_refs" : [
 *      	{
 *      		"name" : "source",
 *      		"path" : [ "path", "to", "source" ]
 *          },
 *          {
 *          	"name" : "destination",
 *          	"path" : [ "path", "to", "destination" ]
 *          }
 *      ]
 *
 * }
 * </code>
 *
 *
 *
 */
public class JSONDataType extends AbstractTaggable implements DataType {

	private static final String ID_FIELD = "id_field";
	private static final String TAGS_FIELD = "tags";

	public static JSONDataType fromString(DataTypeRepository repo, String data, FieldRepository repository) throws Exception {

		//read the json data in itself
		JSONDataType ret = JSONUtils.objectFromString(data, JSONDataType.class);
		ret.setDataTypeRepository(repo);

		//now initialize
		ret.initialize(repository, new JSONObject(data));

		return ret;

	}

	public static JSONDataType fromCollection(String name, String desc, Field idField, Collection<JSONFieldRef> refs) {

		JSONDataType ret = new JSONDataType();

		ret.m_name = name;
		ret.m_description = desc;
		ret.m_fieldRefs = refs.toArray(new JSONFieldRef[refs.size()]);
		ret.m_idField = idField;

		initRefs(ret.m_fieldRefs);

		if (ret.m_fieldRefs == null || ret.m_fieldRefs.length == 0) {
			throw new InvalidDataException("data type field array cannot be null");
		}
		if (ret.m_name == null) {
			throw new InvalidDataException("data type name cannot be null");
		}

		if (ret.m_description == null) {
			throw new InvalidDataException("data type description cannot be null");
		}

		if (ret.m_idField == null) {
			throw new InvalidDataException("data type cannot have a null id field");
		}

		return ret;

	}

	public JSONDataType() {
	}

	@Override
	public Field getField(String name) {

		Field ret = m_byName.get(name);

		if (ret == null) {
			throw new FieldNotFoundException("no such field: " + name);
		}

		return ret;

	}

	protected void initialize(FieldRepository repository, JSONObject cfg) throws JSONException, IOException {

		initRefs(m_fieldRefs);

		//initialize other data structures for performance etc
		m_fields = new Field[m_fieldRefs.length];
		for (int x=0; x<m_fieldRefs.length; x++) {

			m_fields[x] = repository.getField(m_fieldRefs[x].getName());
			if (m_fields[x] == null) {
				throw new FieldNotFoundException("Cannot find field in repository: " + m_fieldRefs[x].getName());
			}

			//if it's an object, load that type too
			if (m_fields[x].getType() == FieldType.OBJECT) {
				loadSubType(repository, cfg, m_fieldRefs[x], m_fields[x]);
			}

		}

		//id field - can be null if it is a subtype - all subtypes are identified by the root type's id field
		Object objIdField = cfg.opt(ID_FIELD);
		if (objIdField != null) {
			m_idField = repository.getField((String)objIdField);
		}

		initializeMappings();

	}

	/**
	 * Get a field given the json path array
	 *
	 * @param path The path into the json
	 * @return The field, if any
	 */
	public Field getFieldFromPath(String... path) {
		String key = null;
		if (path == null) {
			throw new InvalidRequestException("can't get field for null path");
		}

		if (path.length == 1) {
			key = path[0];
		}

		else {
			key = StringUtils.join(path, '.');
		}

		return m_fieldByAbsolutePath.get(key);
	}

	public boolean hasPath(String path) {
		return getFieldFromPath(path) != null;
	}

	private void loadSubType(FieldRepository repository, JSONObject cfg, JSONFieldRef subType, Field field) throws JSONException {

		//get the subtype from the data type repository
		String subTypeName = field.getDataType();
		DataType dataType = m_dataTypeRepository.getDataType(subTypeName);
		if (dataType == null) {
			throw new InvalidDataException("could not resolve data type, field=" + field + ": " + subTypeName);
		}
		m_subTypes.add(dataType);

	}

	protected static void initRefs(JSONFieldRef[] refs) {
		//set paths to names where they are null
		for (JSONFieldRef ref : refs) {
			if (ref.getPath() == null) {
				ref.setPath(new String[] { ref.getName() });
			}
		}
	}

	protected void initializeMappings() {

		for (int x=0; x<m_fields.length; x++) {

			Field field = m_fields[x];

			//add the field
			m_fieldSet.add(field);
			m_ordinals.put(field, x);
			m_byName.put(field.getName(), field);
			m_jsonPaths.put(field, m_fieldRefs[x].getPath());
			m_fieldByAbsolutePath.put(StringUtils.join(m_fieldRefs[x].getPath(), '.'), field);

		}

	}

	public String[] getPath(Field field) {
		return m_jsonPaths.get(field);
	}

	@Override
	public int getOrdinal(Field field) {
		if (field == null) {
			throw new InvalidDataException("Field cannot be null");
		}

		if (!hasField(field)) {
			throw new InvalidDataException("field " + field.getName() + " does not exist in type " + getName());
		}

		return m_ordinals.get(field);
	}

	@Override
	public boolean hasField(Field field) {
		return getFieldSet().contains(field);
	}

	@Override
	public boolean hasField(String name) {
		return getByName().containsKey(name);
	}

	@JsonIgnore
	public DataTypeRepository getDataTypeRepository() { return m_dataTypeRepository; }
	@JsonIgnore
	public void setDataTypeRepository(DataTypeRepository dataTypeRepository) { m_dataTypeRepository = dataTypeRepository; }
	private DataTypeRepository m_dataTypeRepository;

	@JsonIgnore
	public Field getIDField() { return m_idField; }
	@JsonIgnore
	public void setIDField(Field idField) { m_idField = idField; }
	private Field m_idField;

	@JsonIgnore
	public Field getTimeField() { return m_timeField; }
	@JsonIgnore
	public void setTimeField(Field timeField) { m_timeField = timeField; }
	private Field m_timeField;

	public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	public String getDescription() { return m_description; }
	public void setDescription(String description) { m_description = description; }
	private String m_description;

	@JsonProperty
	public String getDisplayName() { return m_displayName; }
	public void setDisplayName(String displayName) { m_displayName = displayName; }
	private String m_displayName;

	public JSONFieldRef[] getFieldRefs() { return m_fieldRefs; }
	public void setFieldRefs(JSONFieldRef[] fieldRefs) { m_fieldRefs = fieldRefs; }
	private JSONFieldRef[] m_fieldRefs;

	@JsonIgnore
	public Field[] getFields() { return m_fields; }
	@JsonIgnore
	public void setFields(Field[] fields) { m_fields = fields; }
	@JsonIgnore
	private Field[] m_fields;

	@JsonIgnore
	public Map<String, Field> getFieldByAbsolutePath() { return m_fieldByAbsolutePath; }
	public void setFieldByAbsolutePath(Map<String, Field> fieldByAbsolutePath) { m_fieldByAbsolutePath = fieldByAbsolutePath; }
	private Map<String, Field> m_fieldByAbsolutePath = new HashMap<String, Field>();

	/**
	 * @return Sub types of this data type. Sub types would appear as an array of one or more objects within this parent data type.
	 */
	public List<DataType> getSubTypes() { return m_subTypes; }
	public void setSubTypes(List<DataType> subTypes) { m_subTypes = subTypes; }
	private List<DataType> m_subTypes = new ArrayList<DataType>();

	/**
	 * @return The name of the data source this type is related to
	 */
	public String getDataSource() { return m_dataSource; }
	public void setDataSource(String dataSource) { m_dataSource = dataSource; }
	private String m_dataSource;

	/**
	 * @return
	 */
	@JsonIgnore
	public Map<Field, Integer> getOrdinals() { return m_ordinals; }
	@JsonIgnore
	public void setOrdinals(Map<Field, Integer> ordinals) { m_ordinals = ordinals; }
	@JsonIgnore
	private Map<Field, Integer> m_ordinals = new HashMap<Field, Integer>();

	/**
	 * @return
	 */
	@JsonIgnore
	public Set<Field> getFieldSet() { return m_fieldSet; }
	@JsonIgnore
	public void setFieldSet(Set<Field> fieldSet) { m_fieldSet = fieldSet; }
	@JsonIgnore
	private Set<Field> m_fieldSet = new HashSet<Field>();

	/**
	 * @return The JSON path defined per field
	 */
	@JsonIgnore
	public Map<Field, String[]> getJsonPaths() { return m_jsonPaths; }
	@JsonIgnore
	public void setJsonPaths(Map<Field, String[]> jsonPaths) { m_jsonPaths = jsonPaths; }
	@JsonIgnore
	private Map<Field, String[]> m_jsonPaths = new HashMap<Field, String[]>();

	/**
	 * @return Fields cached by string name
	 */
	@JsonIgnore
	private Map<String, Field> getByName() { return m_byName; }
	@JsonIgnore
	private void setByName(Map<String, Field> byName) { m_byName = byName; }
	@JsonIgnore
	private Map<String, Field> m_byName = new HashMap<String, Field>();

	//the parent data type only known on the server
	private JSONDataType m_parentType;

	@JsonIgnore
	public UnityInstance getUnity() { return this.unity;  }
	void setUnity(UnityInstance unity) { this.unity = unity; }
	private UnityInstance unity;

	@JsonProperty("tags") //todo: dg_tags ? these aren't technically unity data, which is where dg_tags applies, so calling this tags for now
	@Override
	public Set<Tag> getTags() {
		return super.getTags();
	}
}


