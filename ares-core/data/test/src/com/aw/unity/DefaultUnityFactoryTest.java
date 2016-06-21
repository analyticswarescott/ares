package com.aw.unity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.unity.defaults.DefaultUnityFactory;
import com.aw.unity.json.JSONUnityInstance;

/**
 * Test the unity factory stuff
 *
 *
 *
 */
public class DefaultUnityFactoryTest {

	@Test
	public void test() throws Exception {

		DefaultUnityFactory factory = new DefaultUnityFactory();
		UnityInstance instance = factory.getInstance(TestDocumentHandler.TENANT, new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		//make sure the properties are correct
		assertEquals("unity es data source should be TestDataSource", TestDataSource.class, instance.getDataSource("test").getClass());
		assertEquals("unity type is wrong", JSONUnityInstance.class, instance.getClass());
		assertEquals("unity metadata type wrong", JSONUnityInstance.class, instance.getMetadata().getClass());
		assertNotNull("unity data types ", instance.getMetadata().getDataTypeRepository());
		assertEquals("unity data type array size wrong", 4, instance.getMetadata().getDataTypeRepository().getDataTypes().length);
		assertEquals("unity data type name wrong", "test_type", instance.getMetadata().getDataTypeRepository().getDataTypes()[0].getName());

	}

}
