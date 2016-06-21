package com.aw.unity.query.constraints;

import java.util.Collections;
import java.util.regex.Pattern;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;

/**
 * Regular expression constraint
 *
 *
 *
 */
public class MatchesConstraint extends SingleValueConstraint {

	public MatchesConstraint() {
	}

	public MatchesConstraint(Field field, String value) {
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

		//check the regex against the value
		return m_pattern.matcher(value.toString()).find();

	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.MATCHES;
	}

	public String getValue() { return m_value; }
	public void setValue(Object value) {

		//null regex is not allowed
		if (value == null) {
			throw new InvalidFilterException("matches constraint cannot have a null value");
		}

		//set the value/pattern
		m_value = String.valueOf(value);
		m_pattern = Pattern.compile(m_value);

	}
	private String m_value;

	public Pattern getPattern() { return m_pattern; }
	public void setPattern(Pattern pattern) { m_pattern = pattern; }
	private Pattern m_pattern;

}
