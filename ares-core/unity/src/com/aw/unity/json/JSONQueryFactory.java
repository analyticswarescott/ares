package com.aw.unity.json;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.unity.DataType;
import com.aw.unity.FieldRepository;
import com.aw.unity.IQueryFactory;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.QueryAttribute.Aggregate;

/**
 * A factory for queries defined in JSON. The data passed in must either be a JSONObject instance,
 * or its toString() must express the query in the proper json format.
 *
 * Example format:
 *
 * <code><pre>
{
  "display_name": "Query event_detail ",
  "description" : " desc ",
  "author": "aw",
  "body": {
    "detail": true,
    "top_n": 25,
    "test_n": 100,
    "count_only": false,
    "data_types" : [
    	"email_xyz"
    ],
    "attributes": [
      {
        "field": "event_count",
        "sort": "asc",
        "agg": "countd"
      },
      {
        "field": "un",
        "sort": "asc",
        "agg": "none"
      }
    ]
    "filter": {
	  "operator": "AND",
	  "filter": [
	    {
	      "field": "field_int",
	      "operator": "in",
	      "values": [ 1, 2, 3 ],
	      "enabled": "false"
	    }
	  ]
	}
  }
}
 * </pre></code>
 *
 *
 */
public class JSONQueryFactory implements IQueryFactory {

	static final String FILTER = "filter";
	static final String ATTRIBUTES = "attributes";
	static final String FIELD = "field";
	static final String DATA_TYPES = "data_types";

	@Override
	public Query newQuery(Object data, UnityInstance ctx) throws Exception {

		if (data == null) {
			throw new InvalidDataException("Query cannot be null");
		}

		JSONObject json = null;
		if (data instanceof JSONObject) {
			json = (JSONObject)data;
		}

		else {
			json = new JSONObject(data.toString());
		}

		return build(json, ctx);

	}

	private Query build(JSONObject data, UnityInstance unity) throws Exception {

		Query query = JSONUtils.objectFromString(data.toString(), Query.class);
		query.setUnity(unity);

		//set up filters - see if there's a filter in the json
		JSONObject jsonFilter = data.optJSONObject(FILTER);

		//match all if no filter is provided in the query
		Filter filter = jsonFilter == null ? new FilterGroup() : unity.getMetadata().getFilterFactory().newFilter(jsonFilter);
		query.setFilter(filter);

		//set attributes
		query.setAttributes(buildAttributes(data.getJSONArray(ATTRIBUTES), unity));

		//set data types
		JSONArray types = data.optJSONArray(DATA_TYPES);
		if (types != null) {
			List<DataType> dataTypes = new ArrayList<DataType>();
			for (int x=0; x<types.length(); x++) {
				String strDataType = types.getString(x);
				DataType dataType = unity.getMetadata().getDataType(strDataType);
				if (dataType == null) {
					throw new Exception("no such data type: " + strDataType);
				}
				dataTypes.add(dataType);
			}
			query.setDataTypes(dataTypes.toArray(new DataType[dataTypes.size()]));
		}

		//initialize the query
		query.initialize();

		return query;

	}

	private QueryAttribute[] buildAttributes(JSONArray attribs, UnityInstance ctx) throws Exception {

		FieldRepository fRepo = ctx.getMetadata().getFieldRepository();

		if (attribs == null) {
			throw new InvalidDataException("attributes cannot be missing in query");
		}

		QueryAttribute[] ret = new QueryAttribute[attribs.length()];
		for (int x=0; x<attribs.length(); x++) {
			JSONObject json = attribs.getJSONObject(x);
			ret[x] = JSONUtils.objectFromString(json.toString(), QueryAttribute.class);

			String strField = json.getString(FIELD);
			if (UnityInstance.COUNT_FIELD.getName().equals(strField)) {
				ret[x].setField(UnityInstance.COUNT_FIELD);
			} else {
				ret[x].setField(fRepo.getField(json.getString(FIELD)));
			}

			//make sure aggregate is non-null
			if (ret[x].getAggregate() == null) {
				ret[x].setAggregate(Aggregate.NONE);
			}

		}

		return ret;

	}


}
