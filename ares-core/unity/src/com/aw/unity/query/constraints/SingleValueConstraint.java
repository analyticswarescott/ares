package com.aw.unity.query.constraints;

import java.util.Collection;

import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Any field constraint with a single value should extend this constraint base class.
 *
 *
 *
 */
public abstract class SingleValueConstraint extends ExistsConstraint {

	@Override
	public String printCanonical() {
		return getField() + " " + getCanonicalOperator() + " " + getValue();
	}

	/**
	 * Parses the value as a double by default
	 */
	@Override
	public void setValuesDefault(Collection<Object> values) {

		//make sure the values are correct
		if (values == null || values.size() != 1) {
			throw new InvalidFilterException(getCanonicalOperator() + " constraint must have exactly one value");
		}

		setValue(values.iterator().next());

	}

	/**
	 * Get the value
	 * @return The single value
	 */
	protected abstract Object getValue();

	/**
	 * Set the value
	 * @param value The single value
	 */
	protected abstract void setValue(Object value);

	/**
	 * @return The string representing this operator, suitable for a canonical string - by default this will be the lower case name of the operator
	 */
	protected String getCanonicalOperator() {
		return getOperator().name().toLowerCase();
	}

}
