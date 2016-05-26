package com.aw.common.cluster;

import org.apache.log4j.Logger;

import com.aw.platform.PlatformMgr;

/**
 * Base class for cluster members
 *
 *
 *
 */
public abstract class AbstractLocalMember implements LocalMember {

	private static final Logger LOGGER = Logger.getLogger(AbstractLocalMember.class);

	private Cluster cluster;
	protected PlatformMgr platformMgr;

	public AbstractLocalMember(Cluster cluster, PlatformMgr platformMgr) {
		this.cluster = cluster;
		this.platformMgr = platformMgr;
	}

	/**
	 * try to join the cluster
	 *
	 * @throws ClusterException
	 */
	public synchronized void tryToJoin() throws ClusterException {

		//if we already are a member of the cluster, no-op
		if (joined) {
			return;
		}

		if (!platformMgr.isPlatformComplete()) {

			LOGGER.warn("can't join cluster yet, not complete");

			//we can't join yet, maybe on another platform change event
			return;

		}

		//if we get this far, we have enough nodes to start up

		//special handling for first rest node
		boolean readyToJoin = determineReadiness();

		//if we are ready to join, join here
		if (readyToJoin) {

			//join the cluster
			joinCluster();

			//we are now joined if there was no exception
			joined = true;

		}

	}

	/**
	 * join the cluster
	 *
	 * @throws ClusterException if anything goes wrong
	 */
	private void joinCluster() throws ClusterException {

		LOGGER.debug("calling restCluster.join");
		cluster.join(this);

	}

	/**
	 * attempt to leave the cluster
	 *
	 * @throws Exception
	 */
	public void leave() throws Exception {

		try {

			cluster.leave(this);

		} finally {

			joined = false;

		}

	}

	/**
	 * Perform cluster-specific logic to determine if the local member is ready to join the cluster
	 *
	 * @return whether the local member is ready to join the cluster
	 * @throws ClusterException if anything goes wrong
	 */
	protected abstract boolean determineReadiness() throws ClusterException;

	public boolean isJoined() { return joined; }
	private boolean joined = false;

}
