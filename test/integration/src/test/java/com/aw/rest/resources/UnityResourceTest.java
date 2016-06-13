package com.aw.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.aw.common.util.es.ESKnownIndices;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.common.util.JSONUtils;
import com.aw.tools.generators.ESGenerator;

public class UnityResourceTest extends BaseIntegrationTest {

	static String TENANT_ID = "1";

	@Override
	protected boolean usesElasticsearch() {
		return true;
	}

	@Test
	public final void unityIntegrationTest() throws Exception {

        provisionTenant(TENANT_ID);

        getDatatypes();

        getResolvers();

		getFields();

		queries();

		testCountDistinct();

		testCountAndCountDistinct();

	}

	private static final String CONTEXT_NO_DATATYPES = "{\n" +
			"\"filter\": {},\n" +
			"\"data_types_group\": \"incident\",\n" +
			"\"context\": \"incidents\",\n" +
			"\"attributes\": [\n" +
			"{ \"field\": \"inc_guid\", \"order\": \"asc\", \"aggregate\": \"none\" }\n" +
			",\n" +
			"{ \"field\": \"dg_user\", \"order\": \"asc\", \"aggregate\": \"none\" }\n" +
			"]\n" +
			"}";

	private static final String NO_CONTEXT_EMPTY_DATATYPES = "{\n" +
			"\"filter\": {},\n" +
			"\"data_types_group\": \"incident\",\n" +
			"\"data_types\": [],\n" +
			"\"attributes\": [\n" +
			"{ \"field\": \"dg_guid\", \"order\": \"asc\", \"aggregate\": \"none\" }\n" +
			",\n" +
			"{ \"field\": \"dg_user\", \"order\": \"asc\", \"aggregate\": \"none\" }\n" +
			"]\n" +
			"}";

	public final void getDatatypes() throws Exception {

        String username = UUID.randomUUID().toString();
        addUser(username, TENANT_ID);

        //get datatypes
        JSONArray datatypes = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/datatypes", JSONArray.class);
        assertNotNull("Expected datatypes from unity", datatypes);
        assertNotNull("Datatype array from unity empty", datatypes.length() > 0);

	}

	public final void getResolvers() throws Exception {

        String username = UUID.randomUUID().toString();
        addUser(username, TENANT_ID);

        //get resolvers
        JSONArray resolvers = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/resolvers", JSONArray.class);
        assertNotNull("Expected resolvers from unity", resolvers);
        assertTrue("Resolver array from unity empty", resolvers.length() > 0);

	}

	public final void getFields() throws Exception {

        String username = UUID.randomUUID().toString();
        addUser(username, TENANT_ID);

        //get fields
        JSONArray fields = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/fields", JSONArray.class);
        assertNotNull("Expected fields from unity", fields);
        assertNotNull("Field array from unity empty", fields.length() > 0);

	}

	//bulk test json - logon, download a bunch of bad files of varying sizes, logoff
	/**
{ "ot" : 23, "esu" : "2015-09-01 08:00:00.000 -0500", "un" : "jlehmann" }
{ "ot" :  2, "esu" : "2015-09-01 08:01:00.000 -0500", "un" : "jlehmann", "in" : "badFile1.exe", "fs" : 10000 }
{ "ot" :  2, "esu" : "2015-09-01 08:02:00.000 -0500", "un" : "jlehmann", "in" : "badFile2.exe", "fs" : 20000 }
{ "ot" :  2, "esu" : "2015-09-01 08:03:00.000 -0500", "un" : "jlehmann", "in" : "badFile3.exe", "fs" : 15000 }
{ "ot" :  2, "esu" : "2015-09-01 08:04:00.000 -0500", "un" : "jlehmann", "in" : "badFile4.exe", "fs" : 12000 }
{ "ot" :  2, "esu" : "2015-09-01 08:05:00.000 -0500", "un" : "jlehmann", "in" : "badFile5.exe", "fs" : 18000 }
{ "ot" :  2, "esu" : "2015-09-01 08:06:00.000 -0500", "un" : "jlehmann", "in" : "badFile6.exe", "fs" : 17000 }
{ "ot" :  2, "esu" : "2015-09-01 08:07:00.000 -0500", "un" : "jlehmann", "in" : "badFile7.exe", "fs" : 14000 }
{ "ot" :  2, "esu" : "2015-09-01 08:08:00.000 -0500", "un" : "jlehmann", "in" : "badFile8.exe", "fs" : 19000 }
{ "ot" :  2, "esu" : "2015-09-01 08:09:00.000 -0500", "un" : "jlehmann", "in" : "badFile9.exe", "fs" : 600000 }
{ "ot" :  2, "esu" : "2015-09-01 08:10:00.000 -0500", "un" : "jlehmann", "in" : "badFile10.exe", "fs" : 10000 }
{ "ot" :  2, "esu" : "2015-09-01 08:11:00.000 -0500", "un" : "jlehmann", "in" : "badFile11.exe", "fs" : 12000 }
{ "ot" : 24, "esu" : "2015-09-01 08:15:00.000 -0500", "un" : "jlehmann" }
	 */
	private static List<String> BULK_TEST_DATA_1 = null; static { try { BULK_TEST_DATA_1 = Arrays.asList(new String[] {
		"{ \"dg_utype\": \"user_action_logon\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:00:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\" }"
	});
	} catch (Exception e ) { throw new RuntimeException(e); } }

