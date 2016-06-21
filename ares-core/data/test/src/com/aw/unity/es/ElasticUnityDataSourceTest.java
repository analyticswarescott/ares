package com.aw.unity.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import com.aw.common.util.es.ESKnownIndices;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.exceptions.InitializationException;
import com.aw.common.inject.TestProvider;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.unity.DataType;
import com.aw.unity.TestSecColumn;
import com.aw.unity.TestSecRow;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.DataSourceException;
import com.aw.util.Statics;

public class ElasticUnityDataSourceTest {

	/**
    	{
		    "name": "es",
		    "type": "com.aw.unity.es.UnityDataSourceElastic",
		    "row_security_class": "com.aw.unity.security.SecRowDG",
		    "col_security_class": "com.aw.unity.security.SecColumnDG",
		    "role_resolver_class": "com.aw.unity.security.DataSecurityResolverDG",
	        "host": "{$UNITY_ES_HOST}",
	        "port": 9200,
		    "index_map" : {
		    	"data_type_x" : "index_y"
		    },
		    "default_index" : "{$TENANT_ID}_events"
		}
	 */
	public static final String JSON =
			"  	{\n" +
			"	    \"name\": \"es\",\n" +
			"	    \"type\": \"com.aw.unity.es.UnityDataSourceElastic\",\n" +
			"	    \"row_security_class\": \"com.aw.unity.TestSecRow\",\n" +
			"	    \"column_security_class\": \"com.aw.unity.TestSecColumn\",\n" +
			"	    \"role_resolver_class\": \"com.aw.unity.TestRoleResolver\",\n" +
			"        \"host\": \"localhost\",\n" +
			"        \"port\": 9200,\n" +
			"	    \"index_map\" : {\n" +
			"	    	\"data_type_x\" : \"events_es\"\n" +
			"	    },\n" +
			"	    \"default_index\" : \"events_es\"\n" +
			"	}\n";

	ElasticUnityDataSource dataSource;
	Tenant tenant;
	PlatformMgr mgr;
	Platform platform;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

		SecurityUtil.setThreadSystemAccess();

		//create test object
		dataSource = spy(new ElasticUnityDataSource());

		//create mocks
		tenant = mock(Tenant.class);

