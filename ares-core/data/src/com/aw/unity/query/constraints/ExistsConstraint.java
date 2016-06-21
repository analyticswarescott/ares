package com.aw.unity.query.constraints;

import java.util.Collection;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.query.ConstraintOperator;

/**
 * Detects whether the given field exist on the data.
 *
 *
 *
 */
public class ExistsConstraint extends AbstractFilterConstraint {

	public ExistsConstraint() {
	}

	public ExistsConstraint(Field field) {
		super(field);
	}

	@Override
	protected void setValuesDefault(Collection<Object> values) {
		//no values for this filter type
	}

	@Override
	public boolean matchDefault(Data data) {
		return data.getType().hasField(getField());
	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.EXISTS;
	}

	@Override
	public String printCanonical() {
		return "Field " + getField() + " exists";
	}

}
