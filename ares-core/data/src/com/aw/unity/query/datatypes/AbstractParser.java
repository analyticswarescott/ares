package com.aw.unity.query.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.ValueParser;

/**
 * Basic default parser implementation that assumes all values in the list of values are parsed the
 * same way.
 *
 *
 */
public abstract class AbstractParser implements ValueParser {

	@Override
	public final Collection<Object> parse(ConstraintOperator operator, List<String> values) {
		Collection<Object> ret = operator == ConstraintOperator.IN ? new TreeSet<Object>() : new ArrayList<Object>();
		for (String value : values) {
			ret.add(parse(null, null, value));
		}
		return ret;
	}

}
