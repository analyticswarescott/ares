package com.aw.common.cluster.db;

import org.apache.curator.framework.CuratorFramework;

/**
 * An object to allow distributed operation sequence management (e.g. clustered database writes)
 */
public interface OpSequencer {

	/**
	 * Returns an OpSquenceListener that can be used to get or increment values for a given key
	 * @param client
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public  OpSequenceListener getSequencer(CuratorFramework client, String key)  throws Exception;

	/**
	 * get next shared sequence value
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public int incrementSequence(CuratorFramework client, String key) throws Exception;

	/**
	 * get current shared sequence value
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public int getSequenceValue(CuratorFramework client, String key) throws Exception;

	/**
	 * Provides the Zookeeper path for a given key
	 * @param key
	 * @return
	 */
	public String getSequencePath(String key);

	public void close() throws  Exception;
	public void closeCurrentTenant() throws  Exception;

}
