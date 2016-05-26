package com.aw.unity.json;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.UnityInstance;
import com.aw.unity.query.datatypes.IpAddress;
import com.google.common.net.InetAddresses;

/**
 * Test basic JSON field operations
 *
 *
 */
public class JSONDataTest {

	/**
{
	"name" : "test_type",
	"display_name" : "Test Type",
	"description" : "Test Type Description",
	"id_field" : "field_string",
	"field_refs" : [
		{
			"name" : "field_float",
      		"path" : [ "field1" ]
		},
		{
			"name" : "field_string",
      		"path" : [ "field3" ]
		},
		{
			"name" : "field_int",
      		"path" : [ "field4" ]
		},
		{
			"name" : "field_long",
      		"path" : [ "field5" ]
		},
		{
			"name" : "field_mac",
      		"path" : [ "field6" ]
		},
		{
			"name" : "field_ip",
      		"path" : [ "field2" ]
		},
		{
			"name" : "field_ip_array",
      		"path" : [ "a", "b", "c" ]
		}
	]
}
	 */
	private static final String TYPE_JSON =
			"{\n" +
			"	\"name\" : \"test_type\",\n" +
			"	\"display_name\" : \"Test Type\",\n" +
			"	\"description\" : \"Test Type Description\",\n" +
			"	\"id_field\" : \"field_string\",\n" +
			"	\"field_refs\" : [\n" +
			"		{\n" +
			"			\"name\" : \"field_float\",\n" +
			"      		\"path\" : [ \"field1\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_string\",\n" +
			"      		\"path\" : [ \"field3\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_int\",\n" +
			"      		\"path\" : [ \"field4\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_long\",\n" +
			"      		\"path\" : [ \"field5\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_mac\",\n" +
			"      		\"path\" : [ \"field6\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_ip\",\n" +
			"      		\"path\" : [ \"field2\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_ip_array\",\n" +
			"      		\"path\" : [ \"a\", \"b\", \"c\" ]\n" +
			"		}\n" +
			"	]\n" +
			"}";

	/**
	 * test subtypes too
					{
						"name" : "test_subtype",
						"field_refs" : [
							{
								"name" : "field_ip",
					      		"path" : [ "field2" ]
							},
							{
								"name" : "field_string",
					      		"path" : [ "field3" ]
							},
							{
								"name" : "field_int",
					      		"path" : [ "field4" ]
							}
						]
					}

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
						"name" : "test_subtype",
						"path" : [ "object" ]
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
	 */

	static final String TYPE_JSON_2 =
			"					{\n" +
			"						\"name\" : \"test_subtype\",\n" +
			"						\"field_refs\" : [\n" +
			"							{\n" +
			"								\"name\" : \"field_ip\",\n" +
			"					      		\"path\" : [ \"field2\" ]\n" +
			"							},\n" +
			"							{\n" +
			"								\"name\" : \"field_string\",\n" +
			"					      		\"path\" : [ \"field3\" ]\n" +
			"							},\n" +
			"							{\n" +
			"								\"name\" : \"field_int\",\n" +
			"					      		\"path\" : [ \"field4\" ]\n" +
			"							}\n" +
			"						]\n" +
			"					}\n";

	static final String TYPE_JSON_3 =
			"{\n" +
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
			"						\"name\" : \"test_subtype\",\n" +
			"						\"path\" : [ \"object\" ]\n" +
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
			"			}";

	@Test
	public void getField() throws Exception {

		JSONObject cfg = new JSONObject(
			"{\n" +
			"	\"field1\" : 10.0,\n" +
			"	\"field2\" : \"1.2.3.4\",\n" +
			"	\"field3\" : \"test string\",\n" +
			"	\"field4\" : 15,\n" +
			"	\"field5\" : 20,\n" +
			"	\"field6\" : \"ab:ab:ab:ab:ab:ab\",\n" +
			"	\"a\" : { \"b\" : { \"c\" : [ \"1.2.3.4\", \"2.3.4.5\" ] } },\n" +
			"   \"field8\" : \"102398120398\",\n" +
			"   \"field9\" : \"1234123124:1234123141\"\n" +
			"}"
		);

		UnityInstance unity = new TestUnityInstance();
		JSONFieldRepository fields = JSONFieldRepository.fromString(unity, JSONFieldRepositoryTest.FIELD_JSON);
		Data data = new JSONData(JSONDataType.fromString(new JSONDataTypeRepository(), TYPE_JSON, fields), cfg);

		assertEquals("Double fields", 10.0f, data.getValue(fields.getField("field_float")));
		assertEquals("IP fields", new IpAddress("1.2.3.4"), data.getValue(fields.getField("field_ip")));
		assertEquals("String fields", "test string", data.getValue(fields.getField("field_string")));
		assertEquals("Integer fields", 15, data.getValue(fields.getField("field_int")));
		assertEquals("Long fields", 20L, data.getValue(fields.getField("field_long")));
		assertEquals("Mac fields", "ab:ab:ab:ab:ab:ab", data.getValue(fields.getField("field_mac")));

		assertArrayEquals("IP arrays", new Object[] { InetAddresses.forString("1.2.3.4"), InetAddresses.forString("2.3.4.5") }, (Object[])data.getValue(fields.getField("field_ip_array")));

	}

	@Test
	public void getFieldSubType() throws Exception {

		JSONObject cfg = new JSONObject(
			"{\n" +
			"	\"field1\" : 10.0,\n" +
			"   \"object\" : [{\n" +
			"	     \"field2\" : \"1.2.3.4\",\n" +
			"	     \"field3\" : \"test string\",\n" +
			"	     \"field4\" : 15,\n" +
			"	}],\n" +
			"	\"field5\" : 20,\n" +
			"	\"field6\" : \"ab:ab:ab:ab:ab:ab\",\n" +
			"	\"a\" : { \"b\" : { \"c\" : [ \"1.2.3.4\", \"2.3.4.5\" ] } },\n" +
			"   \"field8\" : \"102398120398\",\n" +
			"   \"field7\" : \"1234123124:1234123141\"\n" +
			"}"
		);


		final Map<String, DataType> types = new HashMap<String, DataType>();
		JSONFieldRepository fields = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);
		JSONDataTypeRepository repo = new JSONDataTypeRepository() {
			@Override
			public DataType getDataType(String name) {
				return types.get(name);
			}
		};

		JSONDataType type1 = JSONDataType.fromString(repo, TYPE_JSON_2, fields);
		types.put(type1.getName(), type1);
		JSONDataType type2 = JSONDataType.fromString(repo, TYPE_JSON_3, fields);
		types.put(type2.getName(), type2);

		Data data = new JSONData(type2, cfg);

		assertEquals("Double fields", 20.0f, data.getValue(fields.getField("field_float")));
		assertEquals("IP fields", new IpAddress("1.2.3.4"), ((Data[])data.getValue(fields.getField("test_subtype")))[0].getValue(fields.getField("field_ip")));
		assertEquals("String fields", "test string", ((Data[])data.getValue(fields.getField("test_subtype")))[0].getValue(fields.getField("field_string")));
		assertEquals("Integer fields", 15, ((Data[])data.getValue(fields.getField("test_subtype")))[0].getValue(fields.getField("field_int")));
		assertEquals("Guid fields", "1234123124:1234123141", data.getValue(fields.getField("field_guid")));

	}

}
