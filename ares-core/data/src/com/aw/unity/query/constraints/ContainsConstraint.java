package com.aw.unity.query.constraints;

import java.util.Collections;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;

public class ContainsConstraint extends SingleValueConstraint {

	public ContainsConstraint() {
	}

	public ContainsConstraint(Field field, String value) {
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
		return value.toString().contains(m_value);

	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.CONTAINS;
	}

	public String getValue() { return m_value; }
	public void setValue(Object value) {

		//can't have null contains
		if (value == null) {
			throw new InvalidFilterException("contains constraint cannot have a null value");
		}
		m_value = value == null ? null : value.toString();

	}
	private String m_value;

}
