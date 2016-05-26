package com.aw.common.rest.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Anything that needs to know the current tenant should implement this interface
 *
 *
 *
 */
public interface TenantAware {

	/**
	 * @return The tenant id associated with the current thread
	 */
	@JsonIgnore
	default public String getTenantID() { return ThreadLocalStore.get() == null ? null : ThreadLocalStore.get().getTenantID(); }
}
