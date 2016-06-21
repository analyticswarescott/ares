package com.aw.unity.json;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.DataType;
import com.aw.unity.TestDocumentHandler;
import com.aw.unity.TestUnityInstance;

public class JSONDataTypeRepositoryTest {

/**
{
		"resolvers" : [
		{
			"type" : "com.aw.unity.json.DefaultJSONDataTypeResolver",
			"exists" : [ "a" ],
  		     "mappings" : [
					{
						"path" : [ "a", "1" ],
						"value" : "abc",
			 			"data_type" : "test_type3"
					}
			]
		},
		{
			"type" : "com.aw.unity.json.DefaultJSONDataTypeResolver",
  		     "mappings" : [
					{
						"path" : [ "abc", "123" ],
						"value" : "xyz",
			 			"data_type" : "test_type"
					},
					{
						"path" : [ "abc", "123" ],
						"value" : "abc",
			 			"data_type" : "test_type2"
					}
			]
		}
		],
  		"types" : [
			{
				"name" : "test_type",
				"display_name" : "Placeholder Type",
				"description" : "Placeholder type description",
				"id_field" : "field_guid",
				"field_refs" : [
					{
						"name" : "field_int",
			      		"path" : [ "field1" ]
					},
					{
						"name" : "field_string",
			      		"path" : [ "field2" ]
					},
					{
						"name" : "field_timestamp",
			      		"path" : [ "field3" ]
					},
					{
						"name" : "field_double",
			      		"path" : [ "field4" ]
					},
					{
						"name" : "field_float",
			      		"path" : [ "field5" ]
					},
					{
						"name" : "field_long",
			      		"path" : [ "field6" ]
					},
					{
						"name" : "field_guid",
			      		"path" : [ "field7" ]
					}
				]
			},
			{
				"name" : "test_subtype",
				"display_name" : "Placeholder Type",
				"description" : "Placeholder type description",
				"field_refs" : [
					{
						"name" : "field_string",
			      		"path" : [ "field2" ]
					},
					{
						"name" : "field_timestamp",
			      		"path" : [ "field3" ]
					},
					{
						"name" : "field_double",
			      		"path" : [ "field4" ]
					}
				]
			},
			{
				"name" : "test_type2",
				"display_name" : "Placeholder Type",
				"description" : "Placeholder type description",
				"id_field" : "field_guid",
				"field_refs" : [
					{
						"name" : "field_int",
			      		"path" : [ "field1" ]
					},
					{
						"name" : "test_subtype"
					},
					{
						"name" : "field_float",
			      		"path" : [ "field5" ]
					},
					{
						"name" : "field_long",
			      		"path" : [ "field6" ]
					},
					{
						"name" : "field_guid",
			      		"path" : [ "field7" ]
					}
				]
			},
			{
				"name" : "test_type3",
				"display_name" : "Placeholder Type",
				"description" : "Placeholder type description",
				"id_field" : "field_guid",
				"field_refs" : [
					{
						"name" : "field_int",
			      		"path" : [ "field1" ]
					},
					{
						"name" : "field_string",
			      		"path" : [ "field2" ]
					},
					{
						"name" : "field_timestamp",
			      		"path" : [ "field3" ]
					},
					{
						"name" : "field_double",
			      		"path" : [ "field4" ]
					},
					{
						"name" : "field_float",
			      		"path" : [ "field5" ]
					},
					{
						"name" : "field_long",
			      		"path" : [ "field6" ]
					},
					{
						"name" : "field_guid",
			      		"path" : [ "field7" ]
					}
				]
			}
		]
}
	 */
	public static final String JSON =
			"{\n" +
			"		\"resolvers\" : [\n" +
			"		{\n" +
			"			\"type\" : \"com.aw.unity.json.DefaultJSONDataTypeResolver\",\n" +
			"			\"exists\" : [ \"a\" ],\n" +
			"  		     \"mappings\" : [\n" +
			"					{\n" +
			"						\"path\" : [ \"a\", \"1\" ],\n" +
			"						\"value\" : \"abc\",\n" +
			"			 			\"data_type\" : \"test_type3\"\n" +
			"					}\n" +
			"			]\n" +
			"		},\n" +
			"		{\n" +
			"			\"type\" : \"com.aw.unity.json.DefaultJSONDataTypeResolver\",\n" +
			"  		     \"mappings\" : [\n" +
			"					{\n" +
			"						\"path\" : [ \"abc\", \"123\" ],\n" +
			"						\"value\" : \"xyz\",\n" +
			"			 			\"data_type\" : \"test_type\"\n" +
			"					},\n" +
			"					{\n" +
			"						\"path\" : [ \"abc\", \"123\" ],\n" +
			"						\"value\" : \"abc\",\n" +
			"			 			\"data_type\" : \"test_type2\"\n" +
			"					}\n" +
			"			]\n" +
			"		}\n" +
			"		], \n" +
			"  		\"types\" : [\n" +
			"			{\n" +
			"				\"name\" : \"test_type\",\n" +
			"				\"display_name\" : \"Placeholder Type\",\n" +
			"				\"description\" : \"Placeholder type description\",\n" +
			"				\"id_field\" : \"field_guid\",\n" +
			"				\"field_refs\" : [\n" +
			"					{\n" +
			"						\"name\" : \"field_int\",\n" +
			"			      		\"path\" : [ \"field1\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_string\",\n" +
			"			      		\"path\" : [ \"field2\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_timestamp\",\n" +
			"			      		\"path\" : [ \"field3\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_double\",\n" +
			"			      		\"path\" : [ \"field4\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_float\",\n" +
			"			      		\"path\" : [ \"field5\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_long\",\n" +
			"			      		\"path\" : [ \"field6\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_guid\",\n" +
			"			      		\"path\" : [ \"field7\" ]\n" +
			"					}\n" +
			"				]\n" +
			"			},\n" +
			"			{\n" +
			"				\"name\" : \"test_subtype\",\n" +
			"				\"display_name\" : \"Placeholder Type\",\n" +
			"				\"description\" : \"Placeholder type description\",\n" +
			"				\"field_refs\" : [\n" +
			"					{\n" +
			"						\"name\" : \"field_string\",\n" +
			"			      		\"path\" : [ \"field2\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_timestamp\",\n" +
			"			      		\"path\" : [ \"field3\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_double\",\n" +
			"			      		\"path\" : [ \"field4\" ]\n" +
			"					}\n" +
			"				]\n" +
			"			},\n" +
			"			{\n" +
			"				\"name\" : \"test_type2\",\n" +
			"				\"display_name\" : \"Placeholder Type\",\n" +
			"				\"description\" : \"Placeholder type description\",\n" +
			"				\"id_field\" : \"field_guid\",\n" +
			"				\"field_refs\" : [\n" +
			"					{\n" +
			"						\"name\" : \"field_int\",\n" +
			"			      		\"path\" : [ \"field1\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"test_subtype\"\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_float\",\n" +
			"			      		\"path\" : [ \"field5\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_long\",\n" +
			"			      		\"path\" : [ \"field6\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_guid\",\n" +
			"			      		\"path\" : [ \"field7\" ]\n" +
			"					}\n" +
			"				]\n" +
			"			},\n" +
			"			{\n" +
			"				\"name\" : \"test_type3\",\n" +
			"				\"display_name\" : \"Placeholder Type\",\n" +
			"				\"description\" : \"Placeholder type description\",\n" +
			"				\"id_field\" : \"field_guid\",\n" +
			"				\"field_refs\" : [\n" +
			"					{\n" +
			"						\"name\" : \"field_int\",\n" +
			"			      		\"path\" : [ \"field1\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_string\",\n" +
			"			      		\"path\" : [ \"field2\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_timestamp\",\n" +
			"			      		\"path\" : [ \"field3\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_ip\",\n" +
			"			      		\"path\" : [ \"field4\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_float\",\n" +
			"			      		\"path\" : [ \"field5\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_long\",\n" +
			"			      		\"path\" : [ \"field6\" ]\n" +
			"					},\n" +
			"					{\n" +
			"						\"name\" : \"field_mac\",\n" +
			"			      		\"path\" : [ \"field7\" ]\n" +
			"					}\n" +
			"				]\n" +
			"			}\n" +
			"		]  \n" +
			"}";

