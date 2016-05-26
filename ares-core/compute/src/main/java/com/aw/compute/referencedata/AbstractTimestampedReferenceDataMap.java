package com.aw.compute.referencedata;

import java.time.Duration;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.TimeSource;
import com.aw.common.util.TimestampedMap;
import com.aw.common.util.ValueSource;

public class AbstractTimestampedReferenceDataMap<K, V> extends TimestampedMap<K, V> implements ReferenceDataMap<K, V> {

	public AbstractTimestampedReferenceDataMap(TimeSource time, ValueSource<K, V> values, Duration maxAge,
			int maxSize) {
		super(time, values, maxAge, maxSize);
	}

	@Override
	public void refreshNow() throws ProcessingException {
		map.clear();
	}

	public ReferenceDataManager getManager() { return this.manager;  }
	public void setManager(ReferenceDataManager manager) { this.manager = manager; }
	private ReferenceDataManager manager;

}
