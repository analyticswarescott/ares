package com.aw.unity.json;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.unity.Field;
import com.aw.unity.FilterFactory;
import com.aw.unity.UnityMetadata;
import com.aw.unity.exceptions.FieldNotFoundException;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.FilterGroup.GroupOperator;

/**
 * Reads a filter to JSON. Due to the level of custom deserialization logic,
 * I felt it wasn't worth the hassle to customize jackson to do this. It's actually
 * simpler just to do it manually in this case. In other cases where the conversion from
 * JSON is more straightforward, we still want to use Jackson serialization.
 *
 *
 */
public class JSONFilterFactory implements FilterFactory {

	static final String OPERATOR = "operator";
	static final String FILTER = "filter";
	static final String ENABLED = "enabled";
	static final String VALUES = "values";
	static final String FIELD = "field";
	static final String NOT = "not";

	public JSONFilterFactory(UnityMetadata meta) {
		m_metadata = meta;
	}

	protected FilterConstraint newConstraint(ConstraintOperator operator, JSONObject data) throws Exception {

		return parse(operator, data);

	}

	/**
	 * Build a filter from the json. The data must either be a JSONObject OR be able to express itself
	 * as json with its toString().
	 *
	 * @param json
	 * @return
	 */
	protected FilterGroup newFilterGroup(GroupOperator operator, JSONObject data) throws Exception {

		//parse the filter
		FilterGroup filter = parse(operator, data);
		return filter;

	}

	@Override
	public Filter newFilter(Object data) throws Exception {
		return newFilter(data, false);
	}

	/**
	 *
	 * @param data The filter to parse
	 * @param emptyGroupNull Whether empty groups should return a null filter - if false, an empty group will be returned
	 * @return The filter
	 * @throws Exception If anything goes wrong
	 */
	private Filter newFilter(Object data, boolean emptyGroupNull) throws Exception {

		JSONObject json = null;
		if (data instanceof JSONObject) {
			json = (JSONObject)data;
		}

		else {

			//handle empty or missing filter - return an empty UFilter in this case for now (matches all)
			if (data == null || data.toString().length() == 0) {
				return new FilterGroup();
			}

			//else it must be a json object
			json = new JSONObject(data.toString());

		}

		//determine what to return based on the operator
		String strOperator = json.optString(OPERATOR, null);

		//nothing, make it match all
		if (strOperator == null) {
			return emptyGroupNull ? null : new FilterGroup();
		}

		//try group operator first
		GroupOperator groupOperator = GroupOperator.fromString(strOperator);
		if (groupOperator == null) {

			//not a group, try constraint
			ConstraintOperator constraintOperator = ConstraintOperator.fromString(strOperator);

			//if not a constraint operator either, that's an error
			if (constraintOperator == null) {
				throw new InvalidDataException("Unrecognized operator: " + strOperator);
			}

			//else it's a constraint
			else {
				return newConstraint(constraintOperator, json);
			}

		}

		//else it's a group
		else {
			return newFilterGroup(groupOperator, json);
		}

	}

	private FilterGroup parse(GroupOperator operator, JSONObject data) throws Exception {

		//our return group filter
		FilterGroup filter = new FilterGroup();

		//set not logic
		filter.setNot(data.optBoolean(NOT));

		//parse the filter attributes
		filter.setOperator(operator);
		filter.setEnabled(data.optBoolean(ENABLED, true));

		//get the filter array
		JSONArray filters = data.getJSONArray(FILTER);
		if (filters == null) {
			throw new InvalidDataException("filter key cannot be missing from a filter group");
		}

		//load the children of this filter
		for (int x=0; x<filters.length(); x++) {
			JSONObject child = filters.getJSONObject(x);
			Filter childFilter = newFilter(child, true);
			if (childFilter != null) {
				filter.getFilters().add(childFilter);
			}
		}

		return filter;

	}

	private FilterConstraint parse(ConstraintOperator operator, JSONObject data) throws Exception {

		if (operator == null) {
			throw new InvalidFilterException("invalid constraint operator: " + operator);
		}

		//create the constraint
		FilterConstraint ret = operator.newConstraint();

		//set the field
		String strField = data.getString(FIELD);
		Field field = m_metadata.getFieldRepository().getField(strField);
		ret.setField(field);

		if (field == null) {
			throw new FieldNotFoundException("field not found in repository: " + data.getString(FIELD));
		}

		//get the values into a list of parsed objects
		JSONArray values = data.getJSONArray(VALUES);
		List<String> valueList = new ArrayList<String>();
		for (int x=0; x<values.length(); x++) {
			valueList.add(values.get(x) == null ? null : values.get(x).toString());
		}
		ret.setValues(field.getType().getValueParser().parse(operator, valueList));

		//set not logic
		ret.setNot(data.optBoolean(NOT, false));

		return ret;

	}

	public UnityMetadata getMetadata() { return m_metadata; }
	public void setMetadata(UnityMetadata meta) { m_metadata = meta; }
	private UnityMetadata m_metadata;

}
