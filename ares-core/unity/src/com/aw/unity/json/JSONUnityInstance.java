package com.aw.unity.json;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.inject.Provider;

import com.aw.unity.*;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.common.util.Utils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.exceptions.QueryExecutionException;
import com.aw.unity.query.Filter;
import com.aw.unity.query.Query;
import com.fasterxml.jackson.databind.Module;

/**
 * Create a unity instance from json.
 *
 *
 *
 */
public class JSONUnityInstance implements UnityInstance, UnityMetadata {

	public static final String DATA_SOURCES = "data_sources";
	public static final String TYPE = "type";
	public static final String DATA_TYPE_RESOLVER = "data_type_resolver";
	public static final String DEFAULT_DATA_SOURCE = "default_data_source";
	public static final String DEFAULT_TIME_FIELD = "default_time_field";

	static Logger logger = Logger.getLogger(JSONUnityInstance.class);

	private Provider<Platform> platform;
	private Module[] modules;

	/**
	 * construct a json unity instance with the given json modules
	 *
	 * @param modules any custom json modules used for serialization
	 */
	public JSONUnityInstance(Module... modules) {
		this.modules = modules;
	}

	/**
	 * @param data Either a JSONObject instance, or its toString() must return a valid json representation of the instance
	 */
	@Override
	public void initialize(Object data, DocumentHandler docs, Provider<Platform> platform) {

		this.platform = platform;

		//initialize from json
		JSONObject json = null;
		if (!(data instanceof JSONObject)) {
			try {
				json = new JSONObject(String.valueOf(data));
			} catch (Exception e) {
				throw new InvalidDataException("Error parsing unity instance json", e);
			}
		}

		else {
			json = (JSONObject)data;
		}

		try {

			//set some defaults
			m_defaultTimeField = json.optString(DEFAULT_TIME_FIELD);

			//load fields, data types, etc using the document handler
			initializeFieldRepository(docs);
			initializeDataTypes(docs);

			//load data sources from the instance config
			initializeDataSources(json);

			//create the filter factory
			m_filterFactory = new JSONFilterFactory(this);

		} catch (Exception e) {
			throw new InvalidDataException("error loading unity instance", e);
		}
	}

	private void initializeDataSources(JSONObject json) throws Exception {

		//get the data sources array
		JSONArray sources = json.getJSONArray(DATA_SOURCES);
		m_dataSources = new UnityDataSource[sources.length()];

		//for each, create the data source instance and initialize it
		for (int x=0; x<sources.length(); x++) {

			JSONObject source = sources.getJSONObject(x);

			//if we have elasticsearch enabled or this source isn't elasticsearch, load it - avoids class issues during unit tests
			if (Utils.isElasticsearchEnabled() || !source.getString(TYPE).toLowerCase().contains("elastic")) {

				//create the instance and initialize it
				UnityDataSource dataSource = (UnityDataSource)Class.forName(source.getString(TYPE)).newInstance();

				//initialize data source from json
				JSONUtils.updateFromString(source.toString(), dataSource);

				dataSource.initialize(this, platform);

				//cache it
				m_dataSources[x] = dataSource;
				m_dataSourceMap.put(m_dataSources[x].getName(), m_dataSources[x]);
			}

			else {
				logger.info("not loading data sources, elasticsearch disabled");
			}

		}

		//get the default data source
		m_defaultDataSource = getDataSource(json.getString(DEFAULT_DATA_SOURCE));

	}

	private void initializeFieldRepository(DocumentHandler docs) throws Exception {

		//get all known fields
		List<Document> fields = docs.getDocumentsOfType(DocumentType.UNITY_FIELD_REPO);

		//master field set
		Set<Field> fieldSet = new TreeSet<Field>();
		for (Document cur : fields) {

			//merge each into master
			JSONFieldRepository curFields = JSONFieldRepository.fromString(this, cur.getBodyAsString());

			for (Field field : curFields.getFields()) {

				//for now enforce no duplicates
				if (!fieldSet.add(field)) {
					throw new InvalidDataException("Duplicate field found: " + field.getName());
				}

			}

		}

		//initialize the master field repository
		m_fieldRepository = JSONFieldRepository.fromCollection(this, fieldSet);

	}