	private static List<String> BULK_TEST_DATA_2 = null; static { try { BULK_TEST_DATA_2 = Arrays.asList(new String[] {
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 10000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 20000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 30000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 40000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 50000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 60000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:45:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile3.exe\", \"uad_fs\" : 15000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:00:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile4.exe\", \"uad_fs\" : 12000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:05:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile5.exe\", \"uad_fs\" : 18000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:05:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile5.exe\", \"uad_fs\" : 18000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:05:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile5.exe\", \"uad_fs\" : 18000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:05:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile5.exe\", \"uad_fs\" : 18000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:05:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile5.exe\", \"uad_fs\" : 18000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:10:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 17000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile6.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 14000 }",
		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile7.exe\", \"uad_fs\" : 14000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:20:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile8.exe\", \"uad_fs\" : 19000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:25:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile9.exe\", \"uad_fs\" : 600000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:30:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile10.exe\", \"uad_fs\" : 10000 }",

		"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:35:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\", \"ua_in\" : \"badFile11.exe\", \"uad_fs\" : 12000 }"
	});
	} catch (Exception e ) { throw new RuntimeException(e); } }

	private static List<String> BULK_TEST_DATA_3 = null; static { try { BULK_TEST_DATA_3 = Arrays.asList(new String[] {

		"{ \"dg_utype\" : \"user_action_logoff\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 09:40:00.000 -0500").getTime()+", \"ua_un\" : \"jlehmann\" }"

	});
	} catch (Exception e ) { throw new RuntimeException(e); } }

	private static int bulkDataSize() { return BULK_TEST_DATA_1.size() + BULK_TEST_DATA_2.size() + BULK_TEST_DATA_3.size(); }

	/**

	 TEST QUERIES
{
    "detail": false,
    "top": 25,
    "count_only": false,
    "data_types": [
        "user_file_copy"
    ],
    "attributes": [
        {
            "field": "ua_in",
            "sort": "asc",
            "aggregate": "none"
        },
        {
            "field": "dg_user",
            "sort": "asc",
            "aggregate": "none"
        },
        {
            "field": "*",
            "sort": "asc",
            "aggregate": "count"
        }
    ],
    "filter": {
        "operator": "AND",
        "filter": [
            {
                "field": "ua_in",
                "operator": "in",
                "values": [
                    "chrome.exe"
                ],
                "enabled": true
            }
        ]
    }
}
	 */

	//what follows is a generic query structure to make defining queries in this test class easier

	//common query json prefix
	public static final String QUERY_PREFIX =
			"{\n" +
			"    \"detail\": false,\n" +
			"    \"size\": 0,\n" +
			"    \"count_only\": false,\n" +
			"    \"data_types\": [\n" +
			"        \"user_file_copy\",\"user_action_logon\",\"user_action_logoff\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n";

	//json part between attributes and filters
	public static final String QUERY_START_FILTER =
			"    ]" +
			",\n" +
			"    \"filter\": {\n" +
			"        \"operator\": \"AND\",\n" +
			"        \"filter\": [\n";

	//common query suffix
	public static final String QUERY_SUFFIX =
			"        ]\n" +
			"    }\n" +
			"}";

	//attribute sets

	public static final String ATTRIBS_COUNT_USER_FILE =
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"order\": \"desc\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"ua_in\",\n" +
			"            \"order\": \"asc\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"dg_user\"\n" +
			"        }\n";

	public static final String ATTRIBS_USER_COUNT =
			"        {\n" +
			"            \"field\": \"dg_user\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }";

	public static final String QUERY_BAD_FILE_1 =
			QUERY_PREFIX +
			ATTRIBS_COUNT_USER_FILE +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"operator\": \"in\",\n" +
			"                \"values\": [\n" +
			"                    \"badFile1.exe\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_STARTS_WITH_BAD_ORDER_BY_COUNT =
			QUERY_PREFIX +
			ATTRIBS_COUNT_USER_FILE +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"operator\": \"starts_with\",\n" +
			"                \"values\": [\n" +
			"                    \"bad\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_CONTAINS_FILE =
			QUERY_PREFIX +
			ATTRIBS_COUNT_USER_FILE +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"operator\": \"contains\",\n" +
			"                \"values\": [\n" +
			"                    \"File\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_COUNT_BY_USER =
			QUERY_PREFIX +
			ATTRIBS_USER_COUNT +
			QUERY_START_FILTER +
			//no filter on this one
			QUERY_SUFFIX;

	public static final String QUERY_CONTAINS_FILE_LOWERCASE =
			QUERY_PREFIX +
			ATTRIBS_COUNT_USER_FILE +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"operator\": \"contains\",\n" +
			"                \"values\": [\n" +
			"                    \"file\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_TIME_TREND_BY_MINUTE =
			QUERY_PREFIX +
