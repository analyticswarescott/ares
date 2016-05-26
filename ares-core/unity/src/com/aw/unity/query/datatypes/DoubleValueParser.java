package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Parse doubles from strings.
 *
 *
 */

public class DoubleValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		try {
			return Double.valueOf(String.valueOf(value));
		} catch (Exception e) {
			throw new InvalidFilterException("error parsing double " + value + ": " + value);
		}
	}

}
