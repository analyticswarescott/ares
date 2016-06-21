package com.aw.unity;

import com.aw.unity.query.Query;
import com.aw.unity.security.DataSecurityResolverBase;
import com.aw.unity.security.ISecRow;

public class TestSecRow implements ISecRow {

	@Override
	public Query enforceRowSecurity(Query query, DataSecurityResolverBase resolver)
			throws Exception {
		return query;
	}

}
