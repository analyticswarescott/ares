package com.aw.common.task;

import java.text.ParseException;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;

/**
 * Execution policy for a task
 *
 * TODO: implement full recurrence, possibly use quartz job framework for tasks?
 *
 *
 *
 */
public class TaskSchedule {

	public enum Type {

		/**
		 * task runs once, asap
		 */
		NOW,

		/**
		 * perpetual tasks should always have one instance running
		 */
		PERPETUAL,

		/**
		 * these tasks run at a set times
		 */
		RECURRING;

		@JsonCreator
		public static Type fromString(String str) {
			return valueOf(str.toUpperCase());
		}

	}

	/**
	 * a task schedule representing perpetually running tasks
	 */
	public final static TaskSchedule PERPETUAL = new TaskSchedule(Type.PERPETUAL);

	/**
	 * a task schedule representing tasks that should run immediately
	 */
	public final static TaskSchedule NOW = new TaskSchedule(Type.NOW);

	public TaskSchedule() {
	}

	public TaskSchedule(Type type) {
		Preconditions.checkArgument(type != Type.RECURRING, "a recurrence pattern must be specified for a recurring task");
		this.type = type;
		this.recurrencePattern = null;
	}

	public TaskSchedule(Type type, String recurrence) throws ParseException {
		this.type = type;
		if (recurrence != null) {
			this.recurrencePattern = RecurrencePattern.valueOf(recurrence);
		}
	}

	public Instant nextTimeAfter(Instant time) {
		switch (type) {
			case NOW: return Instant.MIN; //immediately
			case PERPETUAL: return Instant.MIN; //immediately
			case RECURRING: return recurrencePattern.nextTimeAfter(time); //based on recurrence
			default: throw new UnsupportedOperationException("invalid schedule type for task: " + type);
		}
	}

	/**
	 * @return the schedule type
	 */
	public Type getType() { return type; }
	private Type type;

	public RecurrencePattern getRecurrencePattern() { return this.recurrencePattern;  }
	public void setRecurrencePattern(RecurrencePattern recurrencePattern) { this.recurrencePattern = recurrencePattern; }
	private RecurrencePattern recurrencePattern;


}
