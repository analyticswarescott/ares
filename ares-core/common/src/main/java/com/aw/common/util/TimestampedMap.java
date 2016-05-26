package com.aw.common.util;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map whose entries will be refreshed after the TTL for that specific key has been reached. The ValueSource
 * will be used to retrieve new values when needed based on the max age. The map will not grow past maxSize,
 * least recently used keys will be discarded when max size is reached.
 *
 *
 *
 */
public class TimestampedMap<K, V> {

	private TimeSource timeSource;
	private ValueSource<K, V> values;
	protected Map<K, TimestampedValue> map;
	private Duration ttl;
	private int maxSize;

	public TimestampedMap(TimeSource time, ValueSource<K, V> values, Duration maxAge, int maxSize) {

		this.timeSource = time;
		this.values = values;
		this.ttl = maxAge;
		this.maxSize = maxSize;
		this.map = new InnerMap();

	}

	public int size() {
		return map.size();
	}

	public V get(K key) {

		TimestampedValue value = map.get(key);

		//if no value yet for this key
		if (value == null) {

			//create a new TimestampedValue for this key and add it to the map
			value = new TimestampedValue(this.values.get(key));
			map.put(key, value);

		}

		//else if the value is past the max age
		else if (Utils.isStale(value.timestamp, ttl, now())) {

			//refresh the value
			value.set(this.values.get(key));

		}

		//the value we have at this point is the one to return
		return value.get();

	}

	/**
	 * @return Get now
	 */
	private Instant now() {
		return this.timeSource.now();
	}

	/**
	 * A value with associated creation time
	 */
	public class TimestampedValue {

		//the time this value was retrieved
		private Instant timestamp;

		/**
		 * Creates a timestamped value at the current time
		 *
		 * @param value The value to set
		 */
		public TimestampedValue(V value) {
			set(value);
		}

		/**
		 * @return The value - when set, timestamp is updated automatically
		 */
		private V get() { return this.value;  }
		private void set(V value) {
			this.value = value;
			this.timestamp = now();
		}
		private V value;

	}

	/**
	 * The backing map of a max size, MRU cache
	 *
	 *
	 *
	 */
	private class InnerMap extends LinkedHashMap<K, TimestampedValue> {

		private static final long serialVersionUID = 1L;

		public InnerMap() {
			//access order
			super(16, .75F, true);
		}
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, TimestampedValue> eldest) {
			//don't grow past max size, least recently used elements go first
			return size() > TimestampedMap.this.maxSize;
		}

	}

}
