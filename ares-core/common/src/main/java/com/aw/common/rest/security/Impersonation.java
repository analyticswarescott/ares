package com.aw.common.rest.security;

import com.aw.common.rest.AdminMgr;
import com.aw.common.tenant.Tenant;

public class Impersonation {

	private static ThreadLocal<PlatformSecurityContext> s_impersonationLocals = new ThreadLocal<>();

	public static boolean impersonateTenant(Tenant tenant) throws Exception {
		return impersonateTenant(tenant.getTenantID(), Tenant.SYSTEM_TENANT_UID);
	}
	/**
	 * Impersonate as a tenant.
	 *
	 * @param tenantID The tenant ID to impersonate as
	 * @return Whether the impersonation was needed - if false, unimpersonate should NOT be called
	 * @throws Exception If anything goes wrong
	 */
	public static boolean impersonateTenant(String tenantID) throws Exception {
		return impersonateTenant(tenantID, Tenant.SYSTEM_TENANT_UID);
	}
	public static boolean impersonateTenant(String tenantID, String username) throws Exception {

		//TODO: secure system access
		PlatformSecurityContext ctx = ThreadLocalStore.get();

		//if we are already this tenant, just return
		if (ctx != null && //if we already have a context
				(ctx.getTenantID() == tenantID || //and it's the same string instance (check same instance first, it's quickest)
				 ctx.getTenantID().equals(tenantID)) //or it's the same tenant ID, different strings (last resort check)
			) {
			return false; //let the caller know we didn't need to impersonate
		}

		if (!SecurityUtil.getTenantID().equals("0") && !ctx.getUser().isImpersonated()) {
			throw new Exception( " Impersonation only allowed if established as system tenant (0) ");
		}

		//only create new when we have to for this thread - this may be called many times per second
		PlatformSecurityContext impersonation = s_impersonationLocals.get();
		if (impersonation == null) {
			s_impersonationLocals.set(new DefaultSecurityContext(new AuthenticatedUser(tenantID, "system", "aw", "en-us")));
			impersonation = s_impersonationLocals.get();
		}

		//set the tenant and impersonation flag
		final AuthenticatedUser impersonatedUser = impersonation.getUser();
		impersonatedUser.setTenant(tenantID);
		impersonatedUser.setImpersonated(true);
		impersonatedUser.setId(username);
		impersonatedUser.setName(username);

		//set this as our context
		ThreadLocalStore.set(impersonation);

		return true;

	}

	/**
	 * Restore system tenant
	 *
	 * @throws Exception
	 */
	public static void unImpersonate() throws Exception {

		final PlatformSecurityContext sec = ThreadLocalStore.get();

		if (sec != null && sec.getTenantID().equals("0")) {
			return; //we're already admin
		}

		if (!sec.getTenantID().equals("0") && !sec.getUser().isImpersonated()) {
			throw new Exception( " tenant not impersonated, cannot return to system access ");
		}

		DefaultSecurityContext sec2 = new DefaultSecurityContext(AdminMgr.SYSTEM_USER);
		ThreadLocalStore.set(sec2);

	}

}
