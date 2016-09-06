package com.aw.platform.restcluster;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.cluster.zk.ZkCluster;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.roles.ConfigDbMaster;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.cluster.AbstractLocalMember;
import com.aw.common.cluster.Cluster;
import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.LocalMember;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.EnvironmentSettings.Setting;
import com.aw.common.system.TmpVersion;
import com.aw.common.tenant.Tenant;
import com.aw.common.tenant.TenantProvisioningException;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.platform.DefaultPlatform;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeListener;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformListener;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUpdater;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.PlatformUtils;
import com.aw.platform.monitoring.DefaultPlatformStatus;
import com.aw.platform.monitoring.PlatformStatus;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.tenant.TenantMgr;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A member of a rest cluster.
 *
 *
 *
 */
public class LocalRestMember extends AbstractLocalMember implements SparkHandler, Serializable, LocalMember, RestMember, PlatformListener {

	private static final long serialVersionUID = 1L;

	public static final Logger logger = Logger.getLogger(RestMember.class);

	private RestCluster restCluster;
	private TenantMgr tenantMgr;
	private Provider<DocumentHandler> docs;
	private Provider<DocumentMgr> docMgr;

	@Inject @com.google.inject.Inject
	public LocalRestMember(RestCluster restCluster, TenantMgr tenantMgr, PlatformMgr platformMgr, Provider<DocumentHandler> docs, Provider<DocumentMgr> docMgr) {
		super(restCluster, platformMgr);
		this.tenantMgr = tenantMgr;
		this.restCluster = restCluster;
		this.docs = docs;
		this.docMgr = docMgr;
	}

	@Override
	public void onPlatformChange(Platform platform) throws Exception {

		//try to initialize the document manager if not initialized
		if (!docMgr.get().isInitialized() && platform.hasRole(NodeRole.CONFIG_DB_MASTER) && platform.hasRole(NodeRole.CONFIG_DB_WORKER)) {
			docMgr.get().initDocuments();
		}

		if (!isJoined()) {
			tryToJoin();
		}

	}

	/**
	 * Initialize and join the rest cluster - should only be called in a rest cluster node
	 */
	public void initialize() throws ClusterException {


		logger.error(" DEBUG ±±±±±±±±± local member init ");

		if (isJoined()) {
			logger.warn("rest cluster member asked to start but is already active");
			return;
		}

		logger.debug("initializing documents");

		//if we have a config db, start it here - won't do anything if already started
		if (platformMgr.getPlatform().hasRole(NodeRole.CONFIG_DB_MASTER)
			&& platformMgr.getPlatform().hasRole(NodeRole.CONFIG_DB_WORKER)) {
			logger.info("starting config db role");
			ensureConfigDbRunning();
		}

		//in production only active rest members have local config dbs for now
		docMgr.get().initDocuments();

		//wire up platform Mgr -> doc handler for platform changes on rest only
		docs.get().addListener(platformMgr);

		logger.debug("documents initialized");

		//listen for platform changes
		platformMgr.addListener(this);

	}

	/**
	 * Determine if we're ready to start the local cluster member
	 *
	 * @return
	 * @throws ClusterException
	 */
	@Override
	protected boolean determineReadiness() throws ClusterException {

		boolean readyToJoin = true;

		if (EnvironmentSettings.isFirstNode()) {

			logger.debug("Invoking FIRST_NODE procedures");
			readyToJoin = startFirstNode(); //we are ready if we complete first node startup procedure

		}

		return readyToJoin;

	}

	void ensureConfigDbRunning() throws ClusterException {

		try {


			logger.info("making sure database is running");
			PlatformController controller = newController();
			controller.configureRole(NodeRole.CONFIG_DB_WORKER, platformMgr.getPlatform());
			controller.configureRole(NodeRole.CONFIG_DB_MASTER, platformMgr.getPlatform());

			controller.startRole(NodeRole.CONFIG_DB_WORKER, platformMgr.getPlatform());
			controller.startRole(NodeRole.CONFIG_DB_MASTER, platformMgr.getPlatform());



		} catch (Exception e) {

			throw new ClusterException("error starting config database", e);

		}

	}

