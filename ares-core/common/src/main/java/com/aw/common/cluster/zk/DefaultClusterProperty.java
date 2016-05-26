package com.aw.common.cluster.zk;

import java.io.IOException;

import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.log4j.Logger;

import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.ClusterProperty;
import com.aw.common.util.JSONUtils;
import com.aw.util.Statics;
import com.google.common.base.Preconditions;

/**
 * Default implementation for a cluster property, using curator's NodeCache.
 *
 *
 *
 * @param <T> The type of property
 */
public class DefaultClusterProperty<T> implements ClusterProperty<T>, NodeCacheListener {

	private static final Logger logger = Logger.getLogger(DefaultClusterProperty.class);

	private Class<T> type;
	private NodeCache cache;
	private String name;
	private T unknown;
	private ZkCluster cluster;

	/**
	 * Initialize the property
	 * @param type The type of property
	 * @param framework The curator client
	 * @param name The name of the property, its toString() will be used as the key for this property
	 * @param unknown If the value cannot be retrieved, this will be returned instead
	 */
	public DefaultClusterProperty(Class<T> type, ZkCluster cluster, Object name, T unknown) throws ClusterException {
		this.type = type;
		this.name = String.valueOf(name);
		this.cluster = cluster;
		this.unknown = unknown;
		initCache();

	}

	/**
	 * Notifies any waiters that the property has changed
	 */
	@Override
	public synchronized void nodeChanged() throws Exception {
		notifyAll();
	}

	private void initCache() throws ClusterException {

		String clusterPath = "none";
		try {

			//listen for cluster state
			clusterPath = cluster.getClusterPath();
			cache = new NodeCache(cluster.getClient(), cluster.getClusterPath() + "/" + name);
			cache.getListenable().addListener(this);

			logger.debug(" about to start NodeCache ");
			cache.start();
			logger.debug(" NodeCache started ");

		} catch (Exception e) {
			throw new ClusterException("error getting state cache for rest cluster at path " + clusterPath, e);
		}

	}

	public T get() throws ClusterException {

		synchronized (this) {
			Preconditions.checkNotNull(cache, "state node is null, cluster not properly initialized and watching state - this: " + this);
		}

		try {

			if (cache.getCurrentData() == null) {
				return unknown;
			}

			else {
				return cluster.getSystemZkAccessor().get(cache.getCurrentData().getData(), this.type);
			}

		} catch (Exception e) {
			throw new ClusterException("error getting state", e);
		}

	}

	public void set(T value) throws ClusterException {
		try {

			String path = cluster.getClusterPath() + "/" + name;

			//builder the update state action differently depending on whether the path exists
			PathAndBytesable<?> updateState = null;

			//create node if it doesn't exist
			if (cluster.getClient().checkExists().forPath(path) == null) {
				updateState = cluster.getClient().create();
			}

			else {

				//put the state in the reserved state node
				updateState = cluster.getClient().setData();

			}

			//get the string data we'll be setting
			String str = JSONUtils.objectToString(value);

			//update the data on the path
			updateState.forPath(path, str.getBytes(Statics.CHARSET));

		} catch (Exception e) {
			throw new ClusterException("error setting state", e);
		}
	}

	@Override
	public synchronized void waitFor(T value, long timeout) throws ClusterException {
    	try {

			//wait up to timeout millis
			long start = System.currentTimeMillis();
			long timeToWait = timeout;

			//while we haven't reached the state we're looking for, wait
			while (get() != value) {

				//if we have waited long enough, break
				if (timeToWait <= 0L) {

					break;

				} else {

					//wait as long as we still need to
					wait(timeToWait);

				}

				//figure out how much more time we need to wait
				timeToWait = timeout - (System.currentTimeMillis() - start);

			}

    	} catch (Exception e) {
    		throw new ClusterException("error waiting for platform state", e);
    	}

    	T finalValue = get();
		if (get() != value) {
			throw new ClusterException("timeout waiting for " + this.name + "=" + value + ", current value is " + finalValue);
		}

	}

	public void cleanup() throws IOException {
		this.cache.close();
	}

}
