package com.aw.unity.query;

import java.util.Collection;
import java.util.List;

import com.aw.unity.DataType;
import com.aw.unity.Field;

/**
 * Parses values in filters
 */
public interface ValueParser {

	/**
	 * Parse a single value from a string
	 * @param parentType Parent type for this value, if any. If no parent type for this value, this should be null
	 * @param field TODO
	 * @param value The value to parse
	 * @return
	 */
	public Object parse(DataType parentType, Field field, Object data);

	/**
	 * Parse a set of values from a filter operation
	 * @param operator TODO
	 * @param value The string to parse
	 * @return The typed object as a result of the parsing
	 */
	public Collection<Object> parse(ConstraintOperator operator, List<String> values);

}
