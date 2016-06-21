package com.aw.unity.query.constraints;

import java.util.Collections;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;

public class StartsWithConstraint extends SingleValueConstraint {

	public StartsWithConstraint() {
	}

	public StartsWithConstraint(Field field, String value) {
		setField(field);
		setValues(Collections.singletonList(value));
	}

	@Override
	public boolean matchDefault(Data data) {

		//make sure field exists in the data
		if (!super.matchDefault(data)) {
			return false;
		}

		Object value = data.getValue(getField());

		//can't compare null values
		if (value == null) {
			return false;
		}

		//now we can do the check
		return value.toString().startsWith(m_value);

	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.STARTS_WITH;
	}

	public String getValue() { return m_value; }
	public void setValue(Object value) {

		//can't have null
		if (value == null) {
			throw new InvalidFilterException("starts_with constraint requires a non-null value");
		}
		m_value = String.valueOf(value);

	}
	private String m_value;

}
