package com.aw.unity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aw.common.util.VersionedObject;
import com.aw.unity.UnityRunner.ResponseFormat;
import com.aw.unity.UnityRunner.State;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.exceptions.QueryExecutionException;
import com.aw.unity.query.Filter;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.QueryAttribute.Order;
import com.aw.unity.query.constraints.EqConstraint;

/**
 * Unity utilities convenience class
 *
 *
 *
 */
public class UnityUtil {

	static Logger logger = Logger.getLogger(UnityUtil.class);

	/**
	 * Get all unity query attributes for the given data type, intended for a detail query (Order == NONE,
	 * Aggregate == NONE)
	 *
	 * @param dataType
	 * @return
	 */
	public static QueryAttribute[] getQueryFieldsFor(DataType dataType) {

		//build a list of the attributes
		List<QueryAttribute> attribs = new ArrayList<QueryAttribute>();

    	for (Field field : dataType.getFields()) {

    		//no aggregates or ordering, this would be for a detail query
    		QueryAttribute attrib = new QueryAttribute(field, Order.NONE, Aggregate.NONE);
    		attribs.add(attrib);

    	}

    	//return an array of the fields
    	return attribs.toArray(new QueryAttribute[attribs.size()]);

	}

	/**
	 * Build a detail query selecting all fields for this data type
	 *
	 * @param dataType The data type whose fields will be queried
	 * @return The query for this data type with an empty filter
	 */
	public static Query getDetailQueryFor(DataType dataType, Filter filter) {

    	Query query = new Query();

    	//empty attribute array for a detail query == get all fields, but must have a type
    	query.setAttributes(new QueryAttribute[0]);
		query.setAllAttributes(true);

    	//this is a detail query
    	query.setDetail(true);

    	//not count only
    	query.setCountOnly(false);

    	//the data type
    	if (dataType != null) {
    		query.setDataTypes(dataType);
    	}

    	//set the filter
    	query.setFilter(filter);

    	return query;

	}

	/**
	 * Return a single pojo (plain old java object) given a unity type and a guid.
	 *
	 * @param type
	 * @param timeout Max time to wait in millis, 0 if no maximum
	 * @param id
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static <T> VersionedObject<T> getPojo(UnityInstance unity, DataType type, long timeout, String guid, Class<T> pojoType) throws Exception {

		//filter to get just the object with this guid
		Filter filter = new EqConstraint(type.getIDField(), guid);

		//build the query
		Query query = getDetailQueryFor(type, filter);

		//only get one row back
		query.setSize(1);

		PojoUnityResults<T> results = executeBlocking(unity, query, timeout, pojoType);

		//return the single pojo, or null if nothing was returned in the query
		VersionedObject<T> ret = null;
		if (results.getPojos().size() > 0) {
			return results.getPojos().get(0);
		}
		return ret;

	}

	private static UnityRunner executeBlocking(UnityInstance unity, Query query, long timeout) throws Exception {

		//execute remotely - this shouldn't be needed, but elasticsearch's client apis can't currently run within
		//spark due to a jackson library dependency issue

		UnityRunner runner = unity.execute(query);

    	synchronized (runner) {

    		long start = System.currentTimeMillis();
    		while (runner.getState() != State.COMPLETE) {

    			//unity will notify all when complete
    			runner.wait(timeout);

    			//if there is an error, throw an exception
    			if (runner.getState() == State.ERROR) {
    				throw new Exception("query execution failed: " + runner.getErrorMessage());
    			}

    			//if timeout was exceeded, throw an exception
    			else if (timeout > 0L && System.currentTimeMillis() - start >= timeout) {
    				throw new QueryExecutionException("query timed out");
    			}

    		}

    	}

    	return runner;

	}

	public static <T extends UnityResults> T executeBlocking(UnityInstance unity, Query query, long timeout, ResponseFormat format) throws Exception {

    	return executeBlocking(unity, query, timeout).getResults(format);

	}

	public static <T> PojoUnityResults<T> executeBlocking(UnityInstance unity, Query query, long timeout, Class<T> pojoType) throws Exception {

    	return executeBlocking(unity, query, timeout).getResults(pojoType);

	}

}
