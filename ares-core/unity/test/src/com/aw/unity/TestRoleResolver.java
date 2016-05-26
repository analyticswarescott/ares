package com.aw.unity;

import java.util.List;

import com.aw.unity.query.FilterGroup;
import com.aw.unity.security.DataSecurityResolverBase;

public class TestRoleResolver extends DataSecurityResolverBase {

	@Override
	public FilterGroup getFilterForRoles(List<String> keys) {
		return new FilterGroup();
	}

}
