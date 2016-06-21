package com.aw.unity.query.datatypes;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.OpHandler;

/**
 * Same as in handler
 *
 *
 *
 */
public class TimestampBetweenHandler implements OpHandler {

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.BETWEEN;
	}

	public static final Instant getStartTime(FilterConstraint constraint) {
		return (Instant)((List<Object>)constraint.getValues()).get(0);
	}

	public static final Instant getEndTime(FilterConstraint constraint) {
		return (Instant)((List<Object>)constraint.getValues()).get(1);
	}

	@Override
	public boolean match(Object lValue, Collection<Object> values) {

		//it's a list
		List<Object> objects = (List<Object>)values;

		//the date from the data
		Instant date = ((Instant)lValue);

		//we now have the date, so check that it's between start -> end
		Instant start = ((Instant)objects.get(0));
		Instant end = ((Instant)objects.get(1));
		return !date.isBefore(start) && end.isAfter(date);

	}

}