	/**
	 * If this is the first node in the platform and rest is starting up, check if we need to start
	 * from scratch and bring up zookeeper, etc.
	 *
	 * @return whether the first node startup process completed successfully and we can join the rest cluster
	 * @throws ClusterException
	 */
	protected boolean startFirstNode() throws ClusterException {

		//get the platform for startup
		Platform platform = platformMgr.getPlatform();

		if (platform == null) {
			throw new ClusterException("no platform defined");
		}

		boolean ret = false;

		logger.info("first node! executing start for zookeeper..");

		try {

			//see if we need to start the config db

			//get a platform controll from the rest cluster
			logger.debug("getting reference to REST cluster");

			//if no zookeeper running, start in bootstrap mode
			if (!isZookeeperRunning(platform)) {

				ret = startBootstrap(platform);
				logger.warn("bootstrapping completed and ready to join running zk cluster? " + ret);

			}

			//else already running, return that fact so we indicate that we are ready to join
			else {
				ret = true;
			}

		} finally {

			//only created a temporary controller to get zookeeper started
			m_controller = null;

		}

		//if we started first node, initialize cluster now
		if (ret) {
			restCluster.init();
		}

		//return whether we've started zookeeper, which means we can join the cluster
		return ret;

	}

	/**
	 * Zookeeper not running yet, we need to go into a boostrap mode to get the platform going
	 *
	 * @return whether we started up using the bootstrap method
	 * @throws ClusterException
	 */
	protected boolean startBootstrap(Platform platform) throws ClusterException {

		//try to start zookeeper if it's there - if not do nothing
		if (platform.hasRole(NodeRole.ZOOKEEPER)) {

			try {


				logger.warn("we have zk but it's not running, starting it");
				PlatformController controller = newController();
				controller.startZookeeper(platform);

				//returning true will tell caller to join

			}
			catch (Exception ex) {
				throw new ClusterException("error starting zookeeper", ex);
			}

			//we started
			return true;

		}

		//we didn't start
		return false;

	}

	/**
	 * Get a platform controller. Used by the master of the rest cluster.
	 *
	 * @return A platform controller
	 */
	public PlatformController newController() {
		return new PlatformController(restCluster, this, platformMgr);
	}

	protected boolean isZookeeperRunning(Platform platform) throws ClusterException {

		//use curator to detect if zk is running
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 2); //TODO: configurable

		CuratorFramework client = CuratorFrameworkFactory.newClient(PlatformUtils.getZkConnectString(platform), retryPolicy);


