package com.aw.unity.security;

import java.util.List;

import com.aw.unity.query.Filter;


public abstract class DataSecurityResolverBase {

	public long _cacheTTL = 0L; //default for now

	public abstract Filter getFilterForRoles(List<String> keys);

    public DataSecurityResolverBase() {
    }

    protected void setCacheTTL(long l) {
    	_cacheTTL = l;
    }

}
