package com.aw.compute.referencedata;

import com.aw.common.exceptions.ProcessingException;

/**
 * A reference data object that allows for the lookup of values by keys.
 *
 *
 *
 * @param <T>
 */
public interface ReferenceDataMap<K, V> extends ReferenceDataCollection {

	/**
	 * Get a value given a key.
	 *
	 * @param key The key to look up.
	 * @return The value looked up, or null if no value is present for this key.
	 */
	public V get(K key) throws ProcessingException;

}