//			"        {\n" +
//			"            \"field\": \"dg_user\"\n" +
//			"        },\n" +
			"        {\n" +
			"            \"field\": \"dg_time\",\n" +
			"            \"aggregate\": \"minute\"\n" +
			"        }," +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }" +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"dg_time\",\n" +
			"                \"operator\": \"between\",\n" +
			"                \"values\": [\n" +
			"                    \"2015-09-01 09:00:00.000 -0500\",\n" +
			"                    \"2015-09-01 10:00:00.000 -0500\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_TIME_TREND_BY_MINUTE_BY_USER_BY_FILE =
			QUERY_PREFIX +
			"        {\n" +
			"            \"field\": \"dg_user\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"dg_time\",\n" +
			"            \"aggregate\": \"minute\"\n" +
			"        }," +
			"        {\n" +
			"            \"field\": \"ua_in\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }" +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"dg_time\",\n" +
			"                \"operator\": \"between\",\n" +
			"                \"values\": [\n" +
			"                    \"2015-09-01 09:00:00.000 -0500\",\n" +
			"                    \"2015-09-01 10:00:00.000 -0500\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String QUERY_TIME_TREND_BY_HOUR_BY_USER_BY_FILE =
			QUERY_PREFIX +
			"        {\n" +
			"            \"field\": \"dg_user\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"dg_time\",\n" +
			"            \"aggregate\": \"hour\"\n" +
			"        }," +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }" +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"$$hashkey\": \"object:1234\",\n" +
			"                \"field\": \"dg_time\",\n" +
			"                \"operator\": \"between\",\n" +
			"                \"values\": [\n" +
			"                    \"2015-09-01 08:00:00.000 -0500\",\n" +
			"                    \"2015-09-01 10:00:00.000 -0500\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	public static final String TEST_QUERY =
			QUERY_PREFIX +
			"        {\n" +
			"            \"field\": \"dg_time\",\n" +
			"            \"aggregate\": \"day\"\n" +
			"        }," +
			"        {\n" +
			"            \"field\": \"dg_user\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }" +
			QUERY_START_FILTER +
			"            {\n" +
			"                \"field\": \"dg_time\",\n" +
			"                \"operator\": \"in\",\n" +
			"                \"values\": [\n" +
			"                    \"last_n_days\",\n" +
			"                    \"30\"\n" +
			"                ],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			QUERY_SUFFIX;

	/**
	 * Mappings for test elasticsearch data
	 *
{
        "settings": {
            "number_of_shards": 1,
            "number_of_replicas": 1,
            "index": {"analysis": {"analyzer": {"keylower": {
                "tokenizer": "keyword",
                "filter": "lowercase"
            }}}}
        },

"mappings":
	{
		"user_action_logon": {"properties": {
    		"ua_esu": {"type": "date", "format" : "epoch_millis" },
    		"ua_fs": {"type": "long"},
    		"ua_in": {"type": "string", "index" : "not_analyzed" },
    		"ua_ot": {"type": "long"},
    		"ua_un": {"type": "string", "index" : "not_analyzed" }
		}},

		"user_file_copy": {"properties": {
    		"ua_esu": {"type": "date", "format" : "epoch_millis" },
    		"ua_fs": {"type": "long"},
    		"ua_in": {"type": "string", "index" : "not_analyzed" },
    		"ua_ot": {"type": "long"},
    		"ua_un": {"type": "string", "index" : "not_analyzed" }
		}},

		"user_action_logoff": {"properties": {
    		"ua_esu": {"type": "date", "format" : "epoch_millis" },
    		"ua_fs": {"type": "long"},
    		"ua_in": {"type": "string", "index" : "not_analyzed" },
    		"ua_ot": {"type": "long"},
    		"ua_un": {"type": "string", "index" : "not_analyzed" }
		}}
	}
}
	 */
	private static final String MAPPING =
			"{\n" +
			"        \"settings\": {\n" +
			"            \"number_of_shards\": 1,\n" +
			"            \"number_of_replicas\": 1,\n" +
			"            \"index\": {\"analysis\": {\"analyzer\": {\"keylower\": {\n" +
			"                \"tokenizer\": \"keyword\",\n" +
			"                \"filter\": \"lowercase\"\n" +
			"            }}}}\n" +
			"        },\n" +
			"\n" +
			"\"mappings\": \n" +
			"	{\n" +
			"		\"user_action_logon\": {\"properties\": {\n" +
			"    		\"ua_esu\": {\"type\": \"date\", \"format\" : \"epoch_millis\" },\n" +
			"    		\"ua_fs\": {\"type\": \"long\"},\n" +
			"    		\"ua_in\": {\"type\": \"string\", \"index\" : \"not_analyzed\" },\n" +
			"    		\"ua_ot\": {\"type\": \"long\"},\n" +
			"    		\"ua_un\": {\"type\": \"string\", \"index\" : \"not_analyzed\" }\n" +
			"		}},\n" +
			"		\n" +
			"		\"user_file_copy\": {\"properties\": {\n" +
			"    		\"ua_esu\": {\"type\": \"date\", \"format\" : \"epoch_millis\" },\n" +
			"    		\"ua_fs\": {\"type\": \"long\"},\n" +
			"    		\"ua_in\": {\"type\": \"string\", \"index\" : \"not_analyzed\" },\n" +
			"    		\"ua_ot\": {\"type\": \"long\"},\n" +
			"    		\"ua_un\": {\"type\": \"string\", \"index\" : \"not_analyzed\" }\n" +
			"		}},\n" +
			"		\n" +
			"		\"user_action_logoff\": {\"properties\": {\n" +
			"    		\"ua_esu\": {\"type\": \"date\", \"format\" : \"epoch_millis\" },\n" +
			"    		\"ua_fs\": {\"type\": \"long\"},\n" +
			"    		\"ua_in\": {\"type\": \"string\", \"index\" : \"not_analyzed\" },\n" +
			"    		\"ua_ot\": {\"type\": \"long\"},\n" +
			"    		\"ua_un\": {\"type\": \"string\", \"index\" : \"not_analyzed\" }\n" +
			"		}}\n" +
			"	}\n" +
			"}";

	private static final String WORKSPACE_QUERY_1 = "{\n" +
			"                        \"attributes\": [\n" +
			"                            {\n" +
			"                                \"field\": \"dg_time\",\n" +
			"                                \"order\": \"asc\",\n" +
			"                                \"aggregate\": \"day\"\n" +
			"                            },\n" +
			"                            {\n" +
			"                                \"field\": \"ua_in\",\n" +
			"                                \"order\": \"none\",\n" +
			"                                \"aggregate\": \"none\"\n" +
			"                            },\n" +
			"                            {\n" +
			"                                \"field\": \"*\",\n" +
			"                                \"order\": \"asc\",\n" +
			"                                \"aggregate\": \"count\"\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"filter\": {\n" +
			"                            \"operator\": \"AND\",\n" +
			"                            \"filter\": [\n" +
