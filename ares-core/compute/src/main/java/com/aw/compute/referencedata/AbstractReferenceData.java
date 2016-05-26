package com.aw.compute.referencedata;

import java.time.Duration;
import java.time.Instant;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.TimeSource;
import com.aw.common.util.Utils;

/**
 * Default base class for all reference data that is refreshed all at once
 *
 *
 */
public abstract class AbstractReferenceData implements ReferenceData {

	/**
	 * Default time to live for reference data
	 */
	public static final Duration DEFAULT_TTL = Duration.ofSeconds(5);

	//make this configurable
	TimeSource timeSource = TimeSource.SYSTEM_TIME;

	/**
	 * Time to live on this reference data in milliseconds. If the current reference data is older than this, new reference data will
	 * be automatically pulled the next time data is requested.
	 *
	 * @return The ttl for this reference data
	 */
	public Duration getTTL() { return ttl; }
	public void setTTL(Duration ttl) { this.ttl = ttl; }
	private Duration ttl = DEFAULT_TTL;

	/**
	 * @return The current age of this reference data in milliseconds.
	 */
	public Duration getAge() {
		return Duration.between(lastPull, timeSource.now());
	}

	@Override
	public void refreshNow() throws ProcessingException {

		//next time data is requested, we will refresh
		lastPull = timeSource.now();

	}

	/**
	 * Check if we need to update - this is thread safe, so reference data can be accessed by many threads at once
	 */
	public synchronized void check() throws ProcessingException {

		//first check TTL - if not old enough, return
		if (!isStale()) {
			return;
		}

		onTTLExpired();

		//this should only be executed if no exceptions occurred
		setLastPull(timeSource.now());

	}

	/**
	 * Framework has detected the ttl has expired, a refresh check is needed
	 */
	protected abstract void onTTLExpired() throws ProcessingException;

	/**
	 * We're stale if the last time we pulled plus the time to live is before the current time
	 */
	protected boolean isStale() {
		return Utils.isStale(lastPull, ttl, timeSource.now());
	}

	/**
	 * @return The most recent pull of reference data, in milliseconds
	 */
	public Instant getLastPull() { return lastPull; }
	public void setLastPull(Instant lastPull) { this.lastPull = lastPull; }
	private Instant lastPull = Instant.ofEpochMilli(0L); //we've never pulled

	public ReferenceDataManager getManager() { return manager; }
	public void setManager(ReferenceDataManager manager) { this.manager = manager; }
	private ReferenceDataManager manager;

}
