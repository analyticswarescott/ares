package com.aw.common.util;

import java.util.Optional;

/**
 * Use this in conjunction with {@link JSONUtils#processLeaf(Object, String, JSONUtilsNodeProcessor)}
 * to easily manipulate json nodes
 * @param <T>
 */
public interface JSONUtilsNodeProcessor<T> {
	Optional<T> accept(Object node, String key, Object parent);
}
