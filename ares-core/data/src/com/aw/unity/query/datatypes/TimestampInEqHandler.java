package com.aw.unity.query.datatypes;

import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.OpHandler;
import com.aw.unity.query.TimeRange;

/**
 * Same as in handler
 *
 *
 *
 */
public class TimestampInEqHandler implements OpHandler {

	public TimestampInEqHandler(ConstraintOperator op) {
		m_operator = op;
	}


	@Override
	public ConstraintOperator getOperator() {
		return m_operator;
	}
	private ConstraintOperator m_operator;

	public static Instant getStartTime(FilterConstraint constraint) {
		return getStartTime(constraint.getValues());
	}

	public static Instant getEndTime(FilterConstraint constraint) {
		return getEndTime(constraint.getValues(), getStartTime(constraint.getValues()));
	}

	private static Instant getStartTime(Collection<Object> values) {

		Iterator<Object> valueIter = values.iterator();

		//get the time range enum value first
		TimeRange range = (TimeRange)valueIter.next();

		Date ret = null;

		//single unit
		if (values.size() == 1) {

			//set start time based on range
			switch (range) {

				case TODAY:
					//done already
					ret = getDate(StartOf.DAY);
					break;
				case THIS_WEEK:
					ret = getDate(StartOf.WEEK);
					break;
				case THIS_MONTH:
					ret = getDate(StartOf.MONTH);
					break;
				case THIS_YEAR:
					ret = getDate(StartOf.YEAR);
					break;
				case THIS_QUARTER:
					ret = getDate(StartOf.QUARTER);
					break;
				case YESTERDAY:
					ret = getDate(StartOf.DAY, Calendar.DAY_OF_MONTH, -1);
					break;
				case LAST_WEEK:
					ret = getDate(StartOf.WEEK, Calendar.WEEK_OF_YEAR, -1);
					break;
				case LAST_MONTH:
					ret = getDate(StartOf.MONTH, Calendar.MONTH, -1);
					break;
				case LAST_QUARTER:
					ret = getDate(StartOf.QUARTER, Calendar.MONTH, -3);
					break;
				case LAST_YEAR:
					ret = getDate(StartOf.YEAR, Calendar.YEAR, -1);
					break;

				default:
					throw new InvalidFilterException("invalid time range for a single value: " + range);
			}
		}

		//n units
		else if (range.getCalendarField() > 0) {
			int n = ((Number)valueIter.next()).intValue();

			Calendar cal = s_calendar.get();
			cal.setTimeInMillis(System.currentTimeMillis());

			//subtract the appropriate time to get the start time we need
			cal.add(range.getCalendarField(), -1 * range.getCalendarFieldCount() * n);

			ret = cal.getTime();
		}

		else {
			throw new InvalidFilterException("unsupported range / values combination range=" + range + ", values=" + values);
		}

		return ret.toInstant();

	}

	private static Instant getEndTime(Collection<Object> values, Instant start) {

		Iterator<Object> valueIter = values.iterator();

		//calendars are expensive..
		Calendar cal = s_calendar.get();
		cal.setTime(Date.from(start));

		//get the time range enum value first
		TimeRange range = (TimeRange)valueIter.next();
		switch (range) {
			case YESTERDAY:
				cal.add(Calendar.DAY_OF_MONTH, 1);
				break;
			case LAST_WEEK:
				cal.add(Calendar.WEEK_OF_YEAR, 1);
				break;
			case LAST_MONTH:
				cal.add(Calendar.DAY_OF_MONTH, 1);
				break;
			case LAST_QUARTER:
				cal.add(Calendar.MONTH, 3);
				break;
			case LAST_YEAR:
				cal.add(Calendar.DAY_OF_MONTH, 1);
				break;

			default:
				return null; //no end time in this case
		}

		return cal.getTime().toInstant();

	}

	@Override
	public boolean match(Object lValue, Collection<Object> values) {

		Instant start = getStartTime(values);
		Instant end = getEndTime(values, start);

		boolean startGood = !((Instant)lValue).isBefore(start);

		//make sure it's also either before the end time, or end is not defined
		return (startGood && (end == null || end.isAfter((Instant)lValue)));

	}

	//TODO: this shouldn't be a leak as we shouldn't have many short lived threads..
	//this should be made into a service if we run into that problem
	private static ThreadLocal<Calendar> s_calendar = new ThreadLocal<Calendar>() {
		@Override
		protected Calendar initialValue() {
			return Calendar.getInstance(); //current time
		}
	};

	/**
	 * Start of a particular time unit
	 */
	public enum StartOf {
		SECOND,
		MINUTE,
		HOUR,
		DAY,
		WEEK,
		MONTH,
		QUARTER,
		YEAR
	}

	/**
	 * convenience method for testing dates
	 * @param startOf what level to zero out to start at a specific time unit
	 * @param fields The fields to add to the calendar, along with add amount - these values must come in pairs or an exception will be thrown
	 * @return
	 */
	static Date getDate(StartOf start, int... fields) {

		//start with current time
		Calendar cal = s_calendar.get();
		cal.setTimeInMillis(System.currentTimeMillis());

		if (fields != null && fields.length % 2 != 0) {
			throw new UnsupportedOperationException("ints must be in multiples of 2: Calendar.FIELD, addAmount, etc etc");
		}

		//set up calendar accordingly
		switch (start) {
			case YEAR:
				cal.set(Calendar.DAY_OF_YEAR, 1);
			case QUARTER:
				cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH) / 3) * 3);
			case MONTH:
				cal.set(Calendar.DAY_OF_MONTH, 1);
			case WEEK:
				//set it after, it will screw with stuff above
			case DAY:
				cal.set(Calendar.HOUR_OF_DAY, 0);
			case HOUR:
				cal.set(Calendar.MINUTE, 0);
			case MINUTE:
				cal.set(Calendar.SECOND, 0);
			case SECOND:
				cal.set(Calendar.MILLISECOND, 0);
		}

		if (start == StartOf.WEEK) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		}

		if (fields != null) {
			for (int x=0; x<fields.length; x+=2) {
				cal.add(fields[x], fields[x+1]);
			}
		}

		return cal.getTime();

	}

}
