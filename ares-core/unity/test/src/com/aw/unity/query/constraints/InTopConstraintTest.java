package com.aw.unity.query.constraints;

import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.common.util.JSONUtils;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;

public class InTopConstraintTest {

	private static final String EXPECTED_QUERY =
			"{\n" +
			"  \"fields\" : [ {\n" +
			"    \"array\" : false,\n" +
			"    \"name\" : \"field_string\",\n" +
			"    \"type\" : \"string\",\n" +
			"    \"tags\" : [ ]\n" +
			"  }, {\n" +
			"    \"array\" : false,\n" +
			"    \"name\" : \"field_int\",\n" +
			"    \"type\" : \"int\",\n" +
			"    \"tags\" : [ ]\n" +
			"  } ],\n" +
			"  \"size\" : 10,\n" +
			"  \"time_zone\" : \"-0500\",\n" +
			"  \"count_only\" : false,\n" +
			"  \"measures\" : [ {\n" +
			"    \"order\" : \"desc\",\n" +
			"    \"aggregate\" : \"sum\",\n" +
			"    \"col_data_type\" : \"int\"\n" +
			"  } ],\n" +
			"  \"detail\" : false,\n" +
			"  \"all_attributes\" : false,\n" +
			"  \"dimensions\" : [ {\n" +
			"    \"order\" : \"none\",\n" +
			"    \"aggregate\" : \"none\",\n" +
			"    \"col_data_type\" : \"string\"\n" +
			"  } ]\n" +
			"}";

	@Test
	public void test() throws Exception {

		TestUnityInstance instance = new TestUnityInstance();

		InTopConstraint top = new InTopConstraint();

		top.setField(instance.getFieldRepository().getField("field_string"));
		top.setValues(Arrays.asList(new Object[] { 10, "sum", instance.getFieldRepository().getField("field_int") }));

		Query parent = new Query();
		parent.setFilter(new FilterGroup());

		Query topQuery = top.getQuery(parent);
		topQuery.setTimeZone("-0500");

		String actualQuery = JSONUtils.objectToString(topQuery);
		assertTrue("in_top query wrong", JSONUtils.jsonEquals(EXPECTED_QUERY, actualQuery));

	}

}
