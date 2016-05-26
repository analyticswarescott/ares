package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.common.tenant.Tenant;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;

/**
 * Inject platforms
 */
public class PlatformFactory extends AbstractTenantResourceProvider<Platform>  {

	private Provider<PlatformMgr> platformMgr;

	public PlatformFactory(Provider<PlatformMgr> platformMgr) {
		this.platformMgr = platformMgr;
	}

	@Override
	protected Platform getInstance(Tenant tenant) throws Exception {

		//for now every tenant sees the same platform
		return platformMgr.get().getPlatform();

	}

}
