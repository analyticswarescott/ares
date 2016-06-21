package com.aw.unity.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.unity.TestUnityInstance;
import com.aw.unity.query.Query;

/**
 * Test building a query from JSON
 *
 *
 */
public class JSONQueryFactoryTest {

	private final JSONQueryFactory factory = new JSONQueryFactory();
	/**
 {
    "detail": true,
    "top": 25,
    "count_only": false,
    "data_types" : [
    	"test_type"
    ],
    "attributes": [
      {
        "field": "field_int",
        "order": "asc",
        "aggregate": "count_distinct"
      },
      {
        "field": "field_float",
        "order": "asc",
        "aggregate": "none"
      },
      {
        "field": "field_string",
        "order": "asc",
        "aggregate": "none"
      }
    ],
    "filter": {
	  "operator": "AND",
	  "filter": [
	    {
	      "field": "field_int",
	      "operator": "in",
	      "values": [ 1, 2, 3 ]
	    }
	  ]
	}
}
	 */
	static final String QUERY =
			" {\n" +
			"    \"detail\": true,\n" +
			"    \"top\": 25,\n" +
			"    \"count_only\": false,\n" +
			"    \"data_types\" : [\n" +
			"    	\"test_type\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n" +
			"      {\n" +
			"        \"field\": \"field_int\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"count_distinct\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_ip\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_string\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      }\n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"	  \"operator\": \"AND\",\n" +
			"	  \"filter\": [\n" +
			"	    {\n" +
			"	      \"field\": \"field_int\",\n" +
			"	      \"operator\": \"in\",\n" +
			"	      \"values\": [ 1, 2, 3 ]\n" +
			"	    }\n" +
			"	  ]\n" +
			"	}\n" +
			"  }\n";

	@Test
	public void newQuery_complex() throws Exception {

		Query query = factory.newQuery(new JSONObject(QUERY), new TestUnityInstance());

		assertEquals("query attribute list size wrong", 3, query.getAttributes().length);
		assertNotNull("query attribute(s) null", query.getAttributes()[1].getField());
		assertEquals("query attribute(s) wrong", "field_ip", query.getAttributes()[1].getField().getName());

	}

	@Test
	public void testAllAttributes() throws Exception {

		final String sortedQuery =
			" {\n" +
				"    \"detail\": true,\n" +
				"    \"all_attributes\": true,\n" +
				"    \"count_only\": false,\n" +
				"    \"data_types\" : [\n" +
				"    	\"test_type\"\n" +
				"    ],\n" +
				"    \"attributes\": [\n" +
				"      {\n" +
				"        \"field\": \"field_int\",\n" +
				"        \"order\": \"asc\"\n" +
				"      }\n" +
				"    ]\n" +
				"  }\n";

		Query query = factory.newQuery(new JSONObject(sortedQuery), new TestUnityInstance());

		// Make sure we parsed the all_attributes flag correctly
		assertTrue(query.isAllAttributes());

	}

}
