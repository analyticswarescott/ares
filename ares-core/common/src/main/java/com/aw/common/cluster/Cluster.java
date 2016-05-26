package com.aw.common.cluster;

/**
 * <p>The logical representation of a cluster</p>
 *
 * Example usage, to join a cluster:
 *
 * <pre>
 * <code>
 *     Cluster cluster = (...)
 *     Member member1 = new MyMember(...);
 *     cluster.join(member1);
 *     Member member2 = new MyMember(...);
 *     cluster.join(member2);
 * </code>
 * </pre>
 *
 *
 *
 */
public interface Cluster {

	/**
	 * @return A unique cluster name for this cluster
	 */
	public String getName();

	/**
	 * Joins the cluster
	 *
	 * @param member The member joining the cluster
	 */
	public void join(LocalMember member) throws ClusterException;

	/**
	 * Leaves the cluster
	 *
	 * @param member
	 * @throws ClusterException
	 */
	public void leave(LocalMember member) throws ClusterException;




	/**
	 * get the cluster leader member
	 * @return
	 * @throws ClusterException
	 */
	public Member getLeader() throws ClusterException;

	/**
	 * set the cluster leader member
	 * @param member
	 * @throws ClusterException
	 */
	public void setLeader(Member member) throws ClusterException;

	/**
	 * clean up the cluster
	 *
	 * @throws Exception
	 */
	public void cleanup() throws Exception;

}
