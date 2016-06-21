package com.aw.unity.security;

import com.aw.common.rest.security.SecurityAware;
import com.aw.unity.query.Query;

public interface ISecColumn extends SecurityAware {

	/**
	 * Make runtime query modifications to enforce column-level security
	 * @param query
	 * @param secDef
	 * @return
	 * @throws Exception
	 */
	public Query enforceColumnSecurity(Query query) throws Exception;

}
