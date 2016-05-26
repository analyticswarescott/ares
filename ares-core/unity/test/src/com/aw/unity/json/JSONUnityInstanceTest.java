package com.aw.unity.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.unity.DataType;
import com.aw.unity.TestDataSource;
import com.aw.unity.TestDataType;
import com.aw.unity.TestDataTypeRepository;
import com.aw.unity.TestDocumentHandler;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.QueryAttribute.Order;

public class JSONUnityInstanceTest {

	/**
{
    "type" : "com.aw.unity.json.JSONUnityInstance",
    "tenant_id": "0",
    "default_data_source" : "es",
	"data_sources" : [
		...
    ]
}
	 */
	public static final String JSON =
			"{\n" +
			"    \"type\" : \"com.aw.unity.json.JSONUnityInstance\",\n" +
			"    \"tenant_id\": \"test_tenant\",\n" +
			"    \"default_data_source\" : \"test\",\n" +
			"	\"data_sources\" : [\n" +
			"  	{\n" +
			"	    \"name\": \"test\",\n" +
			"	    \"type\": \"com.aw.unity.TestDataSource\",\n" +
			"	    \"row_security_class\": \"com.aw.unity.TestSecRow\",\n" +
			"	    \"column_security_class\": \"com.aw.unity.TestSecColumn\",\n" +
			"	    \"role_resolver_class\": \"com.aw.unity.TestRoleResolver\",\n" +
			"        \"host\": \"localhost\",\n" +
			"        \"port\": 9200,\n" +
			"	    \"index_map\" : {\n" +
			"	    	\"data_type_x\" : \"index_y\"\n" +
			"	    },\n" +
			"	    \"default_index\" : \"0_events\"\n" +
			"	}\n" +
			"    ]\n" +
			"    \n" +
			"}";

	public static final String FILTER_JSON =
			"{\n" +
			"  \"operator\" : \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \"field_int\",\n" +
			"          \"operator\": \"EQ\",\n" +
			"          \"not\": true,\n" +
			"          \"values\": [1],\n" +
			"          \"enabled\": \"true\"\n" +
			"        },\n" +
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
			"            }\n" +
			"          ]\n" +
			"}";

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void creation() throws Exception {

		JSONUnityInstance instance = new JSONUnityInstance();
		instance.setDataTypeRepository(new TestDataTypeRepository());

		instance.initialize(new JSONObject(JSON), new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		assertNotNull("data sources empty", instance.getDataSources());
		assertEquals("data source array size wrong", 1, instance.getDataSources().length);
		assertEquals("data source type wrong", TestDataSource.class, instance.getDataSources()[0].getClass());

	}

	@Test
	public void execute() throws Exception {

		//create the test instance
		JSONUnityInstance instance = new JSONUnityInstance();
		instance.setDataTypeRepository(new TestDataTypeRepository());
		instance.initialize(new JSONObject(JSON),new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		//run a query
		Query query = new Query();

		query.setAttributes(new QueryAttribute[] {
			new QueryAttribute(instance.getFieldRepository().getField("field_int"), Order.NONE, Aggregate.NONE)
		});

		query.setCountOnly(false);
		query.setDataTypes(new DataType[] { instance.getDataTypeRepository().getDataTypes()[0] });
		query.setDetail(false);
		query.setFilter(instance.getFilterFactory().newFilter(""));
		query.setName("test_query");
		query.setSize(10);

		//TODO: test OData
		//assertNotNull("query response cannot be null", instance.execute(query, ResponseFormat.ODATA));

		//test default data source
		query.setDataTypes(null);
		assertNotNull("query should have used default data source", instance.execute(query));

		try {

			//make a copy of the query
			query = new Query(query);

			//test invalid data source
			query.setDataTypes(new DataType[] { new TestDataType() { public String getDataSource() { return "invalid_data_source"; } } });
			instance.execute(query);
			fail("missing data source should have failed with exception");

		} catch (InvalidQueryException e) {
			//expected
		}

	}

	@Test
	public void newFilter() throws Exception {

		//create the test instance
		JSONUnityInstance instance = new JSONUnityInstance();
		instance.setDataTypeRepository(new TestDataTypeRepository());
		instance.initialize(new JSONObject(JSON),new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		//create a filter from json - make sure there are no errors other than that we're good (filter accuracy will be tested elsewhere)
		assertNotNull("filter not created", instance.newFilter(FILTER_JSON));

	}

	@Test
	public void getTenantID() throws Exception {

		//create the test instance
		JSONUnityInstance instance = new JSONUnityInstance();
		instance.setDataTypeRepository(new TestDataTypeRepository());
		instance.initialize(new JSONObject(JSON),new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		//create a filter from json - make sure there are no errors other than that we're good (filter accuracy will be tested elsewhere)
		assertNotNull("filter not created", instance.newFilter(FILTER_JSON));

	}

	@Test
	public void getDataType() throws Exception {

		//create the test instance
		JSONUnityInstance instance = new JSONUnityInstance();
		instance.setDataTypeRepository(new TestDataTypeRepository());
		instance.initialize(new JSONObject(JSON),new TestDocumentHandler(), new TestProvider<>(new TestPlatform()));

		//make sure we can get the test data type
		assertEquals("getDataType() returned wrong data type for test_type", instance.getDataTypeRepository().getDataTypes()[0], instance.getDataType("test_type"));

	}

}
