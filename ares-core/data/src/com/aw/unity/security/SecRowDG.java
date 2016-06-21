package com.aw.unity.security;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.system.EnvironmentSettings;
import com.aw.unity.query.Filter;
import com.aw.unity.query.Query;

/**
 *
 *
 */
public class SecRowDG implements ISecRow {

	@Override
	public  Query enforceRowSecurity(Query query, DataSecurityResolverBase resolver)
			throws Exception {

		if (EnvironmentSettings.isAuthDisabled()) {
			return query;
		}

		AuthenticatedUser user = getSecurityContext().getUser();

		//the roles array will contain the group key to lookup the rows to be excluded
		if(user!=null && !user.getRoles().isEmpty()){

			//apply a filter if there is one defined for these roles
			Filter roleFilter = resolver.getFilterForRoles(user.getRoles());
			if (roleFilter != null) {
				query = query.add(roleFilter);
			}

		}
		return query;
	}

}
