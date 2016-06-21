package com.aw.unity.query;

import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.constraints.BetweenConstraint;
import com.aw.unity.query.constraints.ContainsConstraint;
import com.aw.unity.query.constraints.EqConstraint;
import com.aw.unity.query.constraints.ExistsConstraint;
import com.aw.unity.query.constraints.GteConstraint;
import com.aw.unity.query.constraints.InBottomConstraint;
import com.aw.unity.query.constraints.InConstraint;
import com.aw.unity.query.constraints.InTopConstraint;
import com.aw.unity.query.constraints.LteConstraint;
import com.aw.unity.query.constraints.MatchesConstraint;
import com.aw.unity.query.constraints.StartsWithConstraint;

/**
 * Known types of constraints
 *
 *
 */
public enum ConstraintOperator {
	EQ(EqConstraint.class),
	IN(InConstraint.class),
	BETWEEN(BetweenConstraint.class),
	LTE(LteConstraint.class),
	GTE(GteConstraint.class),
//TODO: add these
//	LT(LtConstraint.class),
//	GT(GtConstraint.class),
	STARTS_WITH(StartsWithConstraint.class),
	CONTAINS(ContainsConstraint.class),
	MATCHES(MatchesConstraint.class),
	EXISTS(ExistsConstraint.class),
	IN_BOTTOM(InBottomConstraint.class),
	IN_TOP(InTopConstraint.class);

	ConstraintOperator(Class<? extends FilterConstraint> type){
		this.type = type;
	}

	public FilterConstraint newConstraint() throws Exception {
		return type.newInstance();
	}

	/**
	 * @param str The operator string
	 * @return The ConstraintOperator, or null if one could not be determined from the input string
	 */
	public static ConstraintOperator fromString(String str) {
		try {
			return valueOf(str.toUpperCase());
		} catch (Exception e) {
			throw new InvalidFilterException("Unrecognized operator: " + str);
		}
	}
	public Class<? extends FilterConstraint> getType() { return type; }
	private Class<? extends FilterConstraint> type;

}
