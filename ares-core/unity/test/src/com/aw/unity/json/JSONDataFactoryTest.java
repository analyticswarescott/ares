package com.aw.unity.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.unity.Data;
import com.aw.unity.TestDocumentHandler;

public class JSONDataFactoryTest {

	@Test
	public void newData() throws Exception {

		JSONUnityInstance instance = new JSONUnityInstance();
		instance.initialize(JSONUnityInstanceTest.JSON, new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		JSONDataFactory factory = new JSONDataFactory();
		Data data = factory.newData(JSONDataTypeRepositoryTest.JSON_DATA_RESOLVES, instance);

		//make sure it's the right type - other than that, we aren't testing anything here
		assertEquals(instance.getDataTypeRepository().getDataTypes()[0], data.getType());

	}

}

