package com.aw.common.cluster.db;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import java.util.concurrent.TimeUnit;

/**
 * A wrapper to store time and timeout data with a lock
 */
public class CuratorLockWrapper {

	private InterProcessSemaphoreMutex theLock;

	private long lockTime = 0;

	public CuratorLockWrapper() {
		theLock = null;
	}

	public CuratorLockWrapper(CuratorFramework client, String path) {

		theLock = new InterProcessSemaphoreMutex(client, path);

	}

	public boolean acquire(long timeout, TimeUnit timeUnit) throws Exception{
		boolean b = theLock.acquire(timeout, timeUnit); //TODO: configure
		if (b) {
			lockTime = System.currentTimeMillis();
		}
		return b;
	}

	public void release() throws Exception {
		if (theLock != null) {
			theLock.release();
		}
	}

}
