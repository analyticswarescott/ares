package com.aw.common.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;

/*
 *
 * public interfaces for UnityServer
 */
public class AdminMgr {

	public static final Logger logger = LoggerFactory.getLogger(AdminMgr.class);

	public static final AuthenticatedUser SYSTEM_USER = new AuthenticatedUser(Tenant.SYSTEM_TENANT_ID, Tenant.SYSTEM_TENANT_USER_NAME, Tenant.SYSTEM_TENANT_UID, Tenant.SYSTEM_TENANT_LOCALE);

	public static String getConfDir() {
		return EnvironmentSettings.getConfDirectory();
	}

}


