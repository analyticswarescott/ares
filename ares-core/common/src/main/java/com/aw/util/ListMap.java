package com.aw.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A map of lists
 *
 *
 *
 * @param <K> The key type for the lists
 * @param <V> The type of data in the lists
 */
public class ListMap<K, V> extends CollectionMap<K, V, List<V>> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<V> emptyCollection() {
		return Collections.emptyList();
	}

	@Override
	protected List<V> newCollection() {
		return new ArrayList<V>();
	}

	/**
	 * if there is at least one value for the key, return the last value in the list, otherwise return null
	 *
	 * @param key the key whose last value is needed
	 */
	public V getLastOrNull(K key) {

		List<V> ret = get(key);

		if (ret != null && ret.size() > 0) {
			return ret.get(ret.size() - 1);
		}

		else {
			return null;
		}

	}

	/**
	 * number of elements that are in the ListMap for the provided key
	 *
	 * @param key the key
	 * @return the number of elements associated with the key
	 */
	public int size(K key) {

		List<V> values = get(key);

		return values == null ? 0 : values.size();

	}

}
