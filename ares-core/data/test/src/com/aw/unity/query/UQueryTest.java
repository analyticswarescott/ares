package com.aw.unity.query;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.aw.unity.Field;
import com.aw.unity.TestDataType;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.QueryAttribute.Order;
import com.aw.unity.query.constraints.EqConstraint;

public class UQueryTest {

	@Test
	public void copy() {

		Filter filter1 = new FilterGroup().addFilter(new EqConstraint(TestDataType.FIELD_INT, 1));
		Filter filter2 = new FilterGroup().addFilter(new EqConstraint(TestDataType.FIELD_INT, 2));

		Query query1 = new Query();
		query1.setDataTypes(new TestDataType());
		query1.setFilter(filter1);

		//make sure copying creates a new instance
		Query query2 = query1.add(filter2);

		//make sure it's a different query
		assertTrue(query1 != query2);

		//make sure it has both filters
		assertTrue("query with added filter does not contain both",
				   ((FilterGroup)query2.getFilter()).getFilters().contains(filter1) &&
				   ((FilterGroup)query2.getFilter()).getFilters().contains(filter2));


		//make sure the original only has the first constraint
		assertTrue("query that had a filter added to it was not copied properly, should only contain the first filter",
				   query1.getFilter() == filter1 &&
				  !((FilterGroup)query1.getFilter()).getFilters().contains(filter2));
	}

	@Test
	public void getFields() {

		Query q = new Query();
		q.setAttributes(new QueryAttribute[] {
				new QueryAttribute(TestDataType.FIELD_DOUBLE, Order.NONE, Aggregate.AVG),
				new QueryAttribute(TestDataType.FIELD_INT, Order.NONE, Aggregate.AVG),
				new QueryAttribute(TestDataType.FIELD_LONG, Order.NONE, Aggregate.NONE),
				new QueryAttribute(TestDataType.FIELD_STRING, Order.NONE, Aggregate.NONE),
		});
		q.initialize();

		//dimensions should be first, followed by measures
		assertEquals("field list wrong", Arrays.asList(new Field[] { TestDataType.FIELD_DOUBLE, TestDataType.FIELD_INT, TestDataType.FIELD_LONG, TestDataType.FIELD_STRING }), q.getFields());
	}

	@Test
	public void validate_null_field() {
		Query q = new Query();
		q.setAttributes(new QueryAttribute[] {
				new QueryAttribute(null, Order.NONE, Aggregate.AVG),
				new QueryAttribute(TestDataType.FIELD_INT, Order.NONE, Aggregate.AVG),
				new QueryAttribute(TestDataType.FIELD_LONG, Order.NONE, Aggregate.NONE),
				new QueryAttribute(TestDataType.FIELD_STRING, Order.NONE, Aggregate.NONE),
		});

		try {
			q.validate();
			fail("query is invalid but validate passed");
		} catch (InvalidQueryException e) {
		}
	}

	@Test
	public void validate_aggregate_long_timestamp() {

		for (Aggregate agg : Aggregate.values()) {

			//only test timestamp aggregates
			switch (agg) {
				case SECOND:
				case MINUTE:
				case HOUR:
				case DAY:
				case WEEK:
				case MONTH:
				case QUARTER:
				case YEAR:
					break;
				default:
					continue;
			}

			//this should fail
			Query q = new Query();
			q.setAttributes(new QueryAttribute[] {
					new QueryAttribute(TestDataType.FIELD_INT, Order.NONE, Aggregate.AVG),
					new QueryAttribute(TestDataType.FIELD_LONG, Order.NONE, agg),
					new QueryAttribute(TestDataType.FIELD_STRING, Order.NONE, Aggregate.NONE),
			});

			try {
				q.validate();
				fail("query is invalid but validate passed");
			} catch (InvalidQueryException e) {
			}
		}
	}

	@Test
	public void validate_aggregate_timestamp_avg() {
		Query q = new Query();
		q.setAttributes(new QueryAttribute[] {
				new QueryAttribute(TestDataType.FIELD_TIMESTAMP, Order.NONE, Aggregate.AVG),
				new QueryAttribute(TestDataType.FIELD_LONG, Order.NONE, Aggregate.DAY),
				new QueryAttribute(TestDataType.FIELD_STRING, Order.NONE, Aggregate.NONE),
		});

		try {
			q.validate();
			fail("query is invalid but validate passed");
		} catch (InvalidQueryException e) {
		}
	}
}
