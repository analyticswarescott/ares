package com.aw.unity.query;

import java.util.Collection;

import com.aw.unity.Field;

/**
 * A single constraint (condition) on a filter.
 *
 *
 */
public interface FilterConstraint extends Filter {

	/**
	 * @return The values set on this constraint
	 */
	public Collection<Object> getValues();

	/**
	 * @param values The values to set on this constraint
	 */
	public void setValues(Collection<Object> values);

	/**
	 * @return The unity field associated with this constraint
	 */
    public Field getField();

    /**
     * Set the unity field associated with this constraint
     *
     * @param field The unity field associated with this constraint
     */
    public void setField(Field field);

    /**
     * @return The operator associated with this constraint
     */
    public ConstraintOperator getOperator();

    /**
     * @param not Whether the logic on the constraint is not'd (inverted)
     */
    public void setNot(boolean not);

}