	public static final String JSON_DATA_RESOLVES = "{ \"abc\" : { \"123\" : \"xyz\" } }";
	public static final String JSON_DATA_DOES_NOT_RESOLVE = "{ \"abc\" : { \"123\" : \"XYZ\" } }";

	@Test
	public void test() throws Exception {

		JSONDataTypeRepository repo = new JSONDataTypeRepository();
		final JSONFieldRepository fields = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);

		repo.initialize(new TestDocumentHandler(), new TestUnityInstance());

		//make sure the repo loaded
		assertNotNull("resolvers not set", repo.getResolvers());
		assertEquals("resolvers array not created correctly", 2, repo.getResolvers().length);
		assertTrue("default resolver not set as first resolver", repo.getResolvers()[0] instanceof DefaultJSONDataTypeResolver);
		assertTrue("default resolver not initialized correctly from data type repo", ((DefaultJSONDataTypeResolver)repo.getResolvers()[1]).resolve(JSON_DATA_RESOLVES) == repo.getDataTypes()[0]);
		assertNull("default resolver not initialized correctly from data type repo", ((DefaultJSONDataTypeResolver)repo.getResolvers()[1]).resolve(JSON_DATA_DOES_NOT_RESOLVE));

		//make sure we've loaded our subtype correctly
		assertEquals("wrong number of data types", 4, repo.getDataTypes().length);
		DataType subtype = repo.getDataType("test_subtype");
		assertNotNull("subtype missing", subtype);
		assertEquals("subtype field count wrong", 3, subtype.getFields().length);

	}

}
