package com.aw.common.inject.unity;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.inject.AbstractTenantResourceProvider;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.json.JSONUnityInstance;
import com.fasterxml.jackson.databind.Module;

/**
 * A factory for unity instances
 */
public class UnityProvider extends AbstractTenantResourceProvider<UnityInstance> {

	private Provider<DocumentHandler> docs;
	private Provider<Platform> platform;
	private Module[] modules;

	@Inject @com.google.inject.Inject
	public UnityProvider(Provider<DocumentHandler> docs, Provider<Platform> platform, Module... modules) {
		this.docs = docs;
		this.platform = platform;
		this.modules = modules;
	}

	@Override
	protected UnityInstance getInstance(Tenant tenant) throws Exception {

		//get the unity instance - should only be one of these per tenant
		List<Document> instances = docs.get().getDocumentsOfType(DocumentType.UNITY_INSTANCE);
		if (instances.size() != 1) {
			throw new InvalidDataException("There should be exactly one unity instance for tenant " + tenant.getTenantID() + ", found " + instances.size());
		}

		//create, initialize, and return unity
		JSONUnityInstance ret = new JSONUnityInstance(modules);
		ret.initialize(instances.get(0).getBody(), docs.get(), platform);
		return ret;

	}

}
