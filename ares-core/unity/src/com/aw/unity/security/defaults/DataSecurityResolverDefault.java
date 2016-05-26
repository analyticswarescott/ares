package com.aw.unity.security.defaults;

import java.util.List;

import org.apache.log4j.Logger;

import com.aw.unity.query.FilterGroup;
import com.aw.unity.security.DataSecurityResolverBase;

/**
 * Default data security resolver, returns empty filters for roles
 *
 *
 */
public class DataSecurityResolverDefault extends DataSecurityResolverBase {

	static Logger logger = Logger.getLogger(DataSecurityResolverDefault.class);

	public DataSecurityResolverDefault() {
	}

	@Override
	public FilterGroup getFilterForRoles(List<String> keys) {
		logger.warn("Default data security resolver being used, no security being applied for role(s)");
		return new FilterGroup(); //empty filter by default
	}

}
