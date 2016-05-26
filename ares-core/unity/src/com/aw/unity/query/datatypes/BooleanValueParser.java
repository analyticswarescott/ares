package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Parse Booleans from strings.
 *
 *
 */

public class BooleanValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		try {
			return Boolean.parseBoolean(String.valueOf(value));
		} catch (Exception e) {
			throw new InvalidFilterException("error parsing boolean " + value + ": " + value);
		}
	}

}
