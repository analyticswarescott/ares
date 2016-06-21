package com.aw.unity.query;

import java.time.Instant;
import java.util.Collection;

import com.aw.unity.Field;
import com.aw.unity.query.datatypes.TimestampHandler;

public class Utils {

	/**
	 * find all constraints matching the field in the tree
	 *
	 * @param ret
	 * @param filter
	 * @param field
	 */
	static void findAll(Collection<FilterConstraint> ret, Filter filter, Field field) {

		//if we found a leaf node, check it and add it if there's a match
		if (filter instanceof FilterConstraint) {
			FilterConstraint constraint = (FilterConstraint)filter;
			if (constraint.getField().equals(field)) {
				ret.add(constraint);
			}
		}

		//else recurse
		else {
			for (Filter child : filter.getFilters()) {
				findAll(ret, child, field);
			}
		}

	}

	/**
	 * get the earliest start of all of the given timestamp constraints
	 *
	 * @param constraints
	 * @return
	 */
	public static Instant getStart(Collection<FilterConstraint> constraints) {

		Instant ret = null;
		for (FilterConstraint constraint : constraints) {

			Instant start = TimestampHandler.getStart(constraint);
			if (start != null &&
			    (ret == null || ret.isAfter(start))) {
				ret = start;
			}

		}

		return ret;

	}

	/**
	 * get the latest end of all of the timestamp constraints
	 *
	 * @param constraints
	 * @return
	 */
	public static Instant getEnd(Collection<FilterConstraint> constraints) {

		Instant ret = null;
		for (FilterConstraint constraint : constraints) {

			Instant end = TimestampHandler.getEnd(constraint);
			if (end != null &&
			    (ret == null || ret.isBefore(end))) {
				ret = end;
			}

		}

		return ret;

	}
}
