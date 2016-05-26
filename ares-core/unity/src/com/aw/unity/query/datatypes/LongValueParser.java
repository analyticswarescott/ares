package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Parse long values from strings.
 *
 *
 *
 */
public class LongValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		try {
			 return Long.valueOf(String.valueOf(value));
		} catch (Exception e) {
			throw new InvalidFilterException("error parsing long value " + value + ": " + e.getMessage());
		}
	}

}
