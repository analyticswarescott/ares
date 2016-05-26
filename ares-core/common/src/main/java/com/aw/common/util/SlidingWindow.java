package com.aw.common.util;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Counts occurrences of something over a sliding window
 *
 *
 *
 * @param <T> The type of object that is stored in the sliding window
 */
public class SlidingWindow<T> {

	public static long NOT_FOUND = -1L;

	/**
	 * Create a sliding window with no out of order tolerance
	 *
	 * @param unit The window time unit
	 * @param unitCount The number of time units in the window
	 */
	public SlidingWindow(ChronoUnit unit, int unitCount) {
		this(unit, unitCount, null);
	}

	/**
	 * Create a sliding window with the given time order tolerance, using the default time source
	 * (system current time)
	 *
	 * @param unit The window time unit
	 * @param unitCount The number of time units in the window
	 * @param tolerance The out of order tolerance of the objects stored in the window
	 */
	public SlidingWindow(ChronoUnit unit, int unitCount, Duration tolerance) {

		//use system or latest time for current time
		this(unit, unitCount, tolerance, new SystemOrLatestTime());

	}

	/**
	 * Creates a time window of the given length, tolerance, and time source
	 *
	 * @param unit The window time unit
	 * @param unitCount The number of time units in the window
	 * @param tolerance The out of order tolerance of the objects stored in the window
	 * @param timeSource The source of the current time
	 */
	public SlidingWindow(ChronoUnit unit, int unitCount, Duration tolerance, TimeSource timeSource) {

		this.timeUnit = unit;
		this.timeUnitCount = unitCount;
		this.tolerance = tolerance;
		this.timeSource = timeSource;

		//we are aware of SystemOrLatestTime for keeping track of current time
		if (this.timeSource instanceof SystemOrLatestTime) {
			this.feedableTime = (SystemOrLatestTime)this.timeSource;
		}

		//get tolerance or 0 if none
		long lTolerance = tolerance == null ? 0L : tolerance.toMillis();

		//compute the length of this sliding window
		this.length = unit.getDuration().toMillis() * (long)unitCount + lTolerance;

		this.end = currentTime();
		this.start = this.end - this.length;

	}

	public void add(T data, Instant time) {

		//slide the window
		slide();

		//get the long representation
		long lTime = time.toEpochMilli();

		//add if it's in range
		if (this.start <= lTime && this.end >= lTime) {
			this.window.add(new Element<T>(data, lTime));
		}

		//update latest time if applicable
		if (this.feedableTime != null) {
			this.feedableTime.updateLatest(time);
		}

	}

	/**
	 * Find the given number of occurrences in a time period. If found, the start time will be returned.
	 *
	 * This method is NOT thread safe.
	 *
	 * @param number The number of matches required
	 * @param range The time range in which the matches must occur
	 * @param start The earliest time the matches can occur
	 * @return The start time of the match
	 */
	public Deque<Element<T>> find(int number, Duration range, long start) {

		//return value
		Deque<Element<T>> ret = null;

		//clear from last time, if applicable
		elements.clear();

		//short circuit to avoid memory burn on empty or small lists
		if (getTotalCount() < number) {
			return ret;
		}

		//get the duration in millis
		long lDuration = range.toMillis();

		//look through the current matches
		Iterator<Element<T>> iter = this.window.iterator();
		while (iter.hasNext()) {

			Element<T> current = iter.next();

			//determine if we add this match
			if (current.getTime() >= start) {
				 elements.add(current);
			} else {
				continue;
			}

			//determine current time range
			long firstTime = elements.getFirst().getTime();
			long lastTime = elements.getLast().getTime();

			//remove elements outside of the max duration
			while (lastTime - firstTime > lDuration) {
				 elements.removeFirst();

				//update first time
				firstTime = elements.getFirst().getTime();
			}

			//if we have enough elements, we're done
			if (count(this.elements) == number) {
				break;
			}

		}

		//check if we have what we need
		if (count(elements) == number) {

			//remove from the set
			this.window.removeAll(elements);

			//return the built list
			ret = elements;

			//prime the list for the next find call
			this.elements = new LinkedList<Element<T>>();

		}

		//return what we have
		return ret;

	}

	private LinkedList<Element<T>> elements = new LinkedList<Element<T>>();

	/**
	 * Counts the number of occurrences for the given collection of matches. Default implementation assumes 1 to 1
	 * TYPE to count.
	 *
	 * @param matches The matches to count
	 * @return The count
	 */
	protected int count(Collection<Element<T>> matches) {
		return matches.size();
	}

	/**
	 * @return Total count in the window
	 */
	public int getTotalCount() {
		return count(this.window);
	}

	/**
	 * Slide the set forward, removing old elements
	 */
	private void slide() {

		//compute new start/end
		this.end = currentTime();
		this.start = this.end - this.length;

		//remove all matches outside of this window
		Iterator<Element<T>> iter = this.window.iterator();
		while (iter.hasNext()) {

			//remove matches before the current window
			if (iter.next().getTime() < this.start) {
				iter.remove();
			}

		}

	}

	protected long currentTime() {
		return this.timeSource.nowMillis();
	}

	/**
	 * @return The source for the current time
	 */
	public TimeSource getTimeSource() { return this.timeSource; }
	private TimeSource timeSource = TimeSource.SYSTEM_TIME;

	public ChronoUnit getTimeUnit() { return this.timeUnit; }
	private ChronoUnit timeUnit;

	public int getTimeUnitCount() { return this.timeUnitCount; }
	private int timeUnitCount;

	public Duration getTolerance() { return this.tolerance; }
	private Duration tolerance;

	//current window start
	private long start;

	//current window end
	private long end;

	//window length
	private long length;

	//if we have a SystemOrLatestTime, this will be set
	private SystemOrLatestTime feedableTime;

	/**
	 * A bucket is an atomic unit within the sliding window, representing one time unit.
	 *
	 *
	 *
	 * @param <MT> The type of data in the match
	 */
	public static class Element<MT> implements Comparable<Element<MT>> {

		@Override
		public int compareTo(Element<MT> o) {
			long diff = this.time - o.time;
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Create a match
		 *
		 * @param data The data that matched
		 * @param time The time the data matched
		 */
		public Element(MT data, long time) {
			this.data = data;
			this.time = time;
		}

		/**
		 * The data that matched
		 */
		public MT getData() { return this.data; }
		MT data;

		/**
		 * The time the data occurred (i.e. the time the match occurred)
		 */
		public long getTime() { return this.time; }
		long time;

	}

	/**
	 * The sliding window of buckets
	 */
	SortedSet<Element<T>> window = new TreeSet<Element<T>>();

}
