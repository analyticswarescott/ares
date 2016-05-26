package com.aw.common.cluster;

/**
 * A member of a cluster that uses a single leader / multiple follower pattern. Any node can be elected leader, and
 * there is currently no minimum cluster size so a single connected member will be made the leader.
 *
 *
 *
 */
public interface Member {

	/**
	 * @return The platform host for this rest member. Will match a host in the platform definition.
	 */
	public String getHost();

	/**
	 * Cleanup any resources used by this member on shutdown
	 */
	public void cleanup() throws Exception;

}
