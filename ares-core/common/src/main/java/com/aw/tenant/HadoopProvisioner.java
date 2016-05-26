package com.aw.tenant;

import com.aw.common.tenant.Tenant;
import com.aw.platform.NodeRole;
import org.apache.log4j.Logger;

/**
 * provisions kafka
 *
 *
 *
 */
public class HadoopProvisioner implements Provisioner {

	private static final Logger LOGGER = Logger.getLogger(HadoopProvisioner.class);

	@Override
	public Provisioner provision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

		LOGGER.debug(" provisioning hadoop for tenant " + tenant.getTenantID());

		tenantMgr.getPlatformMgr().getTenantFileWriter().createTenantRoot(tenant.getTenantID());

        LOGGER.debug (" completed provisioning of hadoop for tenant " + tenant.getTenantID());

        return this;

	}

	@Override
	public Provisioner unprovision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

        LOGGER.info("un-provisioning hadoop for tenant " + tenant.getTenantID());

		tenantMgr.getPlatformMgr().getTenantFileWriter().deleteTenantRoot(tenant.getTenantID());

        return this;

	}

	@Override
	public NodeRole getRole() {
		return NodeRole.KAFKA;
	}

}
