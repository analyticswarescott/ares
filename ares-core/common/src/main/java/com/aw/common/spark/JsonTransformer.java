package com.aw.common.spark;

import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for transforming an incoming JSON object into one or more typed output formats
 */
public interface JsonTransformer {

	public List<JSONObject> transform(JSONObject input) throws  Exception;

}
