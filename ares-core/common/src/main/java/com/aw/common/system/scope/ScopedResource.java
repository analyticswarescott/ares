package com.aw.common.system.scope;

import com.aw.common.tenant.Tenant;

/**
 * Information on the scope of a resource
 */
public interface ScopedResource {

	public ResourceScope getScope();

	public boolean isInScope(Tenant tenant);
}
