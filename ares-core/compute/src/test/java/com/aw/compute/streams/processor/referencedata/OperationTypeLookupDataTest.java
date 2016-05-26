package com.aw.compute.streams.processor.referencedata;

import org.junit.Test;
import static org.junit.Assert.*;

import com.aw.common.TestPlatform;
import com.aw.common.util.RestClient;
import com.aw.compute.TestUnityInstance;
import com.aw.compute.referencedata.OperationTypeLookupData;
import com.aw.unity.UnityInstance;

public class OperationTypeLookupDataTest {

	static final String JSON =
			"[{\n" +
			"	\"mappings\" : [\n" +
			"		{\n" +
			"			\"path\" : [ \"ot\" ],\n" +
			"			\"value\" : \"xyz\",\n" +
			" 			\"data_type\" : \"test\"\n" +
			"		}\n" +
			"	]\n" +
			"}]\n";

	@Test
	public void check() throws Exception {

		//TODO: dependency injection
		OperationTypeLookupData data = new OperationTypeLookupData(new TestPlatform()) {

			@Override
			protected RestClient getClient() {
				return new RestClient() {
					@Override
					public String getString(String path) throws Exception {
						return JSON;
					}
				};
			}

			@Override
			protected UnityInstance getNewUnity() throws Exception {

				//return a test unity instance
				return new TestUnityInstance();

			}

		};

		assertEquals("user_print_file", data.get("22"));
		assertNull("expected null for missing ot mapping", data.get("100"));
		assertNull("expected null for null key", data.get(null));

	}

}
