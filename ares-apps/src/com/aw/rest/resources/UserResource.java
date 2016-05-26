package com.aw.rest.resources;

import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.security.PlatformPermission;
import com.aw.security.Role;
import com.aw.user.UserManager;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author jhaight
 * TODO: When users are made, actually implement this resource
 */
@Path(com.aw.util.Statics.REST_VERSION + "/users")
public class UserResource implements SecurityAware {

	private UserManager userManager;

	@Inject
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}

	/**
	 * A stand in to get the list of users
	 * @return A list of fake users
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Users",
	notes = "Will return a fake list of users",
	response = String.class,
	responseContainer = "List")
	public Collection<String> get() {
		String tenantId = getTenantID();
		List<String> users = new ArrayList<>();
		users.add("bob-" + tenantId);
		users.add("jane-" + tenantId);
		users.add("jack-" + tenantId);
		users.add("joe-" + tenantId);
		return users;
	}

	@GET
	@Path("{user_id}/tenants")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Tenant> getTenants(@PathParam("user_id") String userId) throws Exception {
		verifyUser(userId);
		final Collection<Tenant> tenants;
		if (Tenant.SYSTEM_TENANT_ID.equals(getTenantID())) {
			tenants = userManager.getTenants(userId);
		}
		else {
			tenants =  new ArrayList<>();
			tenants.add(new Tenant(getTenantID()));
		}
		return tenants;
	}


	@GET
	@Path("{user_id}/security")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<PlatformPermission> getSecurity(@PathParam("user_id") String userId) throws Exception {
		verifyUser(userId);
		final Collection<Role> roles = userManager.getRoles(userId);
		final Set<PlatformPermission> permissions = new HashSet<>();
		for (Role role : roles) {
			permissions.addAll(role.getPermissions());
		}
		return permissions;
	}

	private void verifyUser(String userId) {
		if (userId == null) {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("User ID must be provided").build());
		} else if (!userId.equals(getUserID())) {
			throw new ForbiddenException();
		}
	}
}
