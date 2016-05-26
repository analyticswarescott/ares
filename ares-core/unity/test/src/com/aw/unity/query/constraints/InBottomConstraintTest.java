package com.aw.unity.query.constraints;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.aw.common.util.JSONUtils;
import com.aw.unity.Field;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;

public class InBottomConstraintTest {

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
			"    \"col_data_type\" : \"int\",\n" +
			"    \"aggregate\" : \"sum\"\n" +
			"  } ],\n" +
			"  \"dimensions\" : [ {\n" +
			"    \"order\" : \"none\",\n" +
			"    \"col_data_type\" : \"string\",\n" +
			"    \"aggregate\" : \"none\"\n" +
			"  } ],\n" +
			"  \"detail\" : false,\n" +
			"  \"all_attributes\" : false\n" +
			"}";

	@Test
	public void test() throws Exception {

		TestUnityInstance instance = new TestUnityInstance();

		InTopConstraint constraint = new InTopConstraint();

		constraint.setField(instance.getFieldRepository().getField("field_string"));
		Field field = instance.getFieldRepository().getField("field_int");
		constraint.setValues(Arrays.asList(new Object[] { 10, "sum", instance.getFieldRepository().getField("field_int") }));

		Query parent = new Query();
		parent.setFilter(new FilterGroup());

		Query bottomQuery = constraint.getQuery(parent);
		bottomQuery.setTimeZone("-0500");

		String actualQuery = JSONUtils.objectToString(bottomQuery);

		assertTrue("in_bottom query wrong", JSONUtils.jsonEquals(EXPECTED_QUERY, actualQuery));


	}

}
