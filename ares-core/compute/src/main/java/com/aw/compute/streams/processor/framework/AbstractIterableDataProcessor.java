package com.aw.compute.streams.processor.framework;

import com.aw.common.processor.IterableProcessor;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.spark.StreamDef;
import com.aw.compute.inject.Dependent;
import com.aw.compute.referencedata.GenericLookupData;
import com.aw.compute.referencedata.GenericLookupDataMgr;
import com.aw.compute.streams.exceptions.DataProcessingException;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.*;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes kafka queue as unity data.
 *
 *
 */
public abstract class AbstractIterableDataProcessor implements IterableProcessor, TenantAware, Dependent {

	private static final long serialVersionUID = 1L;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final String REF_DB = "ref_db";

	//TODO: DBDef object?
	protected Map<String, Object> configData;

	public AbstractIterableDataProcessor() {

	}

	/**
	 * Convert incoming json to platform data using unity
	 *
	 * @param messages
	 * @return
	 * @throws Exception
	 */
	protected List<Data> toData(Iterable<String> messages) throws Exception {

		List<Data> ret = new ArrayList<Data>();

		DataType dt = null;
		//get the unity data for this json so we can get the ID
		for (String json : messages) {

			JSONObject jsonData = new JSONObject(json);

			//get a data type for the  message -- TODO: do we need to get for all (we do if we support mixed types in an iterable
			//TODO: would be better performance if we guaranteed one type

			Data tmp = getDependency(UnityInstance.class).newData(json);
			dt = tmp.getType();


			resolveReferences(jsonData, dt);
			Data cur = getDependency(UnityInstance.class).newData(jsonData);

			ret.add(cur);

		}

		return ret;

	}

	//add reference fields to JSON
	private void resolveReferences(JSONObject jsonObject, DataType dataType) throws Exception {

		for (Field f : dataType.getFields()) {
			if (f.getReference() != null) {
				resolveReference(f, jsonObject);
			}

		}
	}

	private void resolveReference(Field f, JSONObject json) throws Exception{

		Reference ref = f.getReference();
		String localKey = json.getString(ref.getLocal_key());
		GenericLookupDataMgr ld = getDependency(GenericLookupDataMgr.class);
		GenericLookupData gld = ld.getLookup(ref.getReference_type(), getRefDBConfig());

		logger.error(" DEBUG: resolving reference ");
		//get the lookup value
		String refValue = gld.get(json.getString(ref.getLocal_key()));

		//add to JSON
		json.put(f.getName(), refValue);


	}

	public void init(StreamDef streamDef) throws ProcessorInitializationException {
		try {
			this.configData = streamDef.getConfigData();
		}
		catch (Exception ex) {
			throw new ProcessorInitializationException(ex.getMessage(), ex);
		}
	}

	protected Map<String, String> getRefDBConfig() {

		return (Map<String, String>) configData.get(REF_DB);

	}


}
