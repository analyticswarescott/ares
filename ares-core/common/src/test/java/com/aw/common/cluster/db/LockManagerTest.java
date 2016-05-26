package com.aw.common.cluster.db;

import com.aw.common.cluster.db.exceptions.LockAcquireException;
import com.aw.common.cluster.zk.AbstractClusterTest;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.platform.PlatformUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;


public class LockManagerTest extends AbstractClusterTest {

	@Test
	public void testLocking() throws Exception {

		SecurityUtil.setThreadSystemAccess(); //tenant 0
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 5); //TODO: configurable
		CuratorFramework client = CuratorFrameworkFactory.newClient(PlatformUtils.getZkConnectString(platform), retryPolicy);
		client.start();

		LockManager lm = new DefaultLockManager(new DefaultZkAccessor(platform, Hive.TENANT));
		lm.setAcquireTimeout(100, TimeUnit.MILLISECONDS);

		String lockPath = "foo-bar";
		CuratorLockWrapper lock1 = lm.acquireLock(client, lockPath);
		try {
			CuratorLockWrapper lock2 = lm.acquireLock(client, lockPath);
		}
		catch (LockAcquireException lex) {
			//expected
		}
		lock1.release();

		CuratorLockWrapper lock2 = lm.acquireLock(client, lockPath);
		lock2.release();



	}


}
