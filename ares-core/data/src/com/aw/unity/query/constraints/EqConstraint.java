package com.aw.unity.query.constraints;

import java.util.Collection;
import java.util.Collections;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.Filter;

/**
 * Check basic equality
 *
 *
 *
 */
public class EqConstraint extends ExistsConstraint {

	public EqConstraint() {
	}

	public EqConstraint(Field field, Object value) {
		super(field);
		setValues(Collections.singleton(value));
		setValue(value);
	}

	/**
	 * Are the two values equal
	 */
	public boolean matchDefault(Data data) {

		//make sure the field exists in the data
		if (!super.matchDefault(data)) {
			return false;
		}

		Object value = data.getValue(getField());
		return value == m_value ||
		       	(value != null &&
		       	 m_value != null &&
		       	 value.equals(m_value));
	}

	@Override
	public Filter deepCopy() {
		EqConstraint ret = (EqConstraint)super.deepCopy();
		ret.m_value = m_value;
		return ret;
	}

	/**
	 * Only uses the first value in the list, must be a list of length 1
	 *
	 * @param values
	 */
	public void setValuesDefault(Collection<Object> values) {
		if (values.size() == 0) {
			throw new InvalidFilterException("eq constraint must have a value.");
		}

		setValue(values.iterator().next());
	}

	@Override
	public String printCanonical() {
		return getField() + " " + getOperator() + " " + getValue();
	}

	public ConstraintOperator getOperator() { return ConstraintOperator.EQ; }

	public Object getValue() { return m_value; }
	public void setValue(Object value) { m_value = value; }
	private Object m_value;

}
