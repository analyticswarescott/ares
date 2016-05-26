package com.aw.common.rest.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Anything needing security related information should implement this interface
 *
 *
 */
public interface SecurityAware extends TenantAware {

	/**
	 * @return The current user ID
	 */
	//this should probably be simplified and this logic encapsulated in the security stuff
	@JsonIgnore
	default public String getUserID() {
		PlatformSecurityContext securityContext = SecurityUtil.getSecurityContext();

		String userID  = null;
		if (securityContext == null) {
			//default to Tenant 0
			userID = "0";
			//tenantID = EnvironmentSettings.getGauntletTenantId();
		}
		else {
			userID = securityContext.getUser().getId();
		}

		if (userID == null) {
			throw new RuntimeException (" Security error: no user ID found for thread " + Thread.currentThread().getName());
		}

		return userID;
	}

	@JsonIgnore
	default public String getTenantID() { return SecurityUtil.getTenantID(); }

	/**
	 * @throws Exception if the current thread does not have system access, an exception will be thrown
	 */
	default public void verifySystemAccess() throws Exception { SecurityUtil.verifySystemAccess(); }

	@JsonIgnore
	default public PlatformSecurityContext getSecurityContext() { return SecurityUtil.getSecurityContext(); }

}
