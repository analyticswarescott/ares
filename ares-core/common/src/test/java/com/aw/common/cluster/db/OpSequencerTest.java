/*
package com.aw.common.cluster.db;

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

import static org.junit.Assert.*;


public class OpSequencerTest extends AbstractClusterTest {

	@Test
	public void testSequencing() throws Exception {

		SecurityUtil.setThreadSystemAccess(); //tenant 0
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 5); //TODO: configurable
		CuratorFramework client = CuratorFrameworkFactory.newClient(PlatformUtils.getZkConnectString(platform), retryPolicy);
		client.start();

		OpSequencer opseq = new DefaultOpSequencer(platform, new DefaultZkAccessor(platform, Hive.TENANT));
		Executor executor =  Executors.newFixedThreadPool(10);
		CompletionService<Boolean> services = new ExecutorCompletionService<>(executor);


		int threads = 10;
		int iterations = 100;

		for (int j=1; j<=threads; j++) {
			Hammer h = new Hammer(client, opseq, j, iterations);
			services.submit(() -> h.run());
		}

		int submittedTasks = threads;
		long start = System.currentTimeMillis();

		//wait until all are done
		int doneCount = 0;
		while (doneCount < submittedTasks){
				Future<Boolean> result = services.take();
			if (result != null) {
				Boolean b  = result.get();
				if (b) {
					doneCount++;
				}
			}

		}

//re-iterate the types and verify ZK counters all went to 1000
		for (int k=1; k<=threads; k++) {
			assertEquals("expect " + iterations * threads + " as a count ", iterations * threads, opseq.getSequenceValue(client, "type" + k));
		}


	}

	private class Hammer {

		private OpSequencer m_seq;
		private CuratorFramework m_client;
		private int m_id;
		private int m_iterations;

		public Hammer(CuratorFramework client, OpSequencer seq, int id, int iterations) {
			m_seq = seq;
			m_client = client;
			m_id = id;
			m_iterations = iterations;

		}

		public Boolean run() throws Exception {

			SecurityUtil.setThreadSystemAccess(); //tenant 0

			//sleep to stagger a tiny bit to ensure overlap
			Thread.sleep(m_id * 10);
			for (int q=1; q<=10; q++) {
				m_seq.getSequencer(m_client, "type" + q);


				for (int i = 1; i <= m_iterations ; i++) {
					int r = m_seq.incrementSequence(m_client, "type" + q);
				}
			}

			//System.out.println("......................>>" + Thread.currentThread().getName() + " done ...........");
			return true;
		}
	}

}
*/
