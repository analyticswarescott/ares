package com.aw.compute.referencedata.geo;

import java.time.Duration;

import javax.inject.Inject;

import com.aw.common.util.TimeSource;
import com.aw.compute.referencedata.AbstractTimestampedReferenceDataMap;

/**
 * A source of geolocation enrichment data for ip addresses
 *
 * TODO: inject location source and make maxAge and maxSize configurable
 *
 *
 *
 */
public class GeoLocationLookupData extends AbstractTimestampedReferenceDataMap<String, GeoLocation> {

	/**
	 * Default maximum age of a geo location before we will retrieve it again
	 */
	private static final Duration DEFAULT_MAX_AGE = Duration.ofMinutes(10);

	/**
	 * Default maximum size of a geo location lookup map
	 */
	private static final int DEFAULT_MAX_SIZE = 100000;

	@Inject @com.google.inject.Inject
	public GeoLocationLookupData(TimeSource timeSource, GeoLocationSource geoSource) {
		super(timeSource, geoSource, DEFAULT_MAX_AGE, DEFAULT_MAX_SIZE);
	}

	/**
	 * Geo location lookup data using the given TimeSource
	 *
	 * @param timeSource The source of current time
	 * @param source The source of fresh geo locations
	 * @param maxAge The maximum age before requesting fresh geo data for an ip
	 * @param maxSize The maximum cache size
	 */
	public GeoLocationLookupData(TimeSource timeSource, GeoLocationSource source, Duration maxAge, int maxSize) {
		super(timeSource, source, maxAge, maxSize);
	}

	/**
	 * Geo location lookup data using system clock
	 *
	 * @param source The source of fresh geo locations
	 * @param maxAge The maximum age before requesting fresh geo data for an ip
	 * @param maxSize The maximum cache size
	 */
	public GeoLocationLookupData(GeoLocationSource source, Duration maxAge, int maxSize) {
		this(TimeSource.SYSTEM_TIME, source, maxAge, maxSize);
	}

}
