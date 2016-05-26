package com.aw.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of key -> key -> value
 *
 *
 *
 */
public class TwoKeyMap<K1, K2, V> extends HashMap<K1, Map<K2, V>> {

	private static final long serialVersionUID = 1L;

	public void put(K1 key1, K2 key2, V value) {

		//get the inner map, create it if it doesn't exist
		Map<K2, V> inner = get(key1);
		if (inner == null) {
			inner = new HashMap<K2, V>();
			put(key1, inner);
		}

		//put the value
		inner.put(key2, value);

	}

	public V get(K1 key1, K2 key2) {

		Map<K2, V> inner = get(key1);

		//no inner map means null return
		if (inner == null) {
			return null;
		}

		return inner.get(key2);

	}

}
