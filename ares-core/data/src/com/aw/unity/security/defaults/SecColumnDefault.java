package com.aw.unity.security.defaults;

import com.aw.unity.query.Query;
import com.aw.unity.security.ISecColumn;

/**
 * Default column security, does nothing for now.
 *
 *
 */
public class SecColumnDefault implements ISecColumn {

	@Override
	public Query enforceColumnSecurity(Query query) throws Exception {
		return query;
	}

}
