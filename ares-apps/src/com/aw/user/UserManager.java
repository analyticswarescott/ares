package com.aw.user;

import com.aw.common.auth.User;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.security.Role;

import java.util.Collection;

/**
 * @author jhaight
 */
public interface UserManager extends SecurityAware {

	/**
	 * Returns the list of roles for the currently
	 * @param userId The id of the user
	 * @return The list of roles available to the specified user
     */
	Collection<Role> getRoles(String userId) throws Exception;

	/**
	 * Returns a list of tenants available to the specified user
	 * @param userId The id of the user
	 * @return The list of tenants available to the specified user
	 * @throws UnsupportedOperationException If tenant ID is not 0 (only works for DGMC)
     */
	Collection<Tenant> getTenants(String userId) throws Exception;

	/**
	 * Get the users for the current tenant
	 * @return A list of users specific to the current tenant
     */
	Collection<User> getUsers();
}