		//wire up mocks
		when(tenant.getTenantID()).thenReturn("1");

	}

	public ElasticUnityDataSource newTestDataSource() throws Exception {

		mgr = new PlatformMgr();
		platform = spy(TestPlatform.build(NodeRole.SPARK_MASTER));
		mgr.setPlatform(platform);

		ElasticUnityDataSource dataSource = JSONUtils.objectFromString(JSON, ElasticUnityDataSource.class);

		mgr = spy(mgr);

		//inject platformMgr - TODO: make this a utility call
		dataSource = spy(dataSource);

		return dataSource;

	}

	/**
	 * Make sure we can load from json
	 *
	 * @throws Exception
	 */
	@Test
	public void jsonMapper() throws Exception {

		UnityInstance instance = new TestUnityInstance();
		ElasticUnityDataSource dataSource = newTestDataSource();
		dataSource.initialize(instance, new TestProvider<>(new TestPlatform()));

		assertEquals("column security type incorrect", TestSecColumn.class, dataSource.getColumnSecurity().getClass());
		assertEquals("row security type incorrect", TestSecRow.class, dataSource.getRowSecurity().getClass());
		assertTrue("index map not correct", dataSource.getIndexMap().containsKey("data_type_x"));

	}

	/**
{
	"bool": {
		"should": [{
			"type": {
				"value": "test_type"
			}
		}, {
			"type": {
				"value": "test_subtype"
			}
		}, {
			"type": {
				"value": "test_type2"
			}
		}, {
			"type": {
				"value": "test_type3"
			}
		}]
	}
}
	 */

	private static final String EXPECTED_QB =
			"{\n" +
			"	\"bool\": {\n" +
			"		\"should\": [{\n" +
			"			\"type\": {\n" +
			"				\"value\": \"test_type\"\n" +
			"			}\n" +
			"		}, {\n" +
			"			\"type\": {\n" +
			"				\"value\": \"test_subtype\"\n" +
			"			}\n" +
			"		}, {\n" +
			"			\"type\": {\n" +
			"				\"value\": \"test_type2\"\n" +
			"			}\n" +
			"		}, {\n" +
			"			\"type\": {\n" +
			"				\"value\": \"test_type3\"\n" +
			"			}\n" +
			"		}]\n" +
			"	}\n" +
			"}";

	@Test
	public void searchForDatatypes() throws Exception {

		UnityInstance instance = new TestUnityInstance();
		ElasticUnityDataSource dataSource = newTestDataSource();
		dataSource.initialize(instance, new TestProvider<>(new TestPlatform()));

		//get known datatypes - build a query from that for test
   	 	DataType[] datatypes = instance.getMetadata().getDataTypeRepository().getDataTypes();
		QueryBuilder qb = dataSource.searchForDatatypes(datatypes);
		assertTrue("query builder for data types wrong (found: " + qb.toString() + ")", JSONUtils.jsonEquals(EXPECTED_QB, qb.toString()));

	}

	@Test
	public void getEarliest_MONTHS() throws Exception {

		System.out.println("dataSource: " + dataSource);
		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.MONTHS);
		doReturn(new String[] {
			"status_1_2016_02",
			"status_1_2015_12",
			"status_1_2016_01"
		}).when(dataSource).getIndicesFromClient();

		Instant earliest = 	dataSource.getEarliest(tenant, ESKnownIndices.STATUS);

		assertEquals(LocalDate.of(2015, 12, 1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant(), earliest);

	}

	@Test
	public void getLatest_MONTHS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.MONTHS);
		doReturn(new String[] {
			"status_1_2016_02",
			"status_1_2015_12",
			"status_1_2016_01"
		}).when(dataSource).getIndicesFromClient();

		Instant earliest = 	dataSource.getLatest(tenant, ESKnownIndices.STATUS);

		assertEquals(LocalDate.of(2016, 03, 1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant(), earliest);

	}

	@Test(expected=InitializationException.class)
	public void initialize() throws Exception {

		UnityInstance instance = new TestUnityInstance();
		ElasticUnityDataSource dataSource = newTestDataSource();
		doThrow(new RuntimeException()).when(platform).getNodes(any());
		dataSource.initialize(instance, new TestProvider<>(platform));

	}

	@Test(expected=DataSourceException.class)
	public void getEarliest_null() throws Exception {

		//handle null tenant with data source exception
		dataSource.getEarliest(null, ESKnownIndices.STATUS);

	}

	@Test(expected=DataSourceException.class)
	public void getLatest_null() throws Exception {

		//handle null tenant with data source exception
		dataSource.getLatest(null, ESKnownIndices.STATUS);

	}

	@Test
	public void getIndex() throws Exception {

		UnityInstance instance = new TestUnityInstance();
		ElasticUnityDataSource dataSource = newTestDataSource();
		assertEquals(ESKnownIndices.EVENTS_ES, dataSource.getIndex(instance.getMetadata().getDataTypeRepository().getDataTypes()[0]));

	}

	@Test
	public void searchForDatatypes_null() throws Exception {

		assertNull(dataSource.searchForDatatypes(null));

	}

	@Test
	public void searchForDatatypes_1() throws Exception {

		UnityInstance instance = new TestUnityInstance();
		ElasticUnityDataSource dataSource = newTestDataSource();
		QueryBuilder builder = dataSource.searchForDatatypes(instance.getMetadata().getDataTypeRepository().getDataTypes()[0]);

		assertEquals("{\n" +
			"  \"type\" : {\n" +
			"    \"value\" : \"test_type\"\n" +
			"  }\n" +
			"}", builder.toString());

	}

	@Test
	public void getDefaultIndex() throws Exception {

		ElasticUnityDataSource dataSource = new ElasticUnityDataSource();
		dataSource.setDefaultIndex(ESKnownIndices.EVENTS_ES);
		assertSame(ESKnownIndices.EVENTS_ES, dataSource.getDefaultIndex());

	}

	@Test
	public void getClient() throws Exception {

		ElasticUnityDataSource dataSource = new ElasticUnityDataSource();
		TransportClient client = mock(TransportClient.class);
		dataSource.setClient(client);
		assertSame(client, dataSource.getClient());

	}
}
