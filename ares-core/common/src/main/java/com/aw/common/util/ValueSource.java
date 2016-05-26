package com.aw.common.util;

/**
 * A source of values given a key
 *
 *
 *
 * @param <K> The key
 * @param <V> The value
 */
public interface ValueSource<K, V> {

	public V get(K key);

}
