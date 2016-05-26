package com.aw.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A map of sets.
 *
 *
 *
 * @param <K> The key type for the sets
 * @param <V> The type stored in the set
 */
public class SetMap<K, V> extends CollectionMap<K, V, Set<V>> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Set<V> emptyCollection() {
		return Collections.emptySet();
	}

	@Override
	protected Set<V> newCollection() {
		return new HashSet<V>();
	}

}
