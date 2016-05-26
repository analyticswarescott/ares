package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.common.tenant.Tenant;
import com.aw.document.DocumentMgr;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;

public class DocumentMgrFactory  extends AbstractTenantResourceProvider<DocumentMgr> {

	private DocumentMgr documentMgr;

	public DocumentMgrFactory(Provider<RestCluster> restCluster, PlatformMgr platformMgr, DocumentJDBCProvider jdbc) {
		documentMgr = new DocumentMgr(restCluster.get(), platformMgr, jdbc);
	}

	@Override
	protected DocumentMgr getInstance(Tenant tenant) throws Exception {
		return documentMgr;
	}

}
