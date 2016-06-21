package com.aw.unity.defaults;

import java.util.List;

import javax.inject.Provider;

import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.unity.UnityFactory;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.json.JSONUnityInstance;

/**
 * Creates unity instances given tenant IDs
 *
 *
 */
public class DefaultUnityFactory implements UnityFactory {

	@Override
	public UnityInstance getInstance(String tenantID, DocumentHandler docs, Provider<Platform> platform) throws Exception {

		//get the unity instance - should only be one of these
		List<Document> instances = docs.getDocumentsOfType(DocumentType.UNITY_INSTANCE);
		if (instances.size() != 1) {
			throw new InvalidDataException("There should be exactly one unity instance for tenant " + tenantID + ", found " + instances.size());
		}

		//instantiate unity for the first document we find
		UnityInstance instance = newInstance(instances.get(0), docs, platform);

		return instance;

	}

	private UnityInstance newInstance(Document doc, DocumentHandler docs, Provider<Platform> platform) throws Exception {

		UnityInstance instance = new JSONUnityInstance();
		instance.initialize(doc.getBody(), docs, platform);

		return instance;

	}
}
