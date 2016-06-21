package com.aw.unity.json;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.TestDataType;
import com.aw.unity.TestUnityMeta;
import com.aw.unity.UnityMetadata;
import com.aw.unity.query.Filter;

/**
 *
 *
 */
public class JSONFilterFactoryTest {

	public static final String FILTER_JSON =
			"{\n" +
			"  \"operator\" : \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \""+TestDataType.FIELD_INT.getName()+"\",\n" +
			"          \"operator\": \"EQ\",\n" +
			"          \"values\": [1],\n" +
			"          \"enabled\": \"true\"\n" +
			"        },\n" +
			"        {\n" +
			"              \"operator\" : \"OR\",\n" +
			"              \"filter\": [\n" +
			"                {\n" +
			"                  \"field\": \""+TestDataType.FIELD_IP.getName()+"\",\n" +
			"                  \"operator\": \"EQ\",\n" +
			"                  \"values\": [\"1.2.3.4\"],\n" +
			"                  \"enabled\": \"true\"\n" +
			"                },\n" +
			"                {\n" +
			"                  \"field\": \""+TestDataType.FIELD_MAC.getName()+"\",\n" +
			"                  \"operator\": \"EQ\",\n" +
			"                  \"values\": [\"ab:ab:ab:ab:ab:ab\"],\n" +
			"                  \"enabled\": \"true\"\n" +
			"                }\n" +
			"              ]\n" +
			"            }\n" +
			"          ]\n" +
			"}";

	/**
{
  "operator" : "AND",
      "filter": [

        {
          "field": "field_int",
          "operator": "EQ",
          "values": [1],
          "enabled": "true"
        },

        {
              "operator" : "OR",
              "filter": [
                {
                  "field": "field_ip",
                  "operator": "EQ",
                  "values": ["1.2.3.4"],
                  "enabled": "true"
                },
                {
                  "field": "field_mac",
                  "operator": "EQ",
                  "values": ["ab:ab:ab:ab:ab:ab"],
                  "enabled": "true"
                }
              ]
        },

        {}

        ]
}
	 */
	public static final String FILTER_EMPTY_GROUP_JSON =
			"{\n" +
			"  \"operator\" : \"AND\",\n" +
			"      \"filter\": [\n" +
			"        \n" +
			"        {\n" +
			"          \"field\": \"field_int\",\n" +
			"          \"operator\": \"EQ\",\n" +
			"          \"values\": [1],\n" +
			"          \"enabled\": \"true\"\n" +
			"        },\n" +
			"        \n" +
			"        {\n" +
			"              \"operator\" : \"OR\",\n" +
			"              \"filter\": [\n" +
			"                {\n" +
			"                  \"field\": \"field_ip\",\n" +
			"                  \"operator\": \"EQ\",\n" +
			"                  \"values\": [\"1.2.3.4\"],\n" +
			"                  \"enabled\": \"true\"\n" +
			"                },\n" +
			"                {\n" +
			"                  \"field\": \"field_mac\",\n" +
			"                  \"operator\": \"EQ\",\n" +
			"                  \"values\": [\"ab:ab:ab:ab:ab:ab\"],\n" +
			"                  \"enabled\": \"true\"\n" +
			"                }\n" +
			"              ]\n" +
			"        },\n" +
			"        \n" +
			"        {}\n" +
			"            \n" +
			"        ]\n" +
			"}";

	@Test
	public void newFilter_complex() throws Exception {

		UnityMetadata meta = new TestUnityMeta();
		Filter filter = meta.getFilterFactory().newFilter(FILTER_JSON);

		assertEquals("filter format not correct after parsing", "( field_int EQ 1 AND ( field_ip EQ 1.2.3.4 OR field_mac EQ ab:ab:ab:ab:ab:ab ) )", filter.printCanonical());

	}

	@Test
	public void newFilter_empty_group() throws Exception {

		UnityMetadata meta = new TestUnityMeta();
		Filter filter = meta.getFilterFactory().newFilter(FILTER_EMPTY_GROUP_JSON);

		assertEquals("filter format not correct after parsing", "( field_int EQ 1 AND ( field_ip EQ 1.2.3.4 OR field_mac EQ ab:ab:ab:ab:ab:ab ) )", filter.printCanonical());

	}

}
