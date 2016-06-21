package com.aw.unity;

import com.aw.unity.query.Query;
import com.aw.unity.security.ISecColumn;

public class TestSecColumn implements ISecColumn {

	@Override
	public Query enforceColumnSecurity(Query query) throws Exception {
		return query;
	}

}
