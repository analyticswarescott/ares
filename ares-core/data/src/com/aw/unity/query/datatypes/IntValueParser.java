package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * Parse int values from strings.
 *
 *
 *
 */
public class IntValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {

		try {
			return Integer.valueOf(String.valueOf(value));
		} catch (InvalidFilterException e) {
			throw new InvalidFilterException("Error parsing " + value + ": " + e.getMessage());
		}

	}

}
