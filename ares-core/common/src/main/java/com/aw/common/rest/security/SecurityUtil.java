package com.aw.common.rest.security;

import org.apache.log4j.Logger;

import com.aw.common.rest.Localizer;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;

/**
 * Created by scott on 18/11/15.
 */
public final class SecurityUtil {

	private static final Logger logger = Logger.getLogger(SecurityUtil.class);

	// Never needs to be instantiated
	private SecurityUtil() {
	}

	private static final AuthenticatedUser SYSTEM_USER = new AuthenticatedUser(
		Tenant.SYSTEM_TENANT_ID,
		Tenant.SYSTEM_TENANT_USER_NAME,
		Tenant.SYSTEM_TENANT_UID,
		Tenant.SYSTEM_TENANT_LOCALE
	);

	private static final PlatformSecurityContext SYSTEM_CONTEXT = new SystemSecurityContext(SYSTEM_USER);

    public static void setThreadSystemAccess() {

    	if (ThreadLocalStore.get() == null || ThreadLocalStore.get().getUser() == null || !ThreadLocalStore.get().getUser().isImpersonated()) {
				ThreadLocalStore.set(SYSTEM_CONTEXT);
    	}

    }

    public static void unsetThreadSystemAccess() {

		ThreadLocalStore.unset();

    }

    /**
	 * Extends the default security and gives system-level access, which has every role
	 */
	private static final class SystemSecurityContext extends DefaultSecurityContext {

		public SystemSecurityContext(AuthenticatedUser authenticatedUser) {
			super(authenticatedUser);
		}

		@Override
		public boolean isUserInRole(String role) {
			// System user has global access
			return true;
		}
	}

	public static void verifySystemAccess() throws Exception {
		if (!getTenantID().equals("0")) {
			throw new Exception( " Impersonation only allowed if established as system tenant (0) : current tenant is " + getTenantID() + " for thread " + Thread.currentThread().getName());
		}
	}

	//Tenant file readers and writers
	//TODO: add secured system versions initialized with a PathResolverSystem
	static String getTenantID()  {
		PlatformSecurityContext securityContext = getSecurityContext();

        String tenantID  = null;
        if (securityContext == null) {
        	//default to Tenant 0
        	tenantID = "0";
        	//tenantID = EnvironmentSettings.getGauntletTenantId();
        }
        else {
        	tenantID = securityContext.getTenantID();
        }

        if (tenantID == null) {
        	throw new RuntimeException (" Security error: no Tenant ID found for thread " + Thread.currentThread().getName());
        }

        return tenantID;
	}

	static PlatformSecurityContext getSecurityContext() {

		if (ThreadLocalStore.get() == null) {
			if (EnvironmentSettings.isAuthDisabled()) {
				logger.warn("Bypassing security because DISABLE_AUTH_VALIDATION is set to 'true'!  -- tenant ID will be '0'  ");

				AuthenticatedUser u = new AuthenticatedUser("exampleID", "example", "0", Localizer.LOCALE_EN_US, null, null, null);

				DefaultSecurityContext sec = new DefaultSecurityContext(u);
				ThreadLocalStore.set(sec);
				return sec;
			} else {
				throw new RuntimeException(" Security error : Auth enabled and no context set for thread " + Thread.currentThread().getName() );
			}
		}
		return ThreadLocalStore.get();
	}


}
