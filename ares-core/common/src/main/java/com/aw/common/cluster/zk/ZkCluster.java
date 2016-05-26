package com.aw.common.cluster.zk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.cluster.Cluster;
import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.ClusterInitializationException;
import com.aw.common.cluster.LocalMember;
import com.aw.common.cluster.Member;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.google.common.base.Preconditions;

/**
 * Zookeeper based cluster.
 *
 * <li>Keys for cluster data will be toString'd.
 * <li>Values for cluster data will be serialized using the platform's json serialization technique.
 *
 *
 *
 */
public abstract class ZkCluster implements Cluster {

	public static Logger logger = LoggerFactory.getLogger(ZkCluster.class);

	/**
	 * Well known cluster keys used to store data
	 */
	public enum Key {

		/**
		 * The master's last known state of the platform
		 */
		PLATFORM_STATE,

		/**
		 * The leader of the rest cluster
		 */
		LEADER,
		UPGRADE_VERSION,
		UPGRADE_NODE_IN_PROGRESS
	}


	//data node within a cluster for data storage
	private static String DATA = "data";

	/**
	 * Create a cluster monitor with the given member
	 *
	 * @param clusterName
	 * @param platform
	 * @param member
	 */
	public ZkCluster(Provider<Platform> platform, String clusterName) throws ClusterInitializationException {

		Preconditions.checkArgument(clusterName != null, "cluster name cannot be null");
		Preconditions.checkArgument(!DATA.equals(clusterName), "cluster name cannot be " + DATA + ", that is a reserved word.");

		m_platform = platform;

		setClusterName(clusterName);

	}

	public void init() throws ClusterException {

		if (initialized) {
			return;
		}

		//TODO: don't initialize cluster until platform is ready

		try {

			//start the zk client -- TODO: why is it stopping if accessed from a zkAccessor?
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 5); //TODO: configurable
			m_client = CuratorFrameworkFactory.newClient(PlatformUtils.getZkConnectString(m_platform.get()), retryPolicy);
			m_client.start();

			systemZkAccessor = new DefaultZkAccessor(m_platform.get(), m_client, Hive.SYSTEM);
			tenantZkAccessor = new DefaultZkAccessor(m_platform.get(),m_client, Hive.TENANT);

		}
		catch (Exception e) {

			//clean up client if anything goes wrong
			if (m_client != null) {
				m_client.close();
				m_client = null;
			}

			throw new ClusterException("error initializing ZK access", e);

		}

