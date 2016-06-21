package com.aw.unity.query.constraints;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;

/**
 * Check betweenness. For now this is implemented as min <= value < max.
 *
 *
 *
 */
public class BetweenConstraint extends ExistsConstraint {

	public BetweenConstraint() {
	}

	public BetweenConstraint(Field field, Object min, Object max) {
		super(field);

		//set values
		List<Object> dates = new ArrayList<Object>();
		dates.add(min);
		dates.add(max);
		setValues(dates);
}

	/**
	 * Are the two values equal
	 */
	public boolean matchDefault(Data data) {

		//if field doesn't apply, we don't match
		if (!super.matchDefault(data)) {
			return false;
		}

		Object value = data.getValue(getField());

		//if any values are null, we can't perform our logic i.e. we didn't match
		if (value == null || m_min == null || m_max == null) {
			return false;
		}

		double min = 0;
		double max = 0;

		//get numeric values for min/max
		switch (getField().getType()) {
			case TIMESTAMP:
				min = ((Instant)m_min).toEpochMilli();
				max = ((Instant)m_max).toEpochMilli();
				break;
			default:
				min = ((Number)m_min).doubleValue();
				max = ((Number)m_max).doubleValue();
				break;
		}

		double val = ((Number)value).doubleValue();

		return min <= val && val < max;

	}

	/**
	 * Only uses the first value in the list, must be a list of length 1
	 *
	 * @param values
	 */
	public void setValuesDefault(Collection<Object> values) {
		if (values.size() != 2) {
			throw new InvalidFilterException("between operator must have exactly two values.");
		}

		Iterator<Object> valueIter = values.iterator();
		m_min = valueIter.next();
		m_max = valueIter.next();
	}

	@Override
	public String printCanonical() {
		return getField() + " " + getOperator() + " " + m_min + " and " + m_max;
	}

	public ConstraintOperator getOperator() { return ConstraintOperator.BETWEEN; }

	public Object getMin() { return m_min; }
	public void setMin(Object min) { m_min = min; }
	private Object m_min;

	public Object getMax() { return m_max; }
	public void setMax(Object max) { m_max = max; }
	private Object m_max;

}
