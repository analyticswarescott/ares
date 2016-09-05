package com.aw.tenant;

import java.time.Instant;

import org.apache.log4j.Logger;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.es.ESClient;
import com.aw.platform.NodeRole;

/**
 * provisions elasticsearch
 *
 *
 *
 */
public class ElasticsearchProvisioner implements Provisioner {

	static final Logger LOGGER = Logger.getLogger(ElasticsearchProvisioner.class);

	@Override
	public Provisioner provision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

    	Impersonation.impersonateTenant(tenant.getTenantID());

    	try {

        	LOGGER.info("provisioning elasticsearch for tenant " + tenant.getTenantID());

        	//for each mapping, post the index to elasticsearch
        	ESClient client = new ESClient(tenantMgr.getPlatformMgr());

        	//create indexes for the current tenant
        	client.applyIndexMappings(tenantMgr.getDocs(), tenantMgr.getPlatform(), Instant.now());

        	//update system tenant to be able to see these indexes
        	client.updateSystemMappings();

            LOGGER.info("completed provisioning of elasticsearch for tenant " + tenant.getTenantID());

            return this;

    	} finally {
   			Impersonation.unImpersonate();
    	}

	}

	@Override
	public Provisioner unprovision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

        Impersonation.impersonateTenant(tenant);

        try {

            LOGGER.info("un-provisioning elasticsearch for tenant " + tenant.getTenantID());

            //for each mapping, post the index to elasticsearch
            ESClient client = new ESClient(tenantMgr.getPlatformMgr());
            client.removeIndexMappings(tenantMgr.getDocs(), tenantMgr.getPlatformMgr());

            LOGGER.info("completed un-provisioning of elasticsearch for tenant " + tenant.getTenantID());

            return this;

        } finally {
            Impersonation.unImpersonate();
        }

	}

	@Override
	public NodeRole getRole() {
		return NodeRole.ELASTICSEARCH;
	}

}
