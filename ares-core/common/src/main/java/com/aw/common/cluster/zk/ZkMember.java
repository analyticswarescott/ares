package com.aw.common.cluster.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.log4j.Logger;

import com.aw.common.cluster.Cluster;
import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.LocalMember;
import com.aw.common.cluster.Member;
import com.google.common.base.Preconditions;

/**
 * Wrapper for zk cluster members
 *
 *
 *
 */
class ZkMember extends LeaderSelectorListenerAdapter implements LocalMember, UnhandledErrorListener {

	static final Logger logger = Logger.getLogger(Member.class);

	/**
	 * create a zk cluster member
	 *
	 * @param wrapped
	 * @param cluster
	 * @throws ClusterException
	 */
	public ZkMember(LocalMember wrapped, ZkCluster cluster) throws ClusterException {

		Preconditions.checkArgument(cluster != null, "cluster cannot be null");

		this.wrapped = wrapped;
		this.cluster = cluster;

	}

/*	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {

		//if we are disconnected, we should stop being leader
		switch (newState) {

			case LOST:
				logger.warn(" stopping leadership due to LOST connection state ");
				stopLeadership(cluster);
				break;

			default:
				//just log for now
				logger.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ >>>>>>>>>>>>>>>>>>>> state changed to " + newState + ", not taking action");
				break;

		}

	}*/

	@Override
	public void takeLeadership(CuratorFramework client) throws Exception {

		cluster.setLeader(wrapped);
		logger.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ >>>>>>>>>>>>>>>>>>>> taking leadership ");
		//perform the role of the leader
		executeLeadership(cluster);

	}

	/**
	 * Initialize the new member of the ZkCluster
	 */
	public void init() throws ClusterException {

		//listen for leadership changes
		try {

			leaderSelector = cluster.newLeaderSelector(this);

			//host is the id
			leaderSelector.setId(getHost());

			//start it up
			leaderSelector.start();

		} catch (RuntimeException e) {

			//make sure if there are errors that we close the leader selector
			leaderSelector.close();
			throw e;

		} catch (ClusterException e) {

			//make sure if there are errors that we close the leader selector
			leaderSelector.close();
			throw e;

		}



	}

	@Override
	public void executeLeadership(Cluster cluster) {

		try {

			wrapped.executeLeadership(cluster);

		} finally {

			try {

				//cooldown to avoid leadership spam
				Thread.sleep(getCooldown());

			} catch (InterruptedException ie) {
				logger.warn("interrupted while cooling down after leadership exception on " + getClass().getName(), ie);
			}

		}

	}

	@Override
	public String getHost() {
		return wrapped.getHost();
	}

	@Override
	public void stopLeadership(Cluster cluster) {
		wrapped.stopLeadership(cluster);
	}

	@Override
	public void cleanup() throws Exception {

		boolean hasLeader = leaderSelector.hasLeadership();
		leaderSelector.close();

		if (wrapped != null) {

			if (hasLeader) {

				wrapped.stopLeadership(cluster);

			}

			//cleanup wrapped
			wrapped.cleanup();
			wrapped = null;

		}

	}


	//the cluster
	private ZkCluster cluster;

	//the actual wrapped member
	private LocalMember wrapped;

	//the leader selector
	LeaderSelector getLeaderSelector() { return leaderSelector; }
	private LeaderSelector leaderSelector;

	/**
	 * When a leadership ends, this cooldown will be applied before finding a new leader. A cooldown avoids spinning
	 * in a state where the same problem is occurring endlessly, resulting in leadership election spam.
	 *
	 * @return the exception cooldown
	 */
	public long getCooldown() { return this.cooldown;  }
	public void setCooldown(long exceptionCooldown) { this.cooldown = exceptionCooldown; }
	private long cooldown = 1000L; //1 second cooldown by default

	@Override
	public void unhandledError(String s, Throwable throwable) {
		logger.error(" unhandled connection state error " + throwable.getMessage());
	}
}
