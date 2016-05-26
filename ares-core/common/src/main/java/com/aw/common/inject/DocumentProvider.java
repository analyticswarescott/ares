package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.common.tenant.Tenant;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;

/**
 * Provides document handler instances per tenant
 *
 *
 *
 */
public class DocumentProvider extends AbstractTenantResourceProvider<DocumentHandler> {

	private Provider<DocumentMgr> docMgr;

	public DocumentProvider(Provider<DocumentMgr> docMgr) {
		this.docMgr = docMgr;
	}

	@Override
	protected DocumentHandler getInstance(Tenant tenant) throws Exception {
		return docMgr.get().getDocHandler();
	}

}
