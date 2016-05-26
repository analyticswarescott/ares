package com.aw.unity.json;

import org.codehaus.jettison.json.JSONObject;

import com.aw.unity.DataTypeRepository;
import com.aw.unity.DataTypeResolver;

/**
 * A data type resolver defined in JSON.
 *
 *
 *
 */
public interface JSONDataTypeResolver extends DataTypeResolver {

	/**
	 * Initialize from the json config
	 *
	 * @param cfg
	 */
	public void initialize(DataTypeRepository repo, JSONObject cfg) throws Exception;

}
