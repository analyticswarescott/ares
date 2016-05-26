package com.aw.tenant;

import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.platform.NodeRole;
import org.apache.log4j.Logger;

/**
 * provisions kafka
 *
 *
 *
 */
public class ZookeeperProvisioner implements Provisioner {

	private static final Logger LOGGER = Logger.getLogger(ZookeeperProvisioner.class);

	@Override
	public Provisioner provision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

		LOGGER.debug(" provisioning zookeeper for tenant " + tenant.getTenantID());

		DefaultZkAccessor zk = tenantMgr.getRestCluster().getTenantZkAccessor();
		zk.createTenantRoot(tenant.getTenantID());

        LOGGER.debug (" completed provisioning of zookeeper for tenant " + tenant.getTenantID());

        return this;

	}

	@Override
	public Provisioner unprovision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

        LOGGER.info("un-provisioning zookeeper for tenant " + tenant.getTenantID());

		DefaultZkAccessor zk = tenantMgr.getRestCluster().getTenantZkAccessor();


		//shut down op sequencers for this tenant
		tenantMgr.getRestCluster().getOpSequencer().closeCurrentTenant();

		zk.deleteTenantRoot(tenant.getTenantID());

		LOGGER.info("completed un-provisioning of zookeeper for tenant " + tenant.getTenantID());

        return this;

	}

	@Override
	public NodeRole getRole() {
		return NodeRole.KAFKA;
	}

}
