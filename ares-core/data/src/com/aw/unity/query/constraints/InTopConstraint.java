package com.aw.unity.query.constraints;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.QueryAttribute.Order;

public class InTopConstraint extends AbstractQueryInConstraint {

	@Override
	protected Order getOrder() {
		//get top N values by ordering descending
		return Order.DESC;
	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.IN_TOP;
	}

}
