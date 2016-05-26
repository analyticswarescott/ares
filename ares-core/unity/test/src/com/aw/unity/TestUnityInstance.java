package com.aw.unity;

import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.unity.json.JSONUnityInstance;
import com.aw.unity.json.JSONUnityInstanceTest;

public class TestUnityInstance extends JSONUnityInstance {

	public TestUnityInstance() throws Exception {
		initialize(JSONUnityInstanceTest.JSON, new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));
	}

}
