package com.aw.tenant;

import com.aw.common.tenant.Tenant;
import com.aw.platform.NodeRole;

/**
 * provisions a role for a tenant
 *
 *
 *
 */
public interface Provisioner {

	/**
	 * provision resources for the given tenant
	 *
	 * @param tenant
	 * @throws ProvisioningException
	 * @returns the provisioner
	 */
	public Provisioner provision(Tenant tenant, TenantMgr tenantMgr) throws Exception;

	/**
	 * unprovision resources for the given tenant
	 *
	 * @param tenant
	 * @throws ProvisioningException
	 * @returns the provisioner
	 */
	public Provisioner unprovision(Tenant tenant, TenantMgr tenantMgr) throws Exception;

	/**
	 * @return the role being provisioned for the tenant
	 */
	public NodeRole getRole();

}
