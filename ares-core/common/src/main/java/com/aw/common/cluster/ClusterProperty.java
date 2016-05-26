package com.aw.common.cluster;

/**
 * A cluster property
 *
 *
 *
 * @param <T>
 */
public interface ClusterProperty<T> {

	public T get() throws ClusterException;

	/**
	 * Wait for the given value on the property. A cluster exception will be thrown if
	 * the timeout is reached.
	 *
	 * @param value The value to wait for
	 * @param timeout The maximum time to wait
	 * @throws ClusterException If anything goes wrong
	 */
	public void waitFor(T value, long timeout) throws ClusterException;

}
