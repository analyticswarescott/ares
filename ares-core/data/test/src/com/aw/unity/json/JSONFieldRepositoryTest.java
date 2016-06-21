package com.aw.unity.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.aw.common.Tag;
import com.aw.unity.FieldType;
import com.aw.unity.TestUnityInstance;

public class JSONFieldRepositoryTest {

	/**
{
	"fields" : [
		{
			"name" : "field_float",
	 		"type" : "float",
	 		"tags" : [ "tag1", "tag2" ]
		},
		{
			"name" : "field_ip",
			"type" : "ip_address"
	 	},
		{
			"name" : "field_ip_array",
			"type" : "ip_address",
			"array" : true
	 	},
		{
			"name" : "field_string",
			"type" : "string"
	 	},
		{
			"name" : "field_int",
			"type" : "int"
	 	},
		{
			"name" : "field_long",
			"type" : "long"
	 	},
		{
			"name" : "field_mac",
			"type" : "mac_address",
			"tags" : [ "tag1", "tag2", "tag3" ]
	 	},
		{
			"name" : "field_double",
			"type" : "double",
			"array" : true
	 	},
		{
			"name" : "field_guid",
			"type" : "guid",
			"array" : true
	 	},
		{
			"name" : "field_timestamp",
			"type" : "timestamp",
			"array" : true
	 	},
		{
			"name" : "test_subtype",
			"type" : "object",
			"object_type" : "test_subtype",
			"array" : true
	 	}
	]
}
	 */
	public static final String FIELD_JSON =
			"{\n" +
			"	\"fields\" : [\n" +
			"		{\n" +
			"			\"name\" : \"field_float\",\n" +
			"	 		\"type\" : \"float\",\n" +
			"	 		\"tags\" : [ \"tag1\", \"tag2\" ]\n" +
			"		},\n" +
			"		{\n" +
			"			\"name\" : \"field_ip\",\n" +
			"			\"type\" : \"ip_address\"\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_ip_array\",\n" +
			"			\"type\" : \"ip_address\",\n" +
			"			\"array\" : true\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_string\",\n" +
			"			\"type\" : \"string\"\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_int\",\n" +
			"			\"type\" : \"int\"\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_long\",\n" +
			"			\"type\" : \"long\"\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_mac\",\n" +
			"			\"type\" : \"mac_address\",\n" +
			"			\"tags\" : [ \"tag1\", \"tag2\", \"tag3\" ]\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_double\",\n" +
			"			\"type\" : \"double\",\n" +
			"			\"array\" : true\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_guid\",\n" +
			"			\"type\" : \"guid\",\n" +
			"			\"array\" : false\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"field_timestamp\",\n" +
			"			\"type\" : \"timestamp\",\n" +
			"			\"array\" : true\n" +
			"	 	},\n" +
			"		{\n" +
			"			\"name\" : \"test_subtype\",\n" +
			"			\"type\" : \"object\",\n" +
			"			\"object_type\" : \"test_subtype\",\n" +
			"			\"array\" : true\n" +
			"	 	}\n" +
			"	]\n" +
			"}";

	@Test
	public void fromString() throws Exception {

		JSONFieldRepository fields = JSONFieldRepository.fromString(new TestUnityInstance(), FIELD_JSON);

		//make sure some basic stuff is true
		assertEquals(11, fields.getFields().length);
		assertTrue(fields.getFields()[6].getName().equals("field_mac"));
		assertTrue(fields.getFields()[6].getType() == FieldType.MAC_ADDRESS);
		assertEquals("tag count wrong", 3, fields.getFields()[6].getTags().size());
		assertEquals("tags wrong", new HashSet<Tag>(Arrays.asList(Tag.valueOf("tag1"), Tag.valueOf("tag2"), Tag.valueOf("tag3"))), fields.getFields()[6].getTags());

	}
}
