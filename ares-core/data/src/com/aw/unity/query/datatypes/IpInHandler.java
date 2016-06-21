package com.aw.unity.query.datatypes;

import java.util.Collection;
import java.util.Set;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.OpHandler;

/**
 * Custom handling for IP address IN constraints.
 *
 *
 *
 */
public class IpInHandler implements OpHandler {

	public IpInHandler() {
	}

	@Override
	public ConstraintOperator getOperator() {
		return ConstraintOperator.IN;
	}

	@Override
	public boolean match(Object lValue, Collection<Object> rValues) {

		IpAddress addr = (IpAddress)lValue;

		//if no data to check against, return false
		if (rValues == null || rValues.size() == 0) {
			return false;
		}

		//should just be a set
		Set<Object> values = (Set<Object>)rValues;

		//check standard equality for addresses
		if (values.contains(lValue)) {
			return true;
		}

		//now check for cidrs - they'll come first in the list
		for (Object addressable : values) {
			if (addressable instanceof CIDR) {
				if (((CIDR)addressable).contains(addr)) {
					return true;
				}
			}

			//CIDRs will always come first in the list
			else {
				break;
			}
		}

		//no match from cidrs
		return false;

	}

}
