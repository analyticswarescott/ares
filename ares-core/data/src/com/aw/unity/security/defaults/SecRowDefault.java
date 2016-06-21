package com.aw.unity.security.defaults;

import com.aw.unity.query.Query;
import com.aw.unity.security.DataSecurityResolverBase;
import com.aw.unity.security.ISecRow;

/**
 * Default row security - does nothing for now.
 *
 *
 */
public class SecRowDefault implements ISecRow {

	@Override
	public Query enforceRowSecurity(Query query, DataSecurityResolverBase resolver)
			throws Exception {
		return query;
	}

}
