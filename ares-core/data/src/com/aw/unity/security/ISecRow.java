package com.aw.unity.security;

import com.aw.common.rest.security.SecurityAware;
import com.aw.unity.query.Query;

public interface ISecRow extends SecurityAware {


	/**
	 * Append row-level security predicates as Unity filter groups
	 * @param query
	 * @param secDef
	 * @return
	 * @throws Exception
	 */
	public Query enforceRowSecurity(Query query, DataSecurityResolverBase resolver) throws Exception;


}
