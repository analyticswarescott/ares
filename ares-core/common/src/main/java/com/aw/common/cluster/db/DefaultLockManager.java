package com.aw.common.cluster.db;

import com.aw.common.cluster.db.exceptions.LockAcquireException;
import com.aw.common.cluster.db.exceptions.LockNotFoundException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.TimeUnit;

/**
 * Manage acquire and release of locks (e.g. a DB write lock on a semantic document
 */
public class DefaultLockManager implements TenantAware, LockManager {


	long m_timeout = 10;
	TimeUnit m_timeUnit = TimeUnit.SECONDS;

	//TODO: determine if we need to track locks
	//private Map<String, CuratorLockWrapper> locks = new HashMap<>();

	private ZkAccessor tenantZkAccessor;

	public DefaultLockManager(ZkAccessor tenantZKAccessor) {
		this.tenantZkAccessor = tenantZKAccessor;
	}


	@Override
	public String getLockPath(String key) {
		//TODO: make ZK base path public or always perform these ops from inside the ZKCluster class?  Maybe add to ClusterAware interface?
		//return "/aw/cluster/DATA/op_locks/" + getTenantID() + "-" + key;
		return tenantZkAccessor.getAbsoluteZkPath(ZkPurpose.LOCKING, key);

	}


	@Override
	public CuratorLockWrapper acquireLock(CuratorFramework client, String key) throws Exception {
		return acquireLock(client, key, m_timeout, m_timeUnit);
	}

	public CuratorLockWrapper acquireLock(CuratorFramework client, String key, long timeout, TimeUnit timeUnit) throws Exception {
		String lockPath = getLockPath(key);

		CuratorLockWrapper wrapper = new CuratorLockWrapper(client, lockPath);
		if (wrapper.acquire(timeout, timeUnit)) {
			return wrapper;
		}
		else {
			throw new LockAcquireException(" could not acquire lock on path " + lockPath);
		}
	}



	//TODO: if we always release locks properly, this should not be needed
	@Override
	public void releaseLock(String key) throws LockNotFoundException {
	/*	String lockPath = getLockPath(key);

		if (locks.containsKey(lockPath)) {
			CuratorLockWrapper wrapper = locks.get(lockPath);
		}
		else {
			throw new LockNotFoundException(" no existing lock found for path " + key);
		}*/
	}

	@Override
	public void setAcquireTimeout(long timeout, TimeUnit timeUnit) throws Exception {
		m_timeout = timeout;
		m_timeUnit = timeUnit;
	}


}
