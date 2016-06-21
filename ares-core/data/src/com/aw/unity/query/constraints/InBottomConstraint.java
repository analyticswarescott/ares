package com.aw.unity.query.constraints;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.QueryAttribute.Order;

/**
 * Where a value is in the least frequent N set of those values.
 *
 *
 *
 */
public class InBottomConstraint extends AbstractQueryInConstraint implements QueryConstraint {

	 @Override
	protected Order getOrder() {
		 //get bottom N values by ordering ascending
		 return Order.ASC;
	}

	 @Override
	public ConstraintOperator getOperator() {
		 return ConstraintOperator.IN_BOTTOM;
	}

}
