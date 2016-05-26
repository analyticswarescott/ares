package com.aw.unity;

import com.aw.unity.query.Filter;

/**
 * A filter factory can build filter objects from input streams or data objects. The type of data
 * required is dependent on the type of factory.
 *
 *
 *
 */
public interface FilterFactory {

	/**
	 * Build a filter from the raw data
	 *
	 * @param data The raw filter data
	 * @return The filter
	 * @throws Exception If anything goes wrong
	 */
	public Filter newFilter(Object data) throws Exception;

}
