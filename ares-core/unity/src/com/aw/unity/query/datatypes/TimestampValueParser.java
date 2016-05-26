package com.aw.unity.query.datatypes;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.aw.common.util.JSONUtils;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.TimeRange;
import com.aw.unity.query.ValueParser;

/**
 * Returns a timestamp in millisecond precision. Supports standard JSON timestamp format
 * (ISO 8601) and millis in numeric format since 1970 epoch.
 *
 * TODO: allow multiple formats - maybe from a config file..
 *
 *
 */
public class TimestampValueParser implements ValueParser {

	//conform is ISO 8601 for now.. 2012-04-23T18:25:43.511Z
	public static final SimpleDateFormat FORMATTED = JSONUtils.JSON_DATE_FORMAT;

	public Object parse(DataType parentType, Field field, Object value) {
		return parseDate(value);
	}

	@Override
	public List<Object> parse(ConstraintOperator operator, List<String> values) {

		//between two timestamps
		if (operator == ConstraintOperator.BETWEEN) {

			return parseBetween(values);

		}

		//in last n time units
		else if (operator == ConstraintOperator.IN || operator == ConstraintOperator.EQ) {

			return parseIn(values);

		}

		//on or before/after a particular point in time
		else if (operator == ConstraintOperator.LTE || operator == ConstraintOperator.GTE) {

			if (values.size() != 1) {
				throw new InvalidFilterException("timestamp " + operator.name().toLowerCase() + " must have a single date as a value");
			}
			return Collections.singletonList(parseDate(values.get(0)));

		}

		else {
			throw new InvalidFilterException("unsupported operator type for timestamp: " + operator.name().toLowerCase());
		}

	}

	//list of date values
	private List<Object> parseBetween(List<String> values) {

		List<Object> ret = new ArrayList<Object>();
		for (String value : values) {

			ret.add(parseDate(value));

		}

		return ret;

	}

	private Instant parseDate(Object value) {

		//TODO: more efficiently handle Long objects
		String strValue = String.valueOf(value);

		//always express as a date value
		Date time = null;

		try {

			//try numeric timestamp first
			try {
				time = new Date(Long.valueOf(strValue));
			} catch (NumberFormatException e) {

				//try formatted version
				time = FORMATTED.parse(strValue);

			}

		} catch (Exception e) {
			throw new InvalidFilterException("error parsing timestamp " + value + ": " + e.getMessage());
		}

		if (time == null) {
			throw new InvalidDataException("couldn't parse date " + value);
		}

		return time.toInstant();

	}

	//last N time units
	private List<Object> parseIn(List<String> values) {

		if (values.size() < 1) {
			throw new InvalidFilterException("timestamp in clause needs at least 1 value, the time range");
		}

		List<Object> ret = new ArrayList<Object>();
		ret.add(TimeRange.valueOf(values.get(0).toUpperCase())); //last_n

		//parse N units
		if (values.size() == 2) {
			ret.add(Integer.parseInt(values.get(1))); //value for n
		}

		return ret;

	}

}

