package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Parse float values from strings.
 */
public class FloatValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		try {
			return Float.valueOf(String.valueOf(value));
		} catch (Exception e) {
			throw new InvalidFilterException("Error parsing " + value + ": " + e.getMessage());
		}
	}

}
