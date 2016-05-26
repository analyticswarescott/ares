package com.aw.util;

import java.util.Collection;
import java.util.HashMap;

/**
 * A map of collections
 *
 *
 *
 * @param <K> The key used in the map
 * @param <V> The type stored in the collection
 * @param <C> The collection type
 */
public abstract class CollectionMap<K, V, C extends Collection<V>> extends HashMap<K, C> {

	private static final long serialVersionUID = 1L;

	/**
	 * If nothing has been added for this key, will return an immutable empty list as the value.
	 */
	@Override
	public C get(Object key) {
		C ret = super.get(key);

		if (ret == null) {
			ret = emptyCollection();
		}

		return ret;
	}

	/**
	 * Add a value to the list belonging to K - create a list if it doesn't already exist
	 *
	 * @param key
	 * @param value
	 * @return If a new key was added
	 */
	public boolean add(K key, V value) {

		boolean ret = false;
		C values = super.get(key);

		//create a list if it doesn't already exist
		if (values == null) {

			//return that we had to create a new list
			ret = true;

			//create the new list and store it
			values = newCollection();
			put(key, values);

		}

		//add the value
		values.add(value);

		//we're done
		return ret;

	}

	/**
	 * @return A new typed collection
	 */
	protected abstract C newCollection();

	/**
	 * @return An empty collection
	 */
	protected abstract C emptyCollection();

}