		//see if we have zookeepers
		initialized = true;

	}


	public void join(LocalMember member) throws ClusterException {

		//make sure the platform is ready
		checkPlatform();

		synchronized (this) {

			//lazy initialization on first request - if not initialized, initial the cluster now
			if (!initialized) {
				init();
			}

		}

		//create and initialize the member
		ZkMember zkMember = new ZkMember(member, this);
		zkMember.init();

		synchronized (this) {

			//keep track of locally added members
			m_localMembers.put(member, zkMember);

		}

	}

	public void leave(LocalMember member) throws ClusterException {

		//avoid threading issues
		ZkMember zkMember = null;
		synchronized (this) {
			zkMember = m_localMembers.remove(member);
		}

		//leave the cluster
		try {

			Preconditions.checkNotNull(zkMember, "leave called for member that was not registered as joined");
			zkMember.cleanup();

		} catch (Exception e) {

			throw new ClusterException("error leaving cluster", e);

		}

	}

	/**
	 * makes sure the platform can run a cluster
	 * @throws ClusterException If it can't
	 */
	private void checkPlatform() {

		//make sure we have zookeeper
		List<PlatformNode> nodes = m_platform.get().getNodes(NodeRole.ZOOKEEPER);
		Preconditions.checkState(nodes != null && nodes.size() > 0, "cluster " + getName() + " error : platform not ready, no zookeeper nodes defined");

	}


	protected String getClusterPath() {
		return getSystemZkAccessor().getAbsoluteZkPath(ZkPurpose.CLUSTER, getName());
	}

	/**
	 * Create a new client for the cluster
	 *
	 * @return The new curator client
	 */
	LeaderSelector newLeaderSelector(LeaderSelectorListener listener) throws ClusterException {

		try {

			LeaderSelector selector = new LeaderSelector(getClient(), getClusterPath(), listener);
			selector.autoRequeue();
			return selector;

		} catch (Exception e) {
			throw new ClusterException("error creating new leader selector for cluster member", e);
		}

	}


	public Member getLeader() throws ClusterException {

		try {
			logger.warn("Getting leader: leader type is " + getMemberType().getTypeName());
			Member o = systemZkAccessor.get(ZkPurpose.CLUSTER, Key.LEADER, getMemberType());

			if (o == null) {
				logger.warn("leader is null!");
			} else {
				logger.warn("got leader of type : " + o.getClass().getTypeName());
			}

			return o;
		}
		catch (Exception ex) {
			throw new ClusterException("exception getting leader ", ex);
		}
	}

	/**
	 * @return The member type for this cluster
	 */
	protected abstract Class<? extends Member> getMemberType();

	public void setLeader(Member member) throws ClusterException {
		systemZkAccessor.put(ZkPurpose.CLUSTER, ZkCluster.Key.LEADER, member);
	}


	/**
	 *  Cleanup the cluster object - this does not shut down the cluster itself, just the local awareness of the cluster
	 */
	public void cleanup() throws Exception {

		//get the members, allowing us to iterate without threading concerns
		ZkMember[] members = null;
		synchronized (this) {
			members = m_localMembers.values().toArray(new ZkMember[m_localMembers.size()]);
		}

		for (ZkMember member : members) {
			member.cleanup();
		}

		systemZkAccessor.cleanup();
		tenantZkAccessor.cleanup();

		initialized = false;

	}

	protected Platform getPlatform() {
		return m_platform.get();
	}

	/**
	 * @return the nodes participating in the cluster, including the local node
	 */
	public List<PlatformNode> getParticipants() throws Exception {

		Preconditions.checkArgument(m_localMembers.size() > 0, "no local members registered, cannot return participants in cluster " + getClusterPath());

		List<PlatformNode> ret = new ArrayList<PlatformNode>();

		//get any member to get the participants
		ZkMember member = m_localMembers.values().iterator().next();

		Collection<Participant> participants = member.getLeaderSelector().getParticipants();

		for (Participant participant : participants) {

			PlatformNode node = m_platform.get().getNode(participant.getId());

			if (node != null) {
				ret.add(node);
			}

		}

		return ret;

	}

	/**
	 * @return The cluster name
	 */
	public String getName() { return m_clusterName; }
	public void setClusterName(String name) { m_clusterName = name; }
	private String m_clusterName;

	//the platform in which this cluster is running
	private Provider<Platform> m_platform;

	//whether this cluster is initialized
	protected boolean isInitialized() { return initialized; }
	private boolean initialized;

	//all members who have joined locally
	public ZkMember getZkMember(Member member) { return m_localMembers.get(member); }
	private Map<Member, ZkMember> m_localMembers = new HashMap<Member, ZkMember>();

	public DefaultZkAccessor getTenantZkAccessor() {
		return tenantZkAccessor;
	}
	public DefaultZkAccessor getSystemZkAccessor() {
		return systemZkAccessor;
	}


	private DefaultZkAccessor systemZkAccessor;
	private DefaultZkAccessor  tenantZkAccessor;



	//the curator client instance
	public CuratorFramework getClient() {

		if (m_client == null) {
			return null;
		}

		if (m_client.getState() == CuratorFrameworkState.STOPPED) {
			System.out.println(" =================@@> DETECTED STOPPED client on CLUSTER instance");
			//m_client.start();
		}
		return m_client; }
	private CuratorFramework m_client = null;


}
