package com.aw.unity.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.tenant.Tenant;
import com.aw.common.util.es.ESKnownIndices;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.QueryFactory;
import com.aw.unity.QueryFactory.QueryFormat;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityUnitTest;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.json.JSONUnityInstanceTest;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.TimeRange;
import com.aw.unity.query.constraints.AbstractFilterConstraint;
import com.aw.unity.query.constraints.BetweenConstraint;
import com.aw.unity.query.constraints.ContainsConstraint;
import com.aw.unity.query.constraints.ExistsConstraint;
import com.aw.unity.query.constraints.GteConstraint;
import com.aw.unity.query.constraints.InConstraint;
import com.aw.unity.query.constraints.LteConstraint;
import com.aw.unity.query.constraints.MatchesConstraint;
import com.aw.unity.query.constraints.StartsWithConstraint;

public class ElasticUnityRunnerTest extends UnityUnitTest {


	private static String EXPECTED_FILTER =
			"{\n" +
			"	\"bool\": {\n" +
			"		\"must\": [{\n" +
			"			\"not\": {\n" +
			"				\"query\": {\n" +
			"					\"terms\": {\n" +
			"						\"field1\": [1]\n" +
			"					}\n" +
			"				}\n" +
			"			}\n" +
			"		}, {\n" +
			"			\"bool\": {\n" +
			"				\"should\": [{\n" +
			"					\"terms\": {\n" +
			"						\"field4\": [\"1.2.3.4\"]\n" +
			"					}\n" +
			"				}, {\n" +
			"					\"terms\": {\n" +
			"						\"field7\": [\"ab:ab:ab:ab:ab:ab\"]\n" +
			"					}\n" +
			"				}]\n" +
			"			}\n" +
			"		}]\n" +
			"	}\n" +
			"}";

	private ElasticUnityRunner runner;

	private TestUnityInstance instance;

	@Before
	public void setUp() throws Exception {

		//use our test unity instance
		instance = new TestUnityInstance();

		// Configure the elastic unity runner to use our test instance
		runner = spy(new ElasticUnityRunner());
		doReturn("1").when(runner).getTenantID();
		doReturn("user").when(runner).getUserID();

		//set up a mock data source
		ElasticUnityDataSource dataSource = spy(new ElasticUnityDataSource());
		dataSource.setDefaultIndex(ESKnownIndices.EVENTS_ES);
		doReturn("1").when(dataSource).getTenantID();
		doReturn(null).when(dataSource).getIndex(any(DataType.class));
		doReturn(instance).when(dataSource).getUnity();
		runner.initialize(dataSource, new Query());

	}

	@Test
	public void toFilterBuilder() throws Exception {

		//test complex filter to filter builder
		QueryBuilder queryBuilder = runner.toQueryBuilder(instance.newFilter(JSONUnityInstanceTest.FILTER_JSON));

		JSONObject object = new JSONObject(queryBuilder.toString());
		assertEquals("filter builder incorrect", new JSONObject(EXPECTED_FILTER).toString(), object.toString());

	}

