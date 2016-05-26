package com.aw.common.cluster;

/**
 * A local member of a cluster (i.e. on this local host)
 */
public interface LocalMember extends Member {

	/**
	 * Called when this node is being forced to stop leadership functions. This could be the result of the framework detecting a loss of connection, for example.
	 * This method will be called concurrently with executeLeadership(), and should result in executeLeadership() to return gracefully.
	 *
	 * Note that this method does not throw any checked exceptions because all exception handling must be done specific to the member's logic and within this call.
	 */
	public void stopLeadership(Cluster cluster);

	/**
	 * Perform the role of the cluster leader. This method should not return unless an error occurs or this process is relinquishing lead.
	 *
	 * Note that this method does not throw any checked exceptions because all exception handling must be done specific to the member's logic and within this call.
	 */
	public void executeLeadership(Cluster cluster);

}
