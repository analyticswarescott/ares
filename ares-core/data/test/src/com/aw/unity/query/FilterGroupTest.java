package com.aw.unity.query;

import static com.aw.unity.TestDataType.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.FilterGroup.GroupOperator;
import com.aw.unity.query.constraints.EqConstraint;
import com.aw.unity.query.constraints.InConstraint;

public class FilterGroupTest {

	@Test
	public void match_or() throws Exception {

		FilterGroup orFilter = new FilterGroup(GroupOperator.OR);
		orFilter.addFilter(new EqConstraint(FIELD_INT, 1));
		orFilter.addFilter(new EqConstraint(FIELD_DOUBLE, 2D));

		//make sure either will make it true
		assertTrue(orFilter.match(new TestData(FIELD_INT, 1, FIELD_DOUBLE, 2D)));
		assertTrue(orFilter.match(new TestData(FIELD_INT, 1, FIELD_DOUBLE, 3D)));
		assertFalse(orFilter.match(new TestData(FIELD_INT, 2, FIELD_DOUBLE, 3D)));

	}

	@Test
	public void match_and() throws Exception {

		FilterGroup andFilter = new FilterGroup(GroupOperator.AND);
		andFilter.addFilter(new EqConstraint(FIELD_INT, 1));
		andFilter.addFilter(new EqConstraint(FIELD_DOUBLE, 2D));

		//make sure both will make it true
		assertTrue(andFilter.match(new TestData(FIELD_INT, 1, FIELD_DOUBLE, 2D)));
		assertFalse(andFilter.match(new TestData(FIELD_INT, 1, FIELD_DOUBLE, 3)));
		assertFalse(andFilter.match(new TestData(FIELD_INT, 2, FIELD_DOUBLE, 3)));

	}

	@Test
	public void validate_missing_operator() {

		FilterGroup root = new FilterGroup(GroupOperator.AND).
							addFilter(new FilterGroup(GroupOperator.OR).
							addFilter(new FilterGroup(null)));;

		try {
			root.validate();
		} catch (InvalidFilterException e) {
		}

	}

	@Test
	public void validate_bad_constraint() {

		FilterGroup root = new FilterGroup(GroupOperator.AND).
							addFilter(new FilterGroup(GroupOperator.OR).
							addFilter(new InConstraint()));

		try {
			root.validate();
		} catch (InvalidFilterException e) {
		}

	}
}
