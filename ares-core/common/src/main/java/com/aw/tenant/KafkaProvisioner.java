package com.aw.tenant;

import java.util.List;

import org.apache.log4j.Logger;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.spark.PreReqUtil;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;

/**
 * provisions kafka
 *
 *
 *
 */
public class KafkaProvisioner implements Provisioner {

	private static final Logger LOGGER = Logger.getLogger(KafkaProvisioner.class);

	@Override
	public Provisioner provision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

		LOGGER.debug(" provisioning kafka for tenant " + tenant.getTenantID());

        List<Document> procs = tenantMgr.getDocs().getDocumentsOfType(DocumentType.STREAM_TENANT);
        PreReqUtil.provisionTopics(tenantMgr.getPlatform(), tenant, procs);

        LOGGER.debug (" completed provisioning of kafka for tenant " + tenant.getTenantID());

        return this;

	}

	@Override
	public Provisioner unprovision(Tenant tenant, TenantMgr tenantMgr) throws Exception {

        LOGGER.info("un-provisioning kafka for tenant " + tenant.getTenantID());

        Impersonation.impersonateTenant(tenant.getTenantID());

        try {

        	List<Document> procs = tenantMgr.getDocs().getDocumentsOfType(DocumentType.STREAM_TENANT);
	        PreReqUtil.unProvisionTopics(tenantMgr.getPlatform(), procs);

	        LOGGER.info("completed un-provisioning of kafka for tenant " + tenant.getTenantID());

        } finally {
            Impersonation.unImpersonate();
        }

        return this;

	}

	@Override
	public NodeRole getRole() {
		return NodeRole.KAFKA;
	}

}
