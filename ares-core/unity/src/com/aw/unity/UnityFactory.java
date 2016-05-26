package com.aw.unity;

import javax.inject.Provider;

import com.aw.document.DocumentHandler;
import com.aw.platform.Platform;

/**
 * Creates unity instances for tenants
 *
 *
 */
public interface UnityFactory {

	//for now, we have a type key in the unity instance data to indicate its class type - when we promote the concept
	//of reflection-created body instances to the document api, this will go away
	public static final String TYPE = "type";

	/**
	 * Create a unity instance for a specific tenant
	 *
	 * @param tenantID The tenant ID for which a unity instance will be created
	 * @return The new unity instance
	 */
	public UnityInstance getInstance(String tenantID, DocumentHandler documents, Provider<Platform> platform) throws Exception;

}