		try {

			//if we can start, zk is running
			client.start();

			int timeout = 2;

			//wait for 2 seconds, if not connected we don't have zookeeper
			boolean connected = client.blockUntilConnected(timeout, TimeUnit.SECONDS);

			//zk is running
			return connected;

		} catch (Exception e) {

			throw new ClusterException("error detecting zookeeper on first node", e);

		} finally {

			//close as necessary
			client.close();

		}


	}

	@Override
	@JsonIgnore
	public PlatformState getPlatformState() throws Exception{

		if (!isMaster()) {

			logger.debug("relaying get state request to leader: " + this.restCluster.getLeader().getHost());
			RemoteRestMember rrr = (RemoteRestMember) restCluster.getLeader();
			rrr.setPlatform(platformMgr);
			PlatformState state = rrr.getPlatformState();
			return state;
		}
		else {

			return m_controller.getPlatformState();


		}

	}

	@Override
	public boolean provision(Tenant tenant) throws Exception {

    	if (!isMaster()) {

        	logger.debug("relaying provisioning of tenant to leader: " + this.restCluster.getLeader().getHost());

			RemoteRestMember rrr = (RemoteRestMember)restCluster.getLeader();
			rrr.setPlatform(platformMgr);

			boolean ret = rrr.provision(tenant);

    		//getRestLeader().provision(tenant);
			logger.debug("relay response was : " + ret);

			if (!ret) {
				throw new TenantProvisioningException("error provisioning tenant");
			}

    	}

    	else {

        	logger.debug("provisioning new tenant " + tenant.getTenantID());

    		tenantMgr.provisionTenant(tenant);

    	}

    	return true;

	}

	public void stop() throws ClusterException {

		try {

			//stop listening to platform changes
			platformMgr.removeListener(this);
			stopLeadership(null);
			leave();

			m_master = false;

		} catch (Exception e) {

			throw new ClusterException("error stopping rest member", e);

		}

	}
	@Override
	public void executeLeadership(Cluster cluster) {

		try {
			logger.warn(" @@@@@@@@@@@ taking leadership");
			//we're master TODO: event?
			m_master = true;

			PlatformController controller = newController();
			m_controller = controller;

			PlatformState state = restCluster.getState();
			switch (state) {
				case READY:
					//do nothing as we have been manually put in this state
					break;
				case UPGRADING:

					if (!platformMgr.isUpgradeInProgress) {

						platformMgr.isUpgradeInProgress = true;
						//if we're upgrading, finish it
						m_controller.executeState(state);
					}
					break;

				default:

					//This is a variable that can be manually set on first node for current DEV multi-node needs
						if (EnvironmentSettings.fetch(Setting.MANUAL_STARTUP).equals("false")) {
							m_controller.executeState(PlatformState.RUNNING);
						}
						else {
							logger.warn(" MAUNAL_STARTUP = true detected -- start platform manually ");
						}


					break;

			}

			//set state to running
			RestCluster restCluster = (RestCluster)cluster;
			restCluster.setState(PlatformState.RUNNING);

			//while we're master, continue blocking
			synchronized (this) {
				while (m_master) {

					synchronized (this) {
						wait();
					}

					//try to execute on requested state
					if (m_requestedState != null) {
						m_controller.executeState(m_requestedState);
						m_requestedState = null;
					}

				}


			}


		} catch (InterruptedException e) {

			//we've been interrupted, we'll return now

		} catch (Exception e) {

			logger.error(" Exception " + e.getClass().getTypeName() +  " in leadership " , e);

			//handle platform level error like this
			SecurityUtil.setThreadSystemAccess();
			platformMgr.handleException(e, NodeRole.REST);

		} finally {

			//we're returning from this method, we are no longer master TODO: event?
			m_master = false;

		}

	}

	@Override
	public DriverRegistrationResponse registerDriver(JSONObject driverInfo) throws Exception {

		//if we are the leader
		if (isMaster()) {

			//register the driver
			return getController().registerDriver(driverInfo);

		} else {

			//else send to leader
			return getLeaderClient().registerDriver(driverInfo);

		}

	}

	@Override
	public void registerProcessor(String driverName, String processorName) throws Exception {

		//if we are the leader
		if (isMaster()) {

			//register the processor
			System.out.println("@@@@@@@@@@±±±±± Admin Resource registering processor " + processorName);
			getController().registerProcessor(driverName, processorName);

		} else {

			//else send to leader
			getLeaderClient().registerProcessor(driverName, processorName);

		}

	}


	/**
	 * Process a request to change the platform state. This will only be processed on the master rest node.
	 *
	 * @param state The platform's new state
	 * @throws ClusterException If anything goes wrong
	 */
	public void requestPlatformState(PlatformState state) throws ClusterException {

		if (!isMaster() || m_controller == null) {
			throw new ClusterException("cannot set state, not fully initialized as master");
		}

		else {

			//request state and notify
			synchronized (this) {
				m_requestedState = state;
				notifyAll();
			}

		}

	}

    public void requestUpgrade(PlatformUpgrade upgrade) throws ClusterException {

		logger.warn(" requesting upgrade " + upgrade.toJSON().toString());
    	m_upgrade = upgrade;
		restCluster.getSystemZkAccessor().put(ZkPurpose.UPGRADE,PlatformUpdater.UPGRADE_VERSION, upgrade.toJSON().toString());
    	requestPlatformState(PlatformState.UPGRADING);

    }

	@Override
	public synchronized void stopLeadership(Cluster cluster) {

		//we are no longer master
		m_master = false;

		//let executeLeadership know
		notifyAll();

	}

	@Override
	public void upgrade(PlatformUpgrade upgrade) throws ClusterException {

		if (!isMaster()) {

			//TODO: improve factoring of getLeader up to ZkCluster
			logger.warn("relaying upgrade request to master: " + ((RestMember) restCluster.getLeader()).getHost());

			RemoteRestMember rrr = (RemoteRestMember)restCluster.getLeader();
			rrr.setPlatform(platformMgr);

			restCluster.getSystemZkAccessor().put(ZkPurpose.UPGRADE,ZkCluster.Key.UPGRADE_VERSION, upgrade);
			rrr.upgrade(upgrade);

		}

		//else do the upgrade
		else {

			requestUpgrade(upgrade);

		}

	}

    public PlatformStatus getStatus(DocumentHandler docs, Instant stattime) throws Exception {

    	PlatformStatus ret = null;

    	//if not leader send to him
    	if (!isMaster()) {

    		logger.info("relaying status request to master: " + getLeaderClient().getHost());
			ret = getLeaderClient().getPlatformStatus(stattime);

    	}

    	//else return status
    	else {

        	//will only work if we're master
        	DefaultPlatformStatus status = new DefaultPlatformStatus(platformMgr, docs, restCluster);
    		status.collect(restCluster.getTenantZkAccessor(), status, stattime);
    		ret = status;

    	}


		return ret;

    }

	/**
	 * Get the PlatformNode for the rest leader
	 *
	 * @return The PlatformNode for the rest leader
 	 * @throws ClusterException If any errors occur
	 */
    @JsonIgnore
	private PlatformNode getLeaderNode() throws ClusterException {

		//get leader
		RestMember leader = (RestMember)this.restCluster.getLeader();

		//detected no leader in the cluster, this is a problem
		if (leader == null) {
				throw new ClusterException("no rest cluster leader found ");
		}

		//get the PlatformNode for the rest leader
		PlatformNode leaderNode = platformMgr.getPlatform().getNode(leader.getHost());

		if (leaderNode == null) {
			throw new ClusterException("could not find leader " + leader.getHost() + " in platform definition");
		}

		return leaderNode;

	}

	@JsonIgnore
	public PlatformClient getLeaderClient() throws ClusterException {
		if (m_leaderClient == null) {

			PlatformNode leader = getLeaderNode();

			if (leader == null) {
				throw new ClusterException (" leader is null after getLeaderNode");
			}
			logger.warn(" common JAR version is : " + TmpVersion.VERSION + " leader was identified as  " + leader.getHost());

			m_leaderClient = new PlatformClient(leader, platformMgr);
		}
		return m_leaderClient;
	}
	private PlatformClient m_leaderClient = null;

	@Override
	public void cleanup() throws Exception {

	}

	public void nodeReporting(String host) throws Exception {

		logger.debug(" node reporting in: " + host);
		//if the node doesn't exist, add it to the platform
		PlatformNode node = platformMgr.getPlatform().getNode(host);

		if (node == null) {

			//this will result in a platform change event
			PlatformNode newNode = newNode(host);
			logger.info(" unprovisioned node " + newNode.getHost() + " added ");

		}
		else {
			fireNodeReporting(host);
		}


	}

	/**
	 * Add an unprovisioned node to the platform
	 *
	 * @param host The hostname of the unprovisioned node
	 */
	public synchronized PlatformNode newNode(String host) throws Exception {

		logger.info("updating platform, adding new unprovisioned node: " + host);

		//get the current platform
		Document platformDoc = docs.get().getDocument(DocumentType.PLATFORM, Platform.LOCAL);
		DefaultPlatform platform = platformDoc.getBodyAsObject();

		//create the node
		PlatformNode node = new DefaultPlatformNode();

		//add the node
		platform.getNodes().put(host, node);
		platformDoc.setBody(new JSONObject(JSONUtils.objectToString(platform)));

		//update the document
		docs.get().updateDocument(platformDoc);

		//return the node
		return node;

	}


	private void fireNodeReporting(String host) {

		//notify listeners
		for (NodeListener listener : nodeListeners) {
			listener.nodeReporting(host);
		}

	}

	public void addNodeListener(NodeListener listener) {
		nodeListeners.add(listener);
	}

	public void removeNodeListener(NodeListener listener) {
		nodeListeners.remove(listener);
	}

	/**
	 * If we are the leader, we will have a controller used to monitor and maintain the platform state.
	 */
	public PlatformController getController() { return m_controller; }

	@JsonIgnore
	private transient PlatformController m_controller;


	public boolean isMaster() { return m_master; }
	void setMaster(boolean master) { m_master = master; }
	private boolean m_master = false;

	public boolean isActive() { return isJoined(); }

	private transient PlatformUpgrade m_upgrade;

	public PlatformState getRequestedState() { return m_requestedState; }
	private PlatformState m_requestedState;

	/**
	 * @return The host this process is running as in the platform
	 */
	public String getHost() {
		if (m_host == null) {
			m_host = EnvironmentSettings.getHost();
		}
		return m_host;
	}
	void setHost(String host) { m_host = host; }
	private String m_host;

	DocumentHandler getDocs() { return docs.get(); }

	private List<NodeListener> nodeListeners = new CopyOnWriteArrayList<>();

}
