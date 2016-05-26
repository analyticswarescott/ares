package com.aw.unity.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityMetadata;
import com.aw.unity.exceptions.InvalidDataException;

/**
 * A data repository that is configured from JSON.
 *
 * {
 * 		"resolvers" : {
 *          "(className)" : {
 *              ... (resolver data) ...
 *          }
 *      },
 * 		"data_types" : [
 * 		]
 *
 * </pre></code>
 *
 */
public class JSONDataTypeRepository implements DataTypeRepository {

	private static final String RESOLVERS = "resolvers";
	private static final String DATA_TYPES = "types";

	private static final String DATA_TYPE_SEPARATOR = ".";

	//resolver type
	private static final String TYPE = "type";

	public JSONDataTypeRepository() {
	}

	public void initialize(DocumentHandler docs, UnityInstance instance) throws Exception {

		unity = instance;

		//get all known data types
		List<Document> dataTypeDocs = docs.getDocumentsOfType(DocumentType.UNITY_DATATYPE_REPO);

		//merge all data type repositories
		JSONObject masterRepo = JSONUtils.merge(dataTypeDocs);

		//initialize the data type array
		List<DataType> dataTypeList = toDataTypes(masterRepo.getJSONArray(DATA_TYPES), instance);
		m_dataTypes = dataTypeList.toArray(new DataType[dataTypeList.size()]);

		//initialize the resolvers
		initializeResolvers(masterRepo.getJSONArray(RESOLVERS));

	}

	private void initializeResolvers(JSONArray resolverSet) throws Exception {

		List<JSONDataTypeResolver> resolvers = new ArrayList<JSONDataTypeResolver>();
		for (int x=0; x<resolverSet.length(); x++) {
			JSONObject resolverJson = resolverSet.getJSONObject(x);

			//create the resolver
			JSONDataTypeResolver resolver = (JSONDataTypeResolver)Class.forName(resolverJson.getString(TYPE)).newInstance();

			//initialize the resolver
			resolver.initialize(this, resolverJson);

			//add the new resolver
			resolvers.add(resolver);

		}

		//set the resolvers array
		m_resolvers = resolvers.toArray(new JSONDataTypeResolver[resolvers.size()]);

	}

	private List<DataType> toDataTypes(JSONArray array, UnityInstance instance) throws Exception {

		//we need the unity metadata
		UnityMetadata meta = instance.getMetadata();

		//load the data types
		List<DataType> dataTypes = new ArrayList<DataType>();
		for (int x=0; x<array.length(); x++) {

			String rawJson = array.getJSONObject(x).toString();

			JSONDataType dataType = JSONDataType.fromString(this, rawJson, meta.getFieldRepository());
			dataType.setUnity(unity);

			//set default fields
			String strTimeField = instance.getDefaultTimeField();
			if (strTimeField != null &&
				dataType.hasField(strTimeField)) {
				dataType.setTimeField(dataType.getField(strTimeField));
			}

			//add the data type
			add(dataTypes, dataType);

		}

		return dataTypes;

	}

	/**
	 * recursively add all types and subtypes
	 *
	 * @param dataTypes The list
	 * @param dataType The root of the data type tree to add
	 */
	private void add(List<DataType> dataTypes, DataType dataType) throws Exception {

		dataTypes.add(dataType);

		if (m_dataTypeMap.containsKey(dataType.getName())) {
			throw new Exception("duplicate data type detected: " + dataType.getName());
		}

		//cache the data type
		m_dataTypeMap.put(dataType.getName(), dataType);

	}

	@Override
	public DataType resolveDataType(Object rawData) {

		for (JSONDataTypeResolver resolver : m_resolvers) {

			//try to resolve to a data type
			DataType dataType = resolver.resolve(rawData);

			//if resolved, return now
			if (dataType != null) {
				return dataType;
			}

		}

		//not resolving data is a fatal error
		throw new InvalidDataException("Could not resolve data to a data type: " + rawData);

	}

	public DataType getDataType(String name) {
		return m_dataTypeMap.get(name);
	}

	private Map<String, DataType> m_dataTypeMap = new HashMap<String, DataType>();

	public DataType[] getDataTypes() { return m_dataTypes; }
	public void setDataTypes(DataType[] dataTypes) { m_dataTypes = dataTypes; }
	private DataType[] m_dataTypes;

	public JSONDataTypeResolver[] getResolvers() { return m_resolvers; }
	public void setResolvers(JSONDataTypeResolver[] resolvers) { m_resolvers = resolvers; }
	private JSONDataTypeResolver[] m_resolvers;

	public DefaultJSONDataTypeResolver getResolver() { return m_resolver; }
	public void setResolver(DefaultJSONDataTypeResolver resolver) { m_resolver = resolver; }
	private DefaultJSONDataTypeResolver m_resolver;

	//the unity instance this data type repository belongs to
	private UnityInstance unity;

}
