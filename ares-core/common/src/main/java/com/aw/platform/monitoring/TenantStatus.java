package com.aw.platform.monitoring;

import java.io.IOException;
import java.util.Collection;

import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.document.DocumentHandler;
import com.aw.platform.Platform;

/**
 * Status for a tenant within the platform.
 *
 *
 *
 */
//@JsonDeserialize(as=DefaultTenantStatus.class)
public interface TenantStatus extends Status {

	/**
	 * @return The tenant this status is associated with
	 */
	public Tenant getTenant();

	/**
	 * Collect status for the given tenant on the given platform
	 *
	 * @param platform The platform on which we are collecting status
	 * @param tenant The tenant on which we are collecting status
	 */
	public void collect(ZkAccessor zk, Platform platform, Tenant tenant, DocumentHandler docs) throws IOException;

	/**
	 * @return The stream status for a single tenant
	 */
	public Collection<StreamStatus> getStreamStatus();

}