	private static String QUERY_JSON_DAY =
			"{\n" +
			"    \"detail\": false,\n" +
			"    \"size\": 25,\n" +
			"    \"time_zone\": \"EST\",\n" +
			"    \"from\" : 0,\n" +
			"    \"count_only\": false,\n" +
			"    \"data_types\" : [\n" +
			"        \"test_type\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n" +
			"      {\n" +
			"        \"field\": \"field_timestamp\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"day\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_string\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"*\",\n" +
			"        \"aggregate\": \"count\"\n" +
			"      }\n" +
			" \n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"      \"operator\": \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \"field_timestamp\",\n" +
			"          \"operator\": \"eq\",\n" +
			"          \"values\": [\n" +
			"            \"last_n_hours\",\n" +
			"            \"24\"\n" +
			"          ]\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"}";
	@Test
	public void buildSearchRequest_day() throws Exception {
		String expectedSr =
				"{\n" +
				"	\"size\": 0,\n" +
				"	\"query\": {\n" +
				"		\"range\": {\n" +
				"			\"field3\": {\n" +
				"				\"from\": \"\",\n" +
				"				\"to\": \"now\",\n" +
				"				\"include_lower\": true,\n" +
				"				\"include_upper\": true\n" +
				"			}\n" +
				"		}\n" +
				"	},\n" +
				"	\"aggregations\": {\n" +
				"		\"field_timestamp:DAY\": {\n" +
				"			\"date_histogram\": {\n" +
				"				\"field\": \"field3\",\n" +
				"				\"interval\": \"1d\",\n" +
				"				\"time_zone\": \"EST\",\n" +
				"				\"format\": \"yyyy-MM-dd\"\n" +
				"			},\n" +
				"			\"aggregations\": {\n" +
				"				\"field_string:NONE\": {\n" +
				"					\"terms\": {\n" +
				"						\"field\": \"field2\",\n" +
				"						\"size\": 0\n" +
				"					}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}";

		setThreadSecurity("1", "test_userid", "test_username");

		//build the query
		Query query = QueryFactory.newQuery(QUERY_JSON_DAY, QueryFormat.JSON, instance);

		//initialize the runner
		ElasticUnityRunner runner = new ElasticUnityRunner();

		runner.initialize(new ElasticUnityDataSource() {
			@Override
			public UnityInstance getUnity() {
				return ElasticUnityRunnerTest.this.instance;
			}
			@Override
			public Client getClient() {
				return new TestClient();
			}
		}, query);

		SearchRequestBuilder sr = runner.buildSearchRequest(query);
		JSONObject srJson = new JSONObject(sr.toString());

		//clear out date
		srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").put("from", "");

		//make sure the search request is as expected
		assertEquals("search request not correct", new JSONObject(expectedSr).toString(), srJson.toString());

	}

	private static String QUERY_JSON_HOUR =
			"{\n" +
			"    \"detail\": false,\n" +
			"    \"size\": 25,\n" +
			"    \"from\" : 0,\n" +
			"    \"time_zone\": \"EST\",\n" +
			"    \"count_only\": false,\n" +
			"    \"data_types\" : [\n" +
			"        \"test_type\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n" +
			"      {\n" +
			"        \"field\": \"field_timestamp\",\n" +
			"        \"order\": \"desc\",\n" +
			"        \"aggregate\": \"hour\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_string\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"*\",\n" +
			"        \"aggregate\": \"count\"\n" +
			"      }\n" +
			" \n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"      \"operator\": \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \"field_timestamp\",\n" +
			"          \"operator\": \"in\",\n" +
			"          \"values\": [\n" +
			"            \"last_week\"\n" +
			"          ]\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"}";

	@Test
	public void buildSearchRequest_hour() throws Exception {
		String expectedSr =
				"{\n" +
				"	\"size\": 0,\n" +
				"	\"query\": {\n" +
				"		\"range\": {\n" +
				"			\"field3\": {\n" +
				"				\"from\": \"\",\n" +
				"				\"to\": \"\",\n" +
				"				\"include_lower\": true,\n" +
				"				\"include_upper\": false\n" +
				"			}\n" +
				"		}\n" +
				"	},\n" +
				"	\"aggregations\": {\n" +
				"		\"field_timestamp:HOUR\": {\n" +
				"			\"date_histogram\": {\n" +
				"				\"field\": \"field3\",\n" +
				"				\"interval\": \"1h\",\n" +
				"				\"time_zone\": \"EST\",\n" +
				"				\"format\": \"yyyy-MM-dd hh a\"\n" +
				"			},\n" +
				"			\"aggregations\": {\n" +
				"				\"field_string:NONE\": {\n" +
				"					\"terms\": {\n" +
				"						\"field\": \"field2\",\n" +
				"						\"size\": 0\n" +
				"					}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}";

		//use our test unity instance
		TestUnityInstance instance = new TestUnityInstance();

		setThreadSecurity("1", "test_userid", "test_username");

		//build the query
		Query query = QueryFactory.newQuery(QUERY_JSON_HOUR, QueryFormat.JSON, instance);

		//initialize the runner
		ElasticUnityRunner runner = new ElasticUnityRunner();

		runner.initialize(new ElasticUnityDataSource() {
			@Override
			public UnityInstance getUnity() {
				return ElasticUnityRunnerTest.this.instance;
			}

			@Override
			public Client getClient() {
				return new TestClient();
			}
		}, query);

		SearchRequestBuilder sr = runner.buildSearchRequest(query);
		JSONObject srJson = new JSONObject(sr.toString());

		//clear out date - TODO: insert values we'd expect to the expected value
		srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").put("from", "");
		srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").put("to", "");

		//make sure the search request is as expected
		assertEquals("search request not correct", new JSONObject(expectedSr).toString(), srJson.toString());

	}

	private static String QUERY_JSON_MINUTE_LTE =
			"{\n" +
			"    \"detail\": false,\n" +
			"    \"size\": 25,\n" +
			"    \"from\" : 0,\n" +
			"    \"count_only\": false,\n" +
			"    \"time_zone\": \"EST\",\n" +
			"    \"data_types\" : [\n" +
			"        \"test_type\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n" +
			"      {\n" +
			"        \"field\": \"field_timestamp\",\n" +
			"        \"order\": \"desc\",\n" +
			"        \"aggregate\": \"hour\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_string\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"*\",\n" +
			"        \"aggregate\": \"count\"\n" +
			"      }\n" +
			" \n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"      \"operator\": \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \"field_timestamp\",\n" +
			"          \"operator\": \"lte\",\n" +
			"          \"values\": [\n" +
			"            \"2015-11-01 00:00:00.000 EST\"\n" +
			"          ]\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"}";

	private static String QUERY_JSON_MINUTE_LTE_DETAIL =
			"{\n" +
			"    \"detail\": true,\n" +
			"    \"size\": 0,\n" +
			"    \"count_only\": false,\n" +
			"    \"time_zone\": \"EST\",\n" +
			"    \"data_types\" : [\n" +
			"        \"test_type\"\n" +
			"    ],\n" +
			"    \"attributes\": [\n" +
			"      {\n" +
			"        \"field\": \"field_timestamp\",\n" +
			"        \"order\": \"desc\",\n" +
			"        \"aggregate\": \"hour\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"field_string\",\n" +
			"        \"order\": \"asc\",\n" +
			"        \"aggregate\": \"none\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"field\": \"*\",\n" +
			"        \"aggregate\": \"count\"\n" +
			"      }\n" +
			" \n" +
			"    ],\n" +
			"    \"filter\": {\n" +
			"      \"operator\": \"AND\",\n" +
			"      \"filter\": [\n" +
			"        {\n" +
			"          \"field\": \"field_timestamp\",\n" +
			"          \"operator\": \"lte\",\n" +
			"          \"values\": [\n" +
			"            \"2015-11-01 00:00:00.000 EST\"\n" +
			"          ]\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"}";

	@Test
	public void buildSearchRequest_minute_lte() throws Exception {
		JSONObject testQuery = new JSONObject(QUERY_JSON_MINUTE_LTE);
		JSONObject expectedSr = new JSONObject(
				"{\n" +
				"    \"size\": 0,\n" +
				"    \"query\": {\"bool\": {\"must\": {\"range\": {\"field_timestamp\": {\n" +
				"        \"from\": null,\n" +
				"        \"to\": \"2015-11-01 01:00:00.000 -0400\",\n" +
				"        \"include_lower\": true,\n" +
				"        \"include_upper\": true\n" +
				"    }}}}},\n" +
				"    \"aggregations\": {\"field_timestamp:HOUR\": {\n" +
				"        \"date_histogram\": {\n" +
				"            \"field\": \"field_timestamp\",\n" +
				"            \"interval\": \"1h\",\n" +
				"            \"time_zone\": \"EST\",\n" +
				"            \"format\": \"yyyy-MM-dd hh a\"\n" +
				"        },\n" +
				"        \"aggregations\": {\"field_string:NONE\": {\"terms\": {\n" +
				"            \"field\": \"field_string\",\n" +
				"            \"size\": 0\n" +
				"        }}}\n" +
				"    }}\n" +
				"}");

		setThreadSecurity("1", "test_userid", "test_username");

		//test all time ranges
		for (TimeRange range : TimeRange.values()) {

			for (Aggregate agg : Aggregate.values()) {

				//only test date aggregates here
				if (agg.getDateFormat() == null) {
					continue;
				}

				//update the properties accordingly
				testQuery.getJSONArray("attributes").getJSONObject(0).put("aggregate", agg.name().toLowerCase());

				//set up values for this filter
				JSONArray values = new JSONArray();
				values.put(range.name().toLowerCase());
				if (range.getCalendarField() >= 0) {
					values.put("1"); //one unit for this test
				}
				testQuery.getJSONObject("filter").getJSONArray("filter").getJSONObject(0).put("values", values);
				testQuery.getJSONObject("filter").getJSONArray("filter").getJSONObject(0).put("operator", "eq");

				//build the query
				Query query = QueryFactory.newQuery(testQuery, QueryFormat.JSON, instance);

				runner.initialize(new ElasticUnityDataSource() {
					@Override
					public UnityInstance getUnity() {
						return ElasticUnityRunnerTest.this.instance;
					}

					@Override
					public Client getClient() {
						return new TestClient();
					}
				}, query);

				SearchRequestBuilder sr = runner.buildSearchRequest(query);
				JSONObject srJson = new JSONObject(sr.toString());

				//check time range
				switch (range) {

					case LAST_MONTH:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.MONTH, Calendar.MONTH, -1).getTime(),
								 srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case LAST_QUARTER:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.QUARTER, Calendar.MONTH, -3).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case LAST_WEEK:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.WEEK, Calendar.WEEK_OF_YEAR, -1).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case LAST_YEAR:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.YEAR, Calendar.YEAR, -1).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case THIS_MONTH:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.MONTH).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case THIS_QUARTER:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.QUARTER).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case THIS_WEEK:
						assertEquals("range=" + range + " agg=" + agg, getDate(StartOf.WEEK).getTime(),
							     srJson.getJSONObject("query").getJSONObject("range").getJSONObject("field3").getLong("from"));
						break;
					case THIS_YEAR:
					case TODAY:
					case YESTERDAY:

					case LAST_N_DAYS:
					case LAST_N_HOURS:
					case LAST_N_MINUTES:
					case LAST_N_MONTHS:
					case LAST_N_QUARTERS:
					case LAST_N_WEEKS:
					case LAST_N_YEARS:

				}
			}

		}

	}

	private enum StartOf {
		SECOND,
		MINUTE,
		HOUR,
		DAY,
		WEEK,
		MONTH,
		QUARTER,
		YEAR
	}

	/**
	 * convenience method for testing datesIndices
	 * @param start what level to zero out to start at a specific time unit
	 * @param fields
	 * @return
	 */
	private Date getDate(StartOf start, int... fields) {
		if (fields.length % 2 != 0) {
			throw new UnsupportedOperationException("ints must be in multiples of 2: Calendar.FIELD, addAmount, etc etc");
		}

		Calendar cal = Calendar.getInstance();

		//set up calendar accordingly
		switch (start) {
			case YEAR:
				cal.set(Calendar.DAY_OF_YEAR, 1);
			case QUARTER:
				cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH) / 3) * 3);
			case MONTH:
				cal.set(Calendar.DAY_OF_MONTH, 1);
			case WEEK:
				//set it after, it will screw with stuff above
			case DAY:
				cal.set(Calendar.HOUR_OF_DAY, 0);
			case HOUR:
				cal.set(Calendar.MINUTE, 0);
			case MINUTE:
				cal.set(Calendar.SECOND, 0);
			case SECOND:
				cal.set(Calendar.MILLISECOND, 0);
		}

		if (start == StartOf.WEEK) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		}

		for (int x=0; x<fields.length; x+=2) {
			cal.add(fields[x], fields[x+1]);
		}

		return cal.getTime();

	}

	@Test(expected = InvalidQueryException.class)
	public void missingField() throws Exception {

		runner.toQueryBuilder(new AbstractFilterConstraint() {

			@Override
			public Collection<FilterConstraint> findAll(Field field) {
				return Collections.emptyList();
			}

			@Override
			public String printCanonical() {
				return null;
			}

			@Override
			public ConstraintOperator getOperator() {
				return ConstraintOperator.BETWEEN;
			}

			@Override
			protected void setValuesDefault(Collection<Object> values) {
			}

			@Override
			protected boolean matchDefault(Data data) {
				return false;
			}

			@Override
			public Field getField() {
				return null;
			}

		});

	}

	@Test(expected = InvalidQueryException.class)
	public void missingOp() throws Exception {

		runner.toQueryBuilder(new AbstractFilterConstraint() {

			@Override
			public Collection<FilterConstraint> findAll(Field field) {
				return Collections.emptyList();
			}

			@Override
			public String printCanonical() {
				return null;
			}

			@Override
			public ConstraintOperator getOperator() {
				return null;
			}

			@Override
			protected void setValuesDefault(Collection<Object> values) {
			}

			@Override
			protected boolean matchDefault(Data data) {
				return false;
			}

			@Override
			public Field getField() {
				return new Field("blah", FieldType.STRING);
			}

		});

	}

	@Test(expected = InvalidQueryException.class)
	public void missingValues() throws Exception {

		runner.toQueryBuilder(new AbstractFilterConstraint() {

			@Override
			public Collection<FilterConstraint> findAll(Field field) {
				return Collections.emptyList();
			}

			@Override
			public String printCanonical() {
				return null;
			}

			@Override
			public ConstraintOperator getOperator() {
				return ConstraintOperator.BETWEEN;
			}

			@Override
			protected void setValuesDefault(Collection<Object> values) {
			}

			@Override
			protected boolean matchDefault(Data data) {
				return false;
			}

			@Override
			public Field getField() {
				return new Field("blah", FieldType.STRING);
			}

			@Override
			public Collection<Object> getValues() {
				return null;
			}

		});

	}

	@Test
	public void gte() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field1\" : {\n" +
				"      \"from\" : 10.0,\n" +
				"      \"to\" : null,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : true\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new GteConstraint(new Field("field_int", FieldType.STRING), 10));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void gteTimestamp() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field3\" : {\n" +
				"      \"from\" : 100,\n" +
				"      \"to\" : null,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : true\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new GteConstraint(new Field("field_timestamp", FieldType.TIMESTAMP), Instant.ofEpochMilli(100L)));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void lte() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field1\" : {\n" +
				"      \"from\" : null,\n" +
				"      \"to\" : 10.0,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : true\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new LteConstraint(new Field("field_int", FieldType.INT), 10));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void lteTimestamp() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field_timestamp\" : {\n" +
				"      \"from\" : null,\n" +
				"      \"to\" : 100,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : true\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new LteConstraint(new Field("field_timestamp", FieldType.TIMESTAMP), Instant.ofEpochMilli(100L)));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void between() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field1\" : {\n" +
				"      \"from\" : 5.0,\n" +
				"      \"to\" : 10.0,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : false\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new BetweenConstraint(new Field("field_int", FieldType.INT), 5, 10));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void betweenTimestamp() throws Exception {

		String expected =
				"{\n" +
				"  \"range\" : {\n" +
				"    \"field3\" : {\n" +
				"      \"from\" : 50,\n" +
				"      \"to\" : 100,\n" +
				"      \"include_lower\" : true,\n" +
				"      \"include_upper\" : false\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new BetweenConstraint(new Field("field_timestamp", FieldType.TIMESTAMP), Instant.ofEpochMilli(50L), Instant.ofEpochMilli(100L)));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void exists() throws Exception {

		String expected =
				"{\n" +
				"  \"exists\" : {\n" +
				"    \"field\" : \"field3\"\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new ExistsConstraint(new Field("field_timestamp", FieldType.TIMESTAMP)));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void contains() throws Exception {

		String expected =
				"{\n" +
				"  \"wildcard\" : {\n" +
				"    \"field1\" : \"*blah*\"\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new ContainsConstraint(new Field("field_int", FieldType.STRING), "blah"));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void in() throws Exception {

		String expected =
				"{\n" +
				"  \"terms\" : {\n" +
				"    \"field2\" : [ \"a\", \"b\", \"c\" ]\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new InConstraint(new Field("field_string", FieldType.STRING), Arrays.asList(new String[] { "a", "b", "c" })));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void startsWith() throws Exception {

		String expected =
				"{\n" +
				"  \"prefix\" : {\n" +
				"    \"field2\" : \"blah\"\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new StartsWithConstraint(new Field("field_string", FieldType.STRING), "blah"));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void matches() throws Exception {

		String expected =
				"{\n" +
				"  \"regexp\" : {\n" +
				"    \"field2\" : {\n" +
				"      \"value\" : \"blah\",\n" +
				"      \"flags_value\" : 65535\n" +
				"    }\n" +
				"  }\n" +
				"}";

		QueryBuilder qb = runner.toQueryBuilder(new MatchesConstraint(new Field("field_string", FieldType.STRING), "blah"));

		assertEquals(expected, qb.toString());

	}

	@Test
	public void getIndices_DAYS() throws Exception {

		Tenant tenant = mock(Tenant.class);
		doReturn(ChronoUnit.DAYS).when(tenant).getIndexTimeUnit();
		doReturn("1").when(tenant).getTenantID();

		Query query = mock(Query.class);
		Filter filter = mock(Filter.class);
		doReturn(null).when(query).getDataTypes();
		doReturn(filter).when(query).getFilter();

		ElasticUnityRunner runner = new ElasticUnityRunner();
		ElasticUnityDataSource elastic = mock(ElasticUnityDataSource.class);
		doReturn(LocalDateTime.of(2015, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)).when(elastic).getEarliest(any(), any());
		doReturn(LocalDateTime.of(2016, 12, 1, 0, 0).toInstant(ZoneOffset.UTC)).when(elastic).getLatest(any(), any());
		runner.setElastic(elastic);
		Field mockField = new Field("dg_time", FieldType.TIMESTAMP);
		doReturn(ESKnownIndices.EVENTS_ES).when(elastic).getDefaultIndex();
		doReturn(Collections.singleton(new BetweenConstraint(mockField,
						LocalDateTime.of(2015, 10, 15, 0, 0).toInstant(ZoneOffset.UTC),
						LocalDateTime.of(2016, 03, 15, 0, 0).toInstant(ZoneOffset.UTC)
				))).when(filter).findAll(any());

		String[] indices = runner.getIndices(tenant, query);

		assertEquals(1, indices.length);
		assertEquals("events_es_1_*", indices[0]);

	}

	@Test
	public void getIndices_WEEKS() throws Exception {

		Tenant tenant = mock(Tenant.class);
		doReturn(ChronoUnit.WEEKS).when(tenant).getIndexTimeUnit();
		doReturn("1").when(tenant).getTenantID();

		Query query = mock(Query.class);
		Filter filter = mock(Filter.class);
		doReturn(null).when(query).getDataTypes();
		doReturn(filter).when(query).getFilter();

		ElasticUnityRunner runner = new ElasticUnityRunner();
		ElasticUnityDataSource elastic = mock(ElasticUnityDataSource.class);
		doReturn(LocalDateTime.of(2015, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)).when(elastic).getEarliest(any(), any());
		doReturn(LocalDateTime.of(2016, 12, 1, 0, 0).toInstant(ZoneOffset.UTC)).when(elastic).getLatest(any(), any());
		runner.setElastic(elastic);
		Field mockField = new Field("dg_time", FieldType.TIMESTAMP);
		doReturn(ESKnownIndices.EVENTS_ES).when(elastic).getDefaultIndex();
		doReturn(Collections.singleton(new BetweenConstraint(mockField,
						LocalDateTime.of(2015, 10, 15, 0, 0).toInstant(ZoneOffset.UTC),
						LocalDateTime.of(2016, 03, 15, 0, 0).toInstant(ZoneOffset.UTC)
				))).when(filter).findAll(any());

		String[] indices = runner.getIndices(tenant, query);

		assertEquals(1, indices.length);
		assertEquals("events_es_1_*", indices[0]);


	}

}
