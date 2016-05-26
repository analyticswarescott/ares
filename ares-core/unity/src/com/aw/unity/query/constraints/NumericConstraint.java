package com.aw.unity.query.constraints;

import java.time.Instant;
import java.util.Collections;

import com.aw.unity.Data;
import com.aw.unity.Field;

/**
 * Less than or equal to
 *
 *
 */
public abstract class NumericConstraint extends SingleValueConstraint {

	public NumericConstraint() {
	}

	public NumericConstraint(Field field, Instant value) {
		setField(field);
		setValues(Collections.singleton(value));
	}

	public NumericConstraint(Field field, Number value) {
		setField(field);
		setValue(value);
		setValues(Collections.singleton(value));
	}

	@Override
	public boolean matchDefault(Data data) {

		//make sure the field exists in the data - otherwise it doesn't match
		if (!super.matchDefault(data)) {
			return false;
		}

		Object value = data.getValue(getField());

		//can't compare nulls
		if (value == null || m_value == null) {
			return false;
		}

		//compare as doubles
		else if (value instanceof Number) {
			return match(((Number)value).doubleValue(), m_value.doubleValue());
		}

		//try parsing a string
		else {
			Double d = Double.parseDouble(String.valueOf(value));
			return match(d.doubleValue(), m_value.doubleValue());
		}

	}

	/**
	 * Perform the core logic of this numeric operator
	 *
	 * @param d1 The lValue (from the data)
	 * @param d2 The rValue (defined in the constraint
	 * @return Whether there was a match
	 */
	protected abstract boolean match(double d1, double d2);

	public Number getValue() { return m_value; }
	public void setValue(Object value) { m_value = (Number)value; }
	private Number m_value;

}
