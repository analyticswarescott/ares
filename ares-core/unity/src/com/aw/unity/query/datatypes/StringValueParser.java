package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;

/**
 * Just pass the string through
 */
public class StringValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		return String.valueOf(value);
	}

}
