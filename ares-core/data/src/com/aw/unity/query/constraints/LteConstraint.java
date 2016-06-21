package com.aw.unity.query.constraints;

import java.time.Instant;

import com.aw.unity.Field;
import com.aw.unity.query.ConstraintOperator;

/**
 * Less than or equal to
 *
 *
 */
public class LteConstraint extends NumericConstraint {

	public LteConstraint() {
	}

	public LteConstraint(Field field, Instant date) {
		super(field, date);
	}

	public LteConstraint(Field field, Number number) {
		super(field, number);
	}

	@Override
	protected String getCanonicalOperator() {
		return "<=";
	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.LTE;
	}

	@Override
	protected boolean match(double d1, double d2) {
		return d1 <= d2;
	}

}
