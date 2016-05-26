package com.aw.common.cluster.zk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aw.common.zookeeper.ZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import org.junit.Test;

import com.aw.common.cluster.Cluster;
import com.aw.common.cluster.LocalMember;
import com.aw.common.cluster.Member;

public class ZkClusterTest extends AbstractClusterTest {

	@Test
	public void basicCluster() throws Exception {

		ZkCluster cluster = new ZkCluster(this.platformProvider, "cluster_test") {
			@Override
			protected Class<? extends Member> getMemberType() {
				return TestMember.class;
			}
		};

		try {

			cluster.init();

			AtomicBoolean someoneLeader = new AtomicBoolean();
			AtomicBoolean someoneTookover = new AtomicBoolean();

			TestMember member1 = new TestMember(1, someoneLeader, someoneTookover);
			TestMember member2 = new TestMember(2, someoneLeader, someoneTookover);

			synchronized (this) {

				cluster.join(member1);
				cluster.join(member2);

				cluster.getZkMember(member1).setCooldown(1L);
				cluster.getZkMember(member2).setCooldown(1L);
				//wait for leader up to 1 second
				if (!someoneLeader.get()) {
					wait(5000L);
				}

				//make sure we became leader
				assertTrue("no one was made leader", someoneLeader.get());

				//stop the current leader
				stopLeader(cluster, member1, member2);

				//make sure someone else took over
				if (!someoneTookover.get()) {
					wait(1000L);
				}

				//make sure the other member took over
				assertTrue("someone should have taken over when leadership was given up", someoneTookover.get());

				//stop the current leader
				stopLeader(cluster, member1, member2);

				member1.cleanup();
				member2.cleanup();

			}

			//don't recreate a cluster for the following tests, this should save some time

			//check objects in key/value store

			ZkAccessor zk = cluster.getSystemZkAccessor();
			zk.put(ZkPurpose.CLUSTER, "test/path/to/value1", new TestData("test data 1"));
			zk.put(ZkPurpose.CLUSTER, "test/path/to/value2", new TestData("test data 2"));

			List<TestData> data = zk.getAll(ZkPurpose.CLUSTER, "test", TestData.class);

			assertEquals(2, data.size());

			//sort by data
			Collections.sort(data, (d1, d2) -> { return d1.data.compareTo(d2.data); });
			assertEquals("test data 1", data.get(0).getData());
			assertEquals("test data 2", data.get(1).getData());


		} finally {

			cluster.cleanup();

		}

	}

	private void stopLeader(Cluster cluster, TestMember... members) {

		for (TestMember member : members) {
			//stop the current leader
			if (member.isLeader()) {
				member.stopLeadership(cluster);
			}
		}

	}

	private class TestMember implements LocalMember {

		public TestMember(int id, AtomicBoolean someoneLeader, AtomicBoolean someoneTookover) {
			m_id = id;
			m_someoneLeader = someoneLeader;
			m_someoneTookover = someoneTookover;
		}

		@Override
		public void executeLeadership(Cluster cluster) {

			//determine what flag is set
			synchronized (ZkClusterTest.this) {

				m_leader = true;

				//if no leader yet, take lead
				if (!m_someoneLeader.get()) {
					m_someoneLeader.set(true);
				}

				//else we're taking over from someone else
				else {
					m_someoneTookover.set(true);
				}

				//notify the waiter
				ZkClusterTest.this.notifyAll();

			}

			synchronized (TestMember.this) {

				//wait until we aren't lead
				while (m_leader) {
					try {
						TestMember.this.wait();
					} catch (InterruptedException e) {
					}
				}

			}

		}

		@Override
		public String getHost() {
			return "testHost" + m_id;
		}

		@Override
		public void cleanup() throws Exception {
		}

		@Override
		public synchronized void stopLeadership(Cluster cluster) {
			m_leader = false;
			notifyAll(); //let the leader thread know
		}

		public boolean isLeader() { return m_leader; }
		private boolean m_leader = false;

		int m_id;
		AtomicBoolean m_someoneLeader;
		AtomicBoolean m_someoneTookover;

	}

	private static class TestData {

		public TestData() {
		}

		public TestData(String data) {
			this.data = data;
		}

		public String getData() { return this.data;  }
		public void setData(String data) { this.data = data; }
		private String data;

	}

}
