package com.aw.unity.json;

import static org.junit.Assert.*;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.TestDataTypeRepository;

public class DefaultJSONDataTypeResolverTest {

	/**
{
	"mappings" : [
		{
			"path" : [ "abc", "123" ],
			"value" : "xyz",
 			"data_type" : "test_data_type"
		}
	]
}
	 */
	static final String RESOLVER_JSON =
			"{\n" +
			"	\"mappings\" : [\n" +
			"		{\n" +
			"			\"path\" : [ \"abc\", \"123\" ],\n" +
			"			\"value\" : \"xyz\",\n" +
			" 			\"data_type\" : \"test\"\n" +
			"		}\n" +
			"	]\n" +
			"} \n";

	/**
{
	"abc" : {
			"123" : "xyz"
	}
}
	 */
	private static final String DATA_JSON_RESOLVES =
			"{\n" +
			"	\"abc\" : {\n" +
			"			\"123\" : \"xyz\"\n" +
			"	}\n" +
			"}";

	/**
{
	"abc" : {
			"123" : "XYZ"
	}
}
	 */
	private static final String DATA_JSON_DOES_NOT_RESOLVE_WRONG_VALUE =
			"{\n" +
			"	\"abc\" : {\n" +
			"			\"123\" : \"XYZ\"\n" +
			"	}\n" +
			"}";

	/**
{
	"abc" : {
			"234" : "xyz"
		}
}
	 */
	private static final String DATA_JSON_DOES_NOT_RESOLVE_WRONG_PATH =
			"{\n" +
			"	\"abc\" : {\n" +
			"			\"234\" : \"xyz\"\n" +
			"		}\n" +
			"}";

	@Test
	public void test() throws Exception {

		DataTypeRepository repository = new TestDataTypeRepository();
		DefaultJSONDataTypeResolver resolver = new DefaultJSONDataTypeResolver();

		resolver.initialize(repository, new JSONObject(RESOLVER_JSON));

		//test resolution
		DataType type = resolver.resolve(new JSONObject(DATA_JSON_RESOLVES));
		assertSame("data type resolution failed", repository.getDataTypes()[0], type);

		type = resolver.resolve(new JSONObject(DATA_JSON_DOES_NOT_RESOLVE_WRONG_PATH));
		assertNull("data type resolution should have failed", type);

		type = resolver.resolve(new JSONObject(DATA_JSON_DOES_NOT_RESOLVE_WRONG_VALUE));
		assertNull("data type resolution should have failed", type);

	}
}
