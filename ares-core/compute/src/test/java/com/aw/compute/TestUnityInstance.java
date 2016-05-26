package com.aw.compute;

import java.io.File;

import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.document.DocumentHandler;
import com.aw.document.LocalDocumentHandler;
import com.aw.document.DocumentType;
import com.aw.unity.json.JSONUnityInstance;
import com.aw.util.Statics;

public class TestUnityInstance extends JSONUnityInstance {

	public TestUnityInstance() {

		try {

			System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

			File location = new File(Statics.RELATIVE_DOCS_PATH);
			if (location.exists()) {
				DocumentHandler handler = new LocalDocumentHandler(location.getAbsolutePath());
				initialize(handler.getDocument(DocumentType.UNITY_INSTANCE, "default").getBody(), handler, new TestProvider<>(new TestPlatform()));
			}

			else {
				throw new Exception("can't find documents on disk for local doc handler (" + location.getAbsolutePath());
			}

		} catch (Exception e) {
			throw new RuntimeException("error building test unity instance", e);
		}

	}

}
