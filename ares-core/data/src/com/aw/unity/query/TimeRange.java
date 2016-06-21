package com.aw.unity.query;

import java.util.Calendar;

/**
 * Available time ranges for a query
 *
 *
 */
public enum TimeRange {
	TODAY,
	YESTERDAY,

	THIS_WEEK,
	THIS_MONTH,
	THIS_QUARTER,
	THIS_YEAR,

	LAST_WEEK,
	LAST_MONTH,
	LAST_QUARTER,
	LAST_YEAR,

	LAST_N_MINUTES(Calendar.MINUTE),
	LAST_N_HOURS(Calendar.HOUR),
	LAST_N_DAYS(Calendar.DAY_OF_MONTH),
	LAST_N_WEEKS(Calendar.WEEK_OF_YEAR),
	LAST_N_MONTHS(Calendar.MONTH),
	LAST_N_QUARTERS(Calendar.MONTH, 3),
	LAST_N_YEARS(Calendar.YEAR);

	private TimeRange() {
	}

	private TimeRange(int calField) {
		this(calField, 1);
	}

	private TimeRange(int calField, int addAmt) {
		m_calField = calField;
		m_calendarFieldCount = addAmt;
	}

	/**
	 * @return The calendar field corresponding to this range's time unit
	 */
	public int getCalendarField() { return m_calField; }
	public void setCalendarField(int calField) { m_calField = calField; }
	private int m_calField = -1;

	/**
	 * @return The number of calendar units this range represents
	 */
	public int getCalendarFieldCount() { return m_calendarFieldCount; }
	public void setCalendarFieldCount(int addAmount) { m_calendarFieldCount = addAmount; }
	private int m_calendarFieldCount;

}
