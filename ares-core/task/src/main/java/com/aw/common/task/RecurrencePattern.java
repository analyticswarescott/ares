package com.aw.common.task;

import java.sql.Date;
import java.text.ParseException;
import java.time.Instant;

import org.quartz.CronExpression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * a recurrence pattern within the product
 *
 *
 *
 */
@JsonSerialize(using=ToStringSerializer.class, as=String.class)
public class RecurrencePattern {

	/**
	 * the cron expression
	 */
	private CronExpression cron;

	private RecurrencePattern(String cronString) throws ParseException {
		cron = new CronExpression(cronString);
	}

	public Instant nextTimeAfter(Instant time) {
		return cron.getNextValidTimeAfter(Date.from(time)).toInstant();
	}

	@Override
	public String toString() {
		return cron.getCronExpression();
	}

	@JsonCreator
	public static RecurrencePattern valueOf(String pattern) throws ParseException {
		return new RecurrencePattern(pattern);
	}

}
