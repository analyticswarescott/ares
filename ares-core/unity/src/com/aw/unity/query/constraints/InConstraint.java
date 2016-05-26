package com.aw.unity.query.constraints;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.query.ConstraintOperator;

/**
 * Check basic equality
 *
 *
 *
 */
public class InConstraint extends ExistsConstraint {

	public InConstraint() {
	}

	public InConstraint(Field field, Collection<Object> values) {
		super(field);
		setValues(values);
	}

	/**
	 * Are the two values equal
	 */
	public boolean matchDefault(Data data) {

		//make sure the field is in the data
		if (!super.matchDefault(data)) {
			return false;
		}

		Object value = data.getValue(getField());
		return m_values.contains(value);

	}

	/**
	 * @param values
	 */
	public void setValuesDefault(Collection<Object> values) {
		for (Object value : values) {
			//make sure they are parsed properly
			m_values.add(getField().getType().parse(String.valueOf(value)));
		}
	}

	@Override
	public String printCanonical() {
		return getField() + " " + getOperator() + " " + getValues();
	}

	public ConstraintOperator getOperator() { return ConstraintOperator.IN; }

	private Set<Object> m_values = new TreeSet<Object>(); //we need the set ordered for handling of custom data types - for example, for IP address IN clauses, CIDRs come before IPs

}
