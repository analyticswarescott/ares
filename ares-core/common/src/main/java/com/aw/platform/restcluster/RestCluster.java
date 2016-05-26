package com.aw.platform.restcluster;

import javax.inject.Provider;

import com.aw.common.zookeeper.structure.ZkPurpose;
import org.apache.log4j.Logger;

import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.ClusterInitializationException;
import com.aw.common.cluster.ClusterProperty;
import com.aw.common.cluster.Member;
import com.aw.common.cluster.db.CuratorLockWrapper;
import com.aw.common.cluster.db.DefaultLockManager;
import com.aw.common.cluster.db.DefaultOpSequencer;
import com.aw.common.cluster.db.LockManager;
import com.aw.common.cluster.db.OpSequencer;
import com.aw.common.cluster.zk.DefaultClusterProperty;
import com.aw.common.cluster.zk.ZkCluster;
import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.Platform;
import com.aw.platform.PlatformUtils;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.google.common.base.Preconditions;

/**
 * Adds concept of PlatformState to the standard cluster
 *
 *
 *
 */
public class RestCluster extends ZkCluster implements AutoCloseable {

	private static final Logger logger = Logger.getLogger(RestCluster.class);

    //wait up to a minute for the platform via waitFor()
	private static final long MAX_PLATFORM_WAIT = 60000L;

	private enum Property {
		STATE,
		STATUS_MESSAGE,
	}

	public RestCluster(Provider<Platform> platform, String clusterName) throws ClusterInitializationException {
		super(platform, clusterName);
	}

	@Override
	protected Class<? extends Member> getMemberType() {
		return RestMember.class;
	}

	@Override
	public void close() throws Exception {
		cleanup();
	}

	@Override
	public void init() throws ClusterException {

		if (isInitialized()) {
			return;
		}

		super.init();

		try {

			//create properties for our cluster
			state = new DefaultClusterProperty<PlatformState>(PlatformState.class, this, Property.STATE, PlatformState.UNKNOWN);
			statusMessage = new DefaultClusterProperty<String>(String.class, this, Property.STATUS_MESSAGE, null);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ClusterException("error getting state cache for rest cluster", e);
		}

	}

    /**
     * Waits for the platform state RUNNING to be reached, up to the platform maximum wait time. Only blocks on a
     * REST instance, as any executing loa property for cluster stategic on another platform node would indicate the platform is running.
     *
     * @param state The state to wait for
     */
    public void waitForRunning() throws ClusterException {

    	if (!PlatformUtils.isRest(getPlatform())) {
    		return;
    	}

    	waitFor(PlatformState.RUNNING, MAX_PLATFORM_WAIT);

    }

    /**
     * Wait a specific number of milliseconds for the platform state to be reached before timing out - when a timeout occurs, a
     * PlatformTimeoutException will be thrown.
     *
     * @param desiredState The state being waited for
     * @throws Exception If anything goes wrong or a timeout has occurred
     */
    public void waitFor(PlatformState desiredState, long timeout) throws ClusterException {

    	Preconditions.checkNotNull(state, "state is null, rest cluster has not been initialized for node " + EnvironmentSettings.getHost());

    	state.waitFor(desiredState, timeout);

    }

	public void setLeader(RestMember member) throws ClusterException {
		getSystemZkAccessor().put(ZkPurpose.CLUSTER, RestCluster.Key.LEADER, member);
	}

	@Override
	public void cleanup() throws Exception {

		if (state != null) {

			//stop monitoring state
			state.cleanup();

		}

		super.cleanup();

	}

	/**
	 * Update the state of the cluster - the state will be toString'd and serialized as a byte array
	 *
	 * @param state
	 */
	public void setState(PlatformState state) throws ClusterException {

		synchronized (this) {
			Preconditions.checkNotNull(state, "state property is null, cluster not properly initialized - this: " + this);
		}

		this.state.set(state);

	}

	/**
	 * The state will be updated as it changes in the cluster. This object can be waited on for state change notifications.
	 *
	 * @return The current state of the cluster
	 */
	public PlatformState getState() throws ClusterException {

		synchronized (this) {
			Preconditions.checkNotNull(state, "state property is null, cluster not properly initialized - this: " + this);
		}

		return state.get();

	}

	/**
	 * Update the state of the cluster - the state will be toString'd and serialized as a byte array
	 *
	 * @param state
	 */
	public void setStatusMessage(String msg) throws ClusterException {

		synchronized (this) {
			Preconditions.checkNotNull(statusMessage, "statusMessage property is null, cluster not properly initialized - this: " + this);
		}

		this.statusMessage.set(msg);

	}

	/**
	 * The state will be updated as it changes in the cluster. This object can be waited on for state change notifications.
	 *
	 * @return The current state of the cluster
	 */
	public String getStatusMessage() throws ClusterException {

		synchronized (this) {
			Preconditions.checkNotNull(statusMessage, "statusMessage property is null, cluster not properly initialized - this: " + this);
		}

		return statusMessage.get();

	}

	//the state of the cluster as a whole
	public ClusterProperty<PlatformState> getStateProperty() { return state; }
	private DefaultClusterProperty<PlatformState> state;

	public ClusterProperty<String> getStatusMessageProperty() { return this.statusMessage;  }
	private DefaultClusterProperty<String> statusMessage;

	//TODO: relocate

	public CuratorLockWrapper acquireLock(String key) throws Exception {
		CuratorLockWrapper wrapper = getLockManager().acquireLock(getClient(), key);
		return wrapper;
	}

	public int incrementOpSequence(String key) throws Exception {
		return getOpSequencer().incrementSequence(getClient(), key);
	}

	public int getCurrentOpSequence(String key) throws Exception {
		return getOpSequencer().getSequenceValue(getClient(), key);
	}

	/**
	 * Get the live platform state.
	 *
	 * @return The platform state
	 * @throws ClusterException If anything goes wrong
	 */
	public PlatformState getPlatformState() throws ClusterException {
		return getState();
	}

	public String getPlatformStatus() throws ClusterException {
		return getStatusMessage();
	}


	private OpSequencer m_sequencer;
	public OpSequencer getOpSequencer() {
		if (m_sequencer == null) {
			m_sequencer = new DefaultOpSequencer(getPlatform(), getTenantZkAccessor());
		}

		return m_sequencer;
	}

	private LockManager m_lockManager;
	public LockManager getLockManager() {

		if (m_lockManager == null) {
			m_lockManager = new DefaultLockManager(getTenantZkAccessor());
		}
		return m_lockManager;
	}

}




