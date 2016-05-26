package com.aw.unity;

import com.aw.unity.query.Query;

/**
 * A particular query factory type.
 *
 *
 *
 */
public interface IQueryFactory {

	/**
	 * Build a new query from the data.
	 *
	 * @param data The data from which to build a query
	 * @return The query built from the data
	 * @throws Exception If anything goes wrong
	 */
	public Query newQuery(Object data, UnityInstance ctx) throws Exception;

}