	private void initializeDataTypes(DocumentHandler docs) throws Exception {

		//build the repository
		JSONDataTypeRepository repo = new JSONDataTypeRepository();
		repo.initialize(docs, this);
		m_dataTypeRepository = repo;

	}

	@Override
	public UnityRunner execute(Query query) throws QueryExecutionException {

		//make sure the query is valid
		query.validate();

		//determine the data source
		String strDataSource = query.getDataSource();

		//get the data source
		UnityDataSource dataSource = m_defaultDataSource;

		//for now default to elasticsearch
		if (strDataSource != null) {
			dataSource = getDataSource(strDataSource);
		}

		//if we couldn't find one, that's a fatal error
		if (dataSource == null) {
			throw new InvalidQueryException("Query requesting invalid data source " + strDataSource);
		}

		//else we have the data source, run the query
		UnityRunner ret = dataSource.execute(query);

		synchronized (this) {

			//cache the runner
			m_elasticRunners.put(ret.getGuid(), ret);

		}

        return ret;

	}

	@Override
	public <T> T toPojo(Data data, Class<T> type) throws Exception {

		//convert to json and build our pojo
    	String json = data.toJsonString(false, true, true);

    	//elasticsearch data source requires that unity is json aware, as elasticsearch is json based
    	T pojo = JSONUtils.objectFromString(json, type, modules);

    	return pojo;

	}

    public synchronized UnityRunner getRunner(UUID guid) {
   	 	return m_elasticRunners.get(guid);
    }

    /**
     * Most recent running elastic search queries
     * TODO: cleanup over time
     */
    @SuppressWarnings("serial")
	private Map<UUID, UnityRunner> m_elasticRunners = new LinkedHashMap<UUID, UnityRunner>(16, .75f, true) {
    	 protected boolean removeEldestEntry(Map.Entry<UUID,UnityRunner> eldest) {
    		 return size() > getMaxRunners(); //keep at most N runners
    	 }
     };

    public int getMaxRunners() { return m_maxRunners; }
    public void setMaxRunners(int maxRunners) { m_maxRunners = maxRunners; }
    private int m_maxRunners = 100;

     @Override
	public Filter newFilter(Object rawData) throws Exception {
		return m_filterFactory.newFilter(rawData);
	}

    public Data newData(Object rawData, DataType dataType) {
    	return m_dataFactory.newData(rawData, dataType, this);
    }

	@Override
	public Data newData(Object rawData) throws Exception {
		return m_dataFactory.newData(rawData, this);
	}

	public DataType getDataType(String dataType) {
		return m_dataTypeRepository.getDataType(dataType);
	}

	@Override
	public UnityDataSource getDataSource(String name) {
		return m_dataSourceMap.get(name);
	}

	public DataTypeRepository getDataTypeRepository() { return m_dataTypeRepository; }
	public void setDataTypeRepository(DataTypeRepository dataTypeRepository) { m_dataTypeRepository = dataTypeRepository; }
	private DataTypeRepository m_dataTypeRepository;

	//lookup maps
	private Map<String, UnityDataSource> m_dataSourceMap = new HashMap<String, UnityDataSource>();

	/**
	 * @return All known fields
	 */
	public FieldRepository getFieldRepository() { return m_fieldRepository; }
	private FieldRepository m_fieldRepository;


	/**
	 * @return Metadata object for this unity instance, passed in as context in various places
	 */
	public UnityMetadata getMetadata() { return this; }

	@Override
	public FilterFactory getFilterFactory() { return m_filterFactory; }
	private FilterFactory m_filterFactory;

	public UnityDataSource[] getDataSources() { return m_dataSources; }
	private UnityDataSource[] m_dataSources;

	private DataFactory m_dataFactory = new JSONDataFactory();

	private UnityDataSource m_defaultDataSource;

	public Map<DataType, String> getIndexMapping() { return m_indexMapping; }
	public void setIndexMapping(Map<DataType, String> indexMapping) { m_indexMapping = indexMapping; }
	private Map<DataType, String> m_indexMapping;

	@Override
	public String getDefaultTimeField() { return m_defaultTimeField; }
	public void setDefaultTimeField(String field) { m_defaultTimeField = field; }
	private String m_defaultTimeField;
}
