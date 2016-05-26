package com.aw.common.inject;

import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformMgr;

/**
 * Platform manager dependency
 */
public class PlatformMgrProvider extends AbstractTenantResourceProvider<PlatformMgr> {

	PlatformMgr platformMgr;

	@Override
	protected synchronized PlatformMgr getInstance(Tenant tenant) throws Exception {

		if (platformMgr == null) {
			platformMgr = new PlatformMgr();
			platformMgr.initialize();
		}

		return platformMgr;

	}

}
