package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;

/**
 * Provide the TenantMgr dependency
 *
 *
 *
 */
public class TenantMgrProvider implements Provider<TenantMgr> {

	public TenantMgrProvider(Provider<DocumentHandler> docs, Provider<Platform> platform,
							 Provider<DocumentMgr> docMgr, Provider<PlatformMgr> platformMgr, Provider<RestCluster> restCluster) {
		mgr = new TenantMgr(docs, platform, docMgr, platformMgr, restCluster);
	}

	@Override
	public TenantMgr get() {
		return mgr;
	}

	private TenantMgr mgr;

}
