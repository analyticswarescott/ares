package com.aw.unity;

import com.aw.unity.json.JSONQueryFactory;
import com.aw.unity.query.Query;

/**
 * The query factory. All queries need to be created from methods on this class.
 *
 *
 */
public class QueryFactory {

	/**
	 * Supported query formats.
	 */
	public enum QueryFormat {
		JSON(new JSONQueryFactory());

		private QueryFormat(IQueryFactory factory) {
			m_factory = factory;
		}

		IQueryFactory getFactory() { return m_factory; }
		private IQueryFactory m_factory;
	}

	/**
	 * Build a query in the provided format.
	 *
	 * @param data The data from which to build the query
	 * @param format The format of the data
	 * @return The query
	 * @throws Exception If anything goes wrong
	 */
	public static Query newQuery(Object data, QueryFormat format, UnityInstance ctx) throws Exception {
		return format.getFactory().newQuery(data, ctx);
	}

}
