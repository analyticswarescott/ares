package com.aw.unity.query.datatypes;

import java.time.Instant;

import com.aw.unity.query.FilterConstraint;

/**
 * utilities for handling any timestamp constraint and returns the start or end time
 *
 *
 *
 */
public class TimestampHandler {

	public static Instant getStart(FilterConstraint constraint) {

		switch (constraint.getOperator()) {
			case EQ:
			case IN: return TimestampInEqHandler.getStartTime(constraint);
			case BETWEEN: return TimestampBetweenHandler.getStartTime(constraint);
			case GTE: return TimestampGteLteHandler.getTimestamp(constraint);
			case LTE: return null;
			default: throw new RuntimeException("unsupported operator for timestamp: " + constraint.getOperator());
		}

	}

	public static Instant getEnd(FilterConstraint constraint) {

		switch (constraint.getOperator()) {
			case EQ:
			case IN: return TimestampInEqHandler.getEndTime(constraint);
			case BETWEEN: return TimestampBetweenHandler.getEndTime(constraint);
			case GTE: return null;
			case LTE: return TimestampGteLteHandler.getTimestamp(constraint);
			default: throw new RuntimeException("unsupported operator for timestamp: " + constraint.getOperator());
		}

	}

}
