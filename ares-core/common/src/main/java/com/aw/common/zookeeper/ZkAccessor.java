package com.aw.common.zookeeper;

import java.io.Closeable;
import java.util.List;

import com.aw.common.cluster.ClusterException;
import com.aw.common.zookeeper.structure.ZkPurpose;

/**
 * Created by scott on 18/04/16.
 */
public interface ZkAccessor extends Closeable {



	public void init() throws Exception;

	/**
	 * Check if key exists in cluster cache
	 *
	 * @param key The key
	 * @throws ClusterException If any errors occur
	 */
	public boolean exists(ZkPurpose purpose, Object key) throws Exception;

	/**
	 * Check if key exists using absolute path - typically for 3p zookeeper stores
	 *
	 * @param key The key
	 * @throws ClusterException If any errors occur
	 */
	public boolean exists(String path) throws Exception;


	void ensure(String path) throws ClusterException;

	/**
	 * Put global cluster data for this cluster into the cluster's global state. The key and object serialization details
	 * will be specific to the cluster type.
	 *
	 * @param member The member that is adding data
	 * @param key The key
	 * @param value The value
	 * @throws ClusterException If any errors occur
	 */
	public void put(ZkPurpose purpose, Object key, Object value) throws Exception;

	/**
	 * Get the cluster value from the cluster's global state, or null if no value exists for the provided key.
	 *
	 * @param key The key
	 * @param type The return type
	 * @return The value
	 * @throws ClusterException If anything goes wrong
	 */
	public <T> T get(ZkPurpose purpose, Object key, Class<T> type) throws Exception;

	//TODO: can we elimiate non-Purposed gets by absolute path?
	public <T> T get( String path, Class<T> type) throws Exception;

	/**
	 * deserialize all java objects under the given base key, checking recursively for objects
	 *
	 * @param baseKey the base key under which the objects can be found
	 * @param type the type of object stored in the leaf nodes. all leaf nodes must contain the same object type
	 * @return the list of objects contain in the leaf node data
	 * @throws Exception if anything goes wrong
	 */
	public <T> List<T> getAll(String path, Class<T> type) throws Exception;

	public <T> List<T> getAll(ZkPurpose purpose, String baseKey, Class<T> type) throws Exception;

	public List<String> getChildren(String path) throws Exception;

	/**
	 * Remove object at path
	 * @param key
	 * @throws ClusterException
	 */
	public void delete(ZkPurpose purpose, Object key) throws Exception;


	public String getAbsoluteZkPath(ZkPurpose purpose, String key);

	public void cleanup();

	public StringBuffer listZK(String path, boolean excludeKnown3pRoots) throws Exception;



}
