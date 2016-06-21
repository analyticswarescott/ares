package com.aw.unity.query;

import java.util.Collection;

/**
 * When an operation occurs that needs custom handling for a particular type, handle it using this interface and plug it into the FieldType
 * enum.
 *
 *
 *
 */
public interface OpHandler {

	/**
	 * @return The operator for which there is custom handling
	 */
	public ConstraintOperator getOperator();

	/**
	 * Handle the match logic.
	 *
	 * @param lValue The lValue of the logic to handle.
	 * @param rValue The rValue of the logic to handle.
	 * @return Whether a match occurred.
	 */
	public boolean match(Object lValue, Collection<Object> values);

}
