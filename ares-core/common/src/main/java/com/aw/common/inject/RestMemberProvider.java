package com.aw.common.inject;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.tenant.Tenant;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;

/**
 * Provider for local rest member
 *
 *
 *
 */
public class RestMemberProvider extends AbstractTenantResourceProvider<LocalRestMember> {

	LocalRestMember member;

	@Inject @com.google.inject.Inject
	public RestMemberProvider(RestCluster cluster, TenantMgr tenantMgr, PlatformMgr platformMgr, Provider<DocumentHandler> docs, Provider<DocumentMgr> docMgr) {
		member = new LocalRestMember(cluster, tenantMgr, platformMgr, docs, docMgr);
	}

	@Override
	protected synchronized LocalRestMember getInstance(Tenant tenant) throws Exception {

		return member;

	}

}
