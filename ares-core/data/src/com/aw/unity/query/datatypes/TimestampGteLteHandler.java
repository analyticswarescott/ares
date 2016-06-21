package com.aw.unity.query.datatypes;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.aw.common.util.JSONUtils;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.OpHandler;

/**
 * Same as in handler
 *
 *
 *
 */
public class TimestampGteLteHandler implements OpHandler {

	public TimestampGteLteHandler(ConstraintOperator op) {
		m_operator = op;
	}

	private ConstraintOperator m_operator;

	@Override
	public ConstraintOperator getOperator() {
		return m_operator;
	}

	public static final Instant getTimestamp(FilterConstraint constraint) {
		return (Instant)((Collection<Object>)constraint.getValues()).iterator().next();
	}

	@Override
	public boolean match(Object lValue, Collection<Object> values) {

		Instant date = Instant.MIN;

		try {

			//values should be dates - TODO: handle date objects
			if (lValue instanceof Instant) {
				date = ((Instant)lValue);
			}

			else {
				date = JSONUtils.JSON_DATE_FORMAT.parse(lValue.toString()).toInstant();
			}

		} catch (ParseException e) {
			throw new InvalidDataException("Date could not be parsed: " + lValue);
		}

		//we now have the date, so check that it's GteLte start -> end
		Instant pointInTime = ((Instant)((List<Object>)values).get(0));

		switch (getOperator()) {
			case LTE:
				return !date.isAfter(pointInTime);
			case GTE:
				return !date.isBefore(pointInTime);
			default:
				throw new InvalidFilterException("found unsupported filter in gte/lte timestamp filter op=" + getOperator() + " values=" + values);
		}

	}

}