//			"                                {\n" +
//			"                                    \"field\": \"dg_time\",\n" +
//			"                                    \"operator\": \"eq\",\n" +
//			"                                    \"values\": [\n" +
//			"                                        \"last_n_days\",\n" +
//			"                                        \"30\"\n" +
//			"                                    ]\n" +
//			"                                }\n" +
			"                            ]\n" +
			"                        }\n" +
			"                    }";

	/** Test Top N values
	{
	    "detail": false,
	    "size": 0,
	    "count_only": false,
	    "data_types": [
	        "user_file_copy","user_action_logon","user_action_logoff"
	    ],
        "attributes": [
            {
                "field": "dg_time",
                "order": "asc",
                "aggregate": "day"
            },
            {
                "field": "ua_in",
                "order": "none",
                "aggregate": "none"
            },
            {
                "field": "*",
                "order": "asc",
                "aggregate": "count"
            }
        ],
        "filter": {
            "operator": "AND",
            "filter": [


            {
                "field": "ua_in",
                "operator": "in_top",
                "values": [
                	"2"
                ]
            }



            ]
        }
    }
	 */
	private static final String TOP_N =
		"	{\n" +
		"	    \"detail\": false,\n" +
		"	    \"size\": 0,\n" +
		"	    \"count_only\": false,\n" +
		"        \"attributes\": [\n" +
		"            {\n" +
		"                \"field\": \"dg_time\",\n" +
		"                \"order\": \"asc\",\n" +
		"                \"aggregate\": \"day\"\n" +
		"            },\n" +
		"            {\n" +
		"                \"field\": \"ua_in\",\n" +
		"                \"order\": \"none\",\n" +
		"                \"aggregate\": \"none\"\n" +
		"            },\n" +
		"            {\n" +
		"                \"field\": \"*\",\n" +
		"                \"order\": \"asc\",\n" +
		"                \"aggregate\": \"count\"\n" +
		"            }\n" +
		"        ],\n" +
		"        \"filter\": {\n" +
		"            \"operator\": \"AND\",\n" +
		"            \"filter\": [\n" +
		"\n" +
		"\n" +
		"            {\n" +
		"                \"field\": \"ua_in\",\n" +
		"                \"operator\": \"in_top\",\n" +
		"                \"values\": [\n" +
		"                	\"2\"\n" +
		"                ]\n" +
		"            }\n" +
		"\n" +
		"\n" +
		"\n" +
		"            ]\n" +
		"        }\n" +
		"    }";

	/** Test Bottom N values
	{
        "attributes": [
            {
                "field": "dg_time",
                "order": "asc",
                "aggregate": "day"
            },
            {
                "field": "ua_in",
                "order": "none",
                "aggregate": "none"
            },
            {
                "field": "*",
                "order": "asc",
                "aggregate": "count"
            }
        ],
        "filter": {
            "operator": "AND",
            "filter": [


            {
                "field": "dg_user",
                "operator": "eq",
                "values": [
                	"jlehmann"
                ]
            },
            {
                "field": "ua_in",
                "operator": "in_bottom",
                "values": [
                	"2"
                ]
            }



            ]
        }
    }
	 */
	private static final String BOTTOM_N =
			"	{\n" +
			"        \"attributes\": [\n" +
			"            {\n" +
			"                \"field\": \"dg_time\",\n" +
			"                \"order\": \"asc\",\n" +
			"                \"aggregate\": \"day\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"order\": \"none\",\n" +
			"                \"aggregate\": \"none\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"field\": \"*\",\n" +
			"                \"order\": \"asc\",\n" +
			"                \"aggregate\": \"count\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"filter\": {\n" +
			"            \"operator\": \"AND\",\n" +
			"            \"filter\": [\n" +
			"\n" +
			"\n" +
			"            {\n" +
			"                \"field\": \"dg_user\",\n" +
			"                \"operator\": \"eq\",\n" +
			"                \"values\": [\n" +
			"                	\"jlehmann\"\n" +
			"                ]\n" +
			"            },\n" +
			"            {\n" +
			"                \"field\": \"ua_in\",\n" +
			"                \"operator\": \"in_bottom\",\n" +
			"                \"values\": [\n" +
			"                	\"2\"\n" +
			"                ]\n" +
			"            }\n" +
			"\n" +
			"\n" +
			"\n" +
			"            ]\n" +
			"        }\n" +
			"    }";

	/**
{
    "size": 3,
    "attributes": [
        {
            "field": "dg_pn",
            "order": "none",
            "aggregate": "none"
        },
        {
            "field": "*",
            "order": "asc",
            "aggregate": "count"
        }
    ],
    "filter": {
        "operator": "and",
        "filter": [
            {
                "operator": "AND",
                "filter": [{
                    "field": "dg_time",
                    "operator": "eq",
                    "values": ["this_month"]
                }]
            },
            {
                "operator": "and",
                "filter": [{
                    "field": "dg_pn",
                    "operator": "eq",
                    "values": ["limewire.exe"]
                }],
                "enabled": true
            }
        ]
    }
}
	 */
	private static final String AND_QUERY =
			"{\n" +
			"    \"size\": 3,\n" +
			"    \"attributes\": [\n" +
			"        {\n" +
			"            \"field\": \"dg_pn\",\n" +
			"            \"order\": \"none\",\n" +
			"            \"aggregate\": \"none\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }\n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"        \"operator\": \"and\",\n" +
			"        \"filter\": [\n" +
			"            {\n" +
			"                \"operator\": \"AND\",\n" +
			"                \"filter\": [{\n" +
			"                    \"field\": \"dg_time\",\n" +
			"                    \"operator\": \"eq\",\n" +
			"                    \"values\": [\"this_month\"]\n" +
			"                }]\n" +
			"            },\n" +
			"            {\n" +
			"                \"operator\": \"and\",\n" +
			"                \"filter\": [{\n" +
			"                    \"field\": \"dg_pn\",\n" +
			"                    \"operator\": \"eq\",\n" +
			"                    \"values\": [\"limewire.exe\"]\n" +
			"                }],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			"        ]\n" +
			"    }\n" +
			"}";

	/**
{
    "filter": {
        "operator": "and",
        "filter": [
            {
                "operator": "and",
                "filter": []
            },
            {
                "operator": "and",
                "filter": [{
                    "field": "ua_un",
                    "operator": "in_top",
                    "values": ["5"]
                }],
                "enabled": true
            }
        ]
    },
    "attributes": [
        {
            "field": "dg_time",
            "order": "asc",
            "aggregate": "day"
        },
        {
            "field": "dg_user",
            "order": "asc",
            "aggregate": "none"
        },
        {
            "field": "*",
            "order": "asc",
            "aggregate": "count"
        }
    ]
}
	 */
	private static final String WORKSPACE_IN_TOP_TEST =
			"{\n" +
			"    \"filter\": {\n" +
			"        \"operator\": \"and\",\n" +
			"        \"filter\": [\n" +
			"            {\n" +
			"                \"operator\": \"and\",\n" +
			"                \"filter\": []\n" +
			"            },\n" +
			"            {\n" +
			"                \"operator\": \"and\",\n" +
			"                \"filter\": [{\n" +
			"                    \"field\": \"ua_un\",\n" +
			"                    \"operator\": \"in_top\",\n" +
			"                    \"values\": [\"5\"]\n" +
			"                }],\n" +
			"                \"enabled\": true\n" +
			"            }\n" +
			"        ]\n" +
			"    },\n" +
			"    \"attributes\": [\n" +
			"        {\n" +
			"            \"field\": \"dg_time\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"day\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"dg_user\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"none\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }\n" +
			"    ]\n" +
			"}";

	/**
	/**
{
    "filter": {},
    "attributes": [
        {
            "field": "dg_time",
            "order": "asc",
            "aggregate": "day"
        },
        {
            "field": "dg_user",
            "order": "asc",
            "aggregate": "none"
        },
        {
            "field": "*",
            "order": "asc",
            "aggregate": "count"
        }
    ]
}
	 */
	private static final String EMPTY_FILTER_TEST =
			"{\n" +
			"    \"filter\": {},\n" +
			"    \"attributes\": [\n" +
			"        {\n" +
			"            \"field\": \"dg_user\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"none\"\n" +
			"        },\n" +
			"        {\n" +
			"            \"field\": \"*\",\n" +
			"            \"order\": \"asc\",\n" +
			"            \"aggregate\": \"count\"\n" +
			"        }\n" +
			"    ]\n" +
			"}";

	public void queries() throws Exception {


        String username = UUID.randomUUID().toString();
        addUser(username, TENANT_ID);

		//set up the user
		setThreadSecurity(TENANT_ID, username, username);

		//insert the data we'll test with
		populateEventDataBulk(TENANT_ID, "user_action_logon", BULK_TEST_DATA_1);
		populateEventDataBulk(TENANT_ID, "user_file_copy", BULK_TEST_DATA_2);
		populateEventDataBulk(TENANT_ID, "user_action_logoff", BULK_TEST_DATA_3);

		//wait for es refresh
		Thread.sleep(2000L);

		//run context, no datatypes
		JSONObject results = runQuery(username, CONTEXT_NO_DATATYPES);
		assertNotNull(results);

		//run context, no datatypes
		results = runQuery(username, NO_CONTEXT_EMPTY_DATATYPES);
		assertNotNull(results);

		//this one should work..
		results = runQuery(username, AND_QUERY);

		//default workspace queries
		results = runQuery(username, WORKSPACE_QUERY_1);

		//check tags
		HttpResponse response = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/datatypes", HttpResponse.class);

		JSONArray types = new JSONArray(IOUtils.toString(response.getEntity().getContent()));
		for (int x=0; x<types.length(); x++) {
			JSONObject jsonObject = types.getJSONObject(x);
			if (jsonObject.getString("name").equals("incident")) {
				assertTrue(jsonObject.has("tags"));
				JSONArray tags = jsonObject.getJSONArray("tags");
				assertEquals(1, tags.length());
				assertEquals("incident", tags.getString(0));
			}
		}

		//default workspace queries
		results = runQuery(username, EMPTY_FILTER_TEST);
		assertEquals("result size wrong", 1, results.getJSONArray("data").length());
		assertEquals("event count wrong", 37, results.getJSONArray("data").getJSONArray(0).getInt(1));

		//look for 1 bad file (badfile1.exe)
		results = runQuery(username, QUERY_BAD_FILE_1);

		//check some things
		assertEquals("result field size not correct", 3, results.getJSONArray("fields").length());
		assertTrue("results empty", results.getJSONArray("data").length() > 0);
		assertEquals("results row width doesn't match field count", 3, results.getJSONArray("data").getJSONArray(0).length());
		assertEquals("result value wrong", 3, results.getJSONArray("data").getJSONArray(0).getInt(0));
		assertEquals("result value wrong", "badFile1.exe", results.getJSONArray("data").getJSONArray(0).getString(1));
		assertEquals("result value wrong", "jlehmann", results.getJSONArray("data").getJSONArray(0).getString(2));

		results = runQuery(username, TOP_N);
		assertEquals("top n result size wrong", 2, results.getJSONArray("data").length());
		assertEquals("top n doc count wrong", 7, results.getJSONArray("data").getJSONArray(0).getInt(2));
		assertEquals("top n doc count wrong", 9, results.getJSONArray("data").getJSONArray(1).getInt(2));

		results = runQuery(username, BOTTOM_N);
		assertEquals("bottom n result size wrong", 2, results.getJSONArray("data").length());
		assertEquals("bottom n doc count wrong", 1, results.getJSONArray("data").getJSONArray(0).getInt(2));
		assertEquals("bottom n doc count wrong", 1, results.getJSONArray("data").getJSONArray(1).getInt(2));

		//look for files starting with "bad"
		results = runQuery(username, QUERY_STARTS_WITH_BAD_ORDER_BY_COUNT);

		//check some things
		assertEquals("result field size not correct", 3, results.getJSONArray("fields").length());
		assertTrue("results empty", results.getJSONArray("data").length() > 0);
		assertEquals("results row width doesn't match field count", 3, results.getJSONArray("data").getJSONArray(0).length());
		assertEquals("sorted count value wrong", 9, results.getJSONArray("data").getJSONArray(0).getInt(0));

		//look for files containing "File"
		results = runQuery(username, QUERY_CONTAINS_FILE);

		//check some things
		assertEquals("result field size not correct", 3, results.getJSONArray("fields").length());
		assertTrue("results empty", results.getJSONArray("data").length() > 0);
		assertEquals("results row width doesn't match field count", 3, results.getJSONArray("data").getJSONArray(0).length());
		assertTrue("result value wrong", results.getJSONArray("data").getJSONArray(0).getInt(0) > 0);

		//look for files containing "File"
		results = runQuery(username, QUERY_COUNT_BY_USER);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 2, results.getJSONArray("fields").length());
		assertTrue("results empty", results.getJSONArray("data").length() > 0);
		assertEquals("results row width doesn't match field count", results.getJSONArray("fields").length(), results.getJSONArray("data").getJSONArray(0).length());
		assertEquals("result size wrong", bulkDataSize(), results.getJSONArray("data").getJSONArray(0).getInt(1));

		//look for count of each user
		results = runQuery(username, QUERY_COUNT_BY_USER);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 2, results.getJSONArray("fields").length());
		assertTrue("results empty", results.getJSONArray("data").length() > 0);
		assertEquals("results row width doesn't match field count", results.getJSONArray("fields").length(), results.getJSONArray("data").getJSONArray(0).length());
		assertEquals("result size wrong", bulkDataSize(), results.getJSONArray("data").getJSONArray(0).getInt(1));

		//look for files containing "file" lowercase
		results = runQuery(username, QUERY_CONTAINS_FILE_LOWERCASE);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 3, results.getJSONArray("fields").length());
		assertEquals("result size wrong", 0, results.getJSONArray("data").length());

		//time trend query
		results = runQuery(username, QUERY_TIME_TREND_BY_MINUTE);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 2, results.getJSONArray("fields").length());
		assertEquals("result size wrong", 41, results.getJSONArray("data").length()); //from 9:00 -> 9:40, the last entry
		assertEquals("value for 9:15 wrong", 17, results.getJSONArray("data").getJSONArray(15).getInt(1));

		//time trend query
		results = runQuery(username, QUERY_TIME_TREND_BY_MINUTE_BY_USER_BY_FILE);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 4, results.getJSONArray("fields").length());
		assertEquals("result size wrong", 10, results.getJSONArray("data").length()); //from 9:00 -> 9:40, the last entry
		assertEquals("value wrong", 6, results.getJSONArray("data").getJSONArray(4).getInt(3));

		//time trend query
		results = runQuery(username, QUERY_TIME_TREND_BY_HOUR_BY_USER_BY_FILE);

		//check some things - grouped on jlehmann, the bucket size should be equal to the doc count of our bulk insert
		assertEquals("result field size not correct", 3, results.getJSONArray("fields").length());
		assertEquals("result size wrong", 2, results.getJSONArray("data").length()); //from 9:00 -> 9:40, the last entry
		assertEquals("value for 8am wrong", 8, results.getJSONArray("data").getJSONArray(0).getInt(2));
		assertEquals("value for 9am wrong", 29, results.getJSONArray("data").getJSONArray(1).getInt(2));

		//now check generally bad requests

		//add a bunch of events
		ESGenerator gen = new ESGenerator(false);
		File out = File.createTempFile("es_load", ".json");
		gen.generate(1000, out);
		populateEventDataBulk(TENANT_ID, null, IOUtils.readLines(new FileReader(out)));
		out.delete();

		//now run a query against this larger data set
		results = runQuery(username, TEST_QUERY);
		assertTrue("results empty", results.getJSONArray("data").length() > 0);

		//guid in the wrong form should be bad request
		response = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/abc123/status", HttpResponse.class);
		assertEquals("expected 400 for status with bad guid", HttpStatus.BAD_REQUEST_400, response.getStatusLine().getStatusCode());

		//guid in the wrong form should be bad request
		response = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/abc123/results", HttpResponse.class);
		assertEquals("expected 400 for results with bad guid", HttpStatus.BAD_REQUEST_400, response.getStatusLine().getStatusCode());

		//status for missing guid should be 404
		response = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/" + UUID.randomUUID() + "/status", HttpResponse.class);
		assertEquals("expected 404 for missing status", HttpStatus.NOT_FOUND_404, response.getStatusLine().getStatusCode());

		//results for missing guid should be 404
		response = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/" + UUID.randomUUID() + "/results", HttpResponse.class);
		assertEquals("expected 404 for missing results", HttpStatus.NOT_FOUND_404, response.getStatusLine().getStatusCode());

	}

	public void testSorting() throws Exception {

		// Sorting attributes only work with detail queries
		final String sortedByUsername = "{ \"detail\": true, \"size\": 10, \"count_only\": false, \"data_types\": [\"user_file_copy\" ], \"attributes\": [ {\"field\": \"ua_in\", \"order\": \"desc\"} ], \"sort_attributes\": [ { \"field\": \"ua_in\", \"order\": \"desc\" } ] } ";

        String testUser = UUID.randomUUID().toString();
        addUser(testUser, TENANT_ID);

		elasticSearchService.deleteIndex("*");

		//create the index
		createIndex(TENANT_ID, ESKnownIndices.EVENTS_ES, MAPPING);

		//insert the data we'll test with
		final List<String> testData = Arrays.asList(
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:15:00.000 -0500").getTime()+", \"ua_un\" : \"aBadUser\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 10000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"cBadUser\", \"ua_in\" : \"badFile3.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"bBadUser\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 20000 }"
		);
		populateEventDataBulk(TENANT_ID, "user_file_copy", testData);

		//this one should work..
		JSONObject results = runQuery(testUser, sortedByUsername);
		final JSONArray data = results.getJSONArray("data");
		assertEquals("badFile3.exe", data.getJSONArray(0).getString(0));
		assertEquals("badFile2.exe", data.getJSONArray(1).getString(0));
		assertEquals("badFile1.exe", data.getJSONArray(2).getString(0));

	}

	public void testCountDistinct() throws Exception {

		final String distinctFiles = "{ \"detail\": false, \"count_only\": false, \"data_types\": [\"user_file_copy\" ], \"attributes\": [ {\"field\": \"dg_user\"}, {\"field\": \"ua_in\", \"order\": \"desc\", \"aggregate\": \"count_distinct\"} ] } ";

		String testUser = UUID.randomUUID().toString();
		addUser(testUser, TENANT_ID);

		elasticSearchService.deleteIndex("*");

		//create the index
		createIndex(TENANT_ID, ESKnownIndices.EVENTS_ES, MAPPING);

		//insert a bunch of bad file copies
		final List<String> testData = Arrays.asList(

			// badUser1 has two different files
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 10000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 30000 }",

			// badUser2 has three different files
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile3.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 20000 }"
		);
		populateEventDataBulk(TENANT_ID, "user_file_copy", testData);

		//this one should work..
		JSONObject results = runQuery(testUser, distinctFiles);
		final JSONArray data = results.getJSONArray("data");
		assertEquals("badUser2", data.getJSONArray(0).getString(0));
		assertEquals(3, data.getJSONArray(0).getInt(1));
		assertEquals("badUser1", data.getJSONArray(1).getString(0));
		assertEquals(2, data.getJSONArray(1).getInt(1));

	}

	public void testCountAndCountDistinct() throws Exception {

		final String distinctFiles = "{ \"detail\": false, \"count_only\": false, \"data_types\": [\"user_file_copy\" ], " +
			"	\"attributes\": [ " +
			"		{\"field\": \"dg_user\", \"aggregate\": \"count\"}, " +
			"		{\"field\": \"dg_file_size\", \"aggregate\": \"avg\"}, " +
			"		{\"field\": \"ua_in\", \"order\": \"desc\", \"aggregate\": \"count_distinct\"} " +
			"	] " +
			"} ";

		String testUser = UUID.randomUUID().toString();
		addUser(testUser, TENANT_ID);

		elasticSearchService.deleteIndex("*");

		//create the index
		createIndex(TENANT_ID, ESKnownIndices.EVENTS_ES, MAPPING);

		//insert a bunch of bad file copies
		final List<String> testData = Arrays.asList(

			// badUser1 has two different files
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 10000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser1\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 30000 }",

			// badUser2 has three different files
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile3.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2016-02-18 09:15:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile2.exe\", \"uad_fs\" : 30000 }",
			"{ \"dg_utype\": \"user_file_copy\", \"ua_esu\" : "+JSONUtils.JSON_DATE_FORMAT.parse("2015-09-01 08:30:00.000 -0500").getTime()+", \"ua_un\" : \"badUser2\", \"ua_in\" : \"badFile1.exe\", \"uad_fs\" : 20000 }"
		);
		populateEventDataBulk(TENANT_ID, "user_file_copy", testData);

		//this one should work..
		JSONObject results = runQuery(testUser, distinctFiles);
		final JSONArray data = results.getJSONArray("data");
		assertEquals(6, data.getJSONArray(0).getInt(0));
		assertEquals(25_000L, data.getJSONArray(0).getLong(1));
		assertEquals(3, data.getJSONArray(0).getInt(2));

	}


	protected JSONObject runQuery(String username, String json) throws Exception {
		Thread.sleep(1000);

		JSONObject runner = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query", new JSONObject(json), JSONObject.class);

		//get the guid from the response
		String guid = runner.getString("guid");
		assertNotNull("query guid null after execution", guid);

		//ask for status
		String status = this.<JSONObject>authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/" + guid + "/status", JSONObject.class).getString("state");

		//keep track of how long this takes
		long start = System.currentTimeMillis();

		//poll until we're done
		while (!"complete".equalsIgnoreCase(status)) {

			//don't thrash too much
			Thread.sleep(100L);

			//wait ten seconds, that should be way more than enough
			assertTrue("waited too long for query", System.currentTimeMillis() - start < 10000L);

			//check the status again
			JSONObject statusObj = this.<JSONObject>authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/" + guid + "/status", JSONObject.class);
			status = statusObj.getString("state");

			if ("error".equals(status)) {
				fail("error during query: " + statusObj.toString());
			}

		}

		//shouldn't ever be true but just to be sure
		assertEquals("query never completed", "complete", status);

		//now get the results
		return authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/query/" + guid + "/results", JSONObject.class);

	}

}