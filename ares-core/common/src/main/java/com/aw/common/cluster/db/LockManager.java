package com.aw.common.cluster.db;

import com.aw.common.cluster.db.exceptions.LockNotFoundException;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.TimeUnit;

/**
 * Created by scott on 24/02/16.
 */
public interface LockManager {


	/**
	 * return Zookeeper path for a key
	 * @param key
	 * @return
	 */
	String getLockPath(String key);

	/**
	 * Acquire a lock, blocking with a timeout
	 * @param client
	 * @param key
	 * @return
	 * @throws Exception
	 */
	CuratorLockWrapper acquireLock(CuratorFramework client, String key) throws Exception;


	/**
	 * Release a lock at an explicit path
	 * @param key
	 * @throws LockNotFoundException
	 */
	void releaseLock(String key) throws LockNotFoundException;

	void setAcquireTimeout(long timeout, TimeUnit timeUnit) throws Exception;
}
