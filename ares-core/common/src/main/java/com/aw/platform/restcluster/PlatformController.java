package com.aw.platform.restcluster;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.aw.common.zookeeper.structure.ZkPurpose;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.cluster.ClusterException;
import com.aw.common.exceptions.InitializationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.common.spark.DriverState;
import com.aw.common.spark.DriverStatus;
import com.aw.common.spark.PreReqUtil;
import com.aw.common.spark.SparkDriverSubmit;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.TmpVersion;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUpdater;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.exceptions.PlatformInitializationException;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.nodes.NodeClient;
import com.aw.platform.roles.HdfsName;
import com.aw.platform.roles.Rest;

/**
 * Platform controller
 */
public class PlatformController implements TenantAware {

    public static final Logger logger = LoggerFactory.getLogger(PlatformController.class);

	private boolean interrupt = false;

	protected HashMap<String, DriverStatus> _globalDrivers = new HashMap<>();
	public HashMap<String, DriverStatus> getDrivers() {
		return _globalDrivers;
	}

	public void clearDriverState() {
		_globalDrivers = new HashMap<String, DriverStatus>();
		_processors = new HashMap<String, String>();
		logger.info(" ** driver state cleared ");
	}
	//store processor names that drivers report having started
	//TODO: expand stored status data and mechanism
	protected  HashMap<String, String> _processors;
	public HashMap<String, String> getProcessors() {
		return _processors;
	}

    //TODO: move to platform package
    public static enum PlatformHealth {
        GREEN,
        YELLOW,
        RED,
        ORANGE,
        UNKNOWN;
    }

    public static enum PlatformState {
    	UNKNOWN, //if the local rest member hasn't joined the platform yet
        STARTING,
        STOPPING,
        VERIFYING,
        VIABLE,
        READY,
		UPGRADING,
        RUNNING,
        STOPPED,
        PENDING; //this would be when the platform nodes are not complete
    }

    private RestCluster restCluster;
    private LocalRestMember member;
    private PlatformMgr platformMgr;

    @Inject @com.google.inject.Inject
    public PlatformController(RestCluster restCluster, LocalRestMember member, PlatformMgr platformMgr) {
    	this.restCluster = restCluster;
    	this.member = member;
    	this.platformMgr = platformMgr;
	}

    public PlatformState getPlatformState() throws Exception{
        return restCluster.getState();
    }

    /**
     * @return Is the platform start enabled? checks PLATFORM_START system property
     */
    public static boolean isPlatformStartEnabled() {
    	return EnvironmentSettings.isPlatformStartEnabled();
    }

    public void setPlatformState(PlatformState platformState) throws ClusterException{

        //m_platformState = platformState;
		restCluster.setState(platformState);

		//wait for it to propagate back
		restCluster.waitFor(platformState, 10000L);

        //let all waiters know we changed state
        synchronized (this) {
        	notifyAll();
        }

        //TODO: variable level based on state?
        logger.info(" **** Platform state: " + platformState.toString());

	}

	//private PlatformState m_platformState = PlatformState.STOPPED;


    public String getPlatformStatus() {
        return m_platformStatus;
    }
    public void setPlatformStatus(String platformStatus) throws ClusterException {
        m_platformStatus = platformStatus;
        restCluster.setStatusMessage(platformStatus);
		logger.info("Platform Status: " + platformStatus);
    }

    private String m_platformStatus = "no status";


    public PlatformHealth getPlatformHealth() {return m_platformHealth;}
    public void setPlatformHealth(PlatformHealth platformHealth) {
        m_platformHealth = platformHealth;

        //TODO: platform listener should be notified of status change

        logger.info(" **** Platform health is now: " + m_platformHealth.toString());
    }

    private static PlatformHealth m_platformHealth = PlatformHealth.UNKNOWN;

	public void shutdownPlatform() throws Exception{
		stopPlatform();
	}

	//TODO: clean this mess up
	public DriverRegistrationResponse registerDriver(JSONObject driverInfo) throws Exception{
		logger.debug(this + " driver registration received for driver " + driverInfo);

		String driverName = driverInfo.getString("driver_name");
		String driverID = driverInfo.getString("driver_id");
		if (_globalDrivers.containsKey(driverName)) {

			driverHeardFrom();
			DriverStatus ds = getDrivers().get(driverName);

			//is there a driver stop pending?
			if (driverStop) {
				logger.info(" driver stop requested: sending STOP response ");
				return DriverRegistrationResponse.STOP;
			} else {
				ds.set_state(DriverState.REGISTERED);
				//logger.info(" driver registration processed for driver " + ds.getDriverName());
				return DriverRegistrationResponse.OK;
			}
		}
		else {
			logger.warn(" unrecognized driver tried to register " + driverName + " expected: " + _globalDrivers.keySet());
			throw new RuntimeException("unrecognized driver tried to register " + driverName + " expected: " + _globalDrivers.keySet());
			//return HttpStatus.SC_BAD_REQUEST;
		}
	}

	public void executeState(PlatformState state) throws Exception {

		switch (state) {

			case RUNNING: startPlatform(); break;
			case READY:
				if (getPlatformState() != PlatformState.RUNNING) {
					throw new ClusterException("cannot execute state READY if not RUNNING: " + state);
				}
				logger.warn(" stopping processing to achieve state READY");
				stopProcessing();
				break;

			case UPGRADING:
				if (!upgradeInProgress) {
					logger.warn(" UPGDRADING STATE detected and not in progress...launching upgrade");
					upgradePlatform();
				}
				break;
			case STOPPED: stopPlatform(); break;
			default: throw new ClusterException("cannot execute state: " + state);

		}

	}

	private boolean upgradeInProgress = false;
	public void upgradePlatform() throws Exception {

		if (upgradeInProgress) {
			return;
		}

		stopProcessing();//have to stop the drivers to reduce log noise for debugging TODO: revisit


		upgradeInProgress = true;
		//set state
		if (getPlatformState() != PlatformState.UPGRADING) {
			setPlatformState(PlatformState.UPGRADING);
		}

		//set the upgrade object
		upgrade = restCluster.getSystemZkAccessor().get(ZkPurpose.UPGRADE, PlatformUpdater.UPGRADE_VERSION, PlatformUpgrade.class);

		logger.info("upgrading to " + upgrade.getVersionId());

		//update the platform
		PlatformUpdater updater = new PlatformUpdater(restCluster, platformMgr, upgrade);

		updater.patchCluster();

		upgradeInProgress = false;

		//restart the platform when we're done
		executeState(PlatformState.RUNNING);

	}

	//TODO: expand to expected>>complete model
	public boolean isProcessorRegistered(String processorName) {
		if (getProcessors().containsKey(processorName)){
			return true;
		}
		return false;

	}

	public void registerProcessor(String driverName, String processorName) {
		getProcessors().put(processorName, driverName);
	}

    public void stopPlatform() throws Exception{

        stopProcessing(); //TODO: this does nothing yet

        //TODO improve shutdown logic and types

        try {

            setPlatformState(PlatformState.STOPPING);

            stopRole(NodeRole.SPARK_MASTER);
            stopRole(NodeRole.SPARK_WORKER);
            stopRole(NodeRole.ELASTICSEARCH);
            stopRole(NodeRole.KAFKA);
            stopRole(NodeRole.HDFS_NAME);
            stopRole(NodeRole.HDFS_JOURNAL);
            stopRole(NodeRole.HDFS_DATA);

            //TODO: stop rest

            //TODO: shut down zk client (no more zk connections at this point)

            stopRole(NodeRole.ZOOKEEPER);

            setPlatformState(PlatformState.STOPPED);

        }
        catch (Exception ex) {
            throw new RuntimeException("Error during platform node shutdown/verification", ex);
        }

    }

    /**
     * Standard code path, we are starting all roles
     *
     * @throws Exception
     */
    public void configurePlatformNodes() throws Exception {

    	//configure all nodes
        logger.info(" configuring platform roles ");

        //configure each role, order shouldn't matter
        for (PlatformNode node : platformMgr.getPlatform().getNodes().values()) {
    		configureNode(node);
        }

    }

    public void configureNode(PlatformNode node) throws Exception {

		NodeClient ncli = newNodeClient(node);
		ncli.updateNode(node);

    }

    public void configureRole(NodeRole role, Platform platform) throws Exception{

		//configure each node role
		for (PlatformNode node : platform.getNodes(role)) {

			try {

				configureNode(node);

			} catch (Exception e) {
				throw new Exception("error configuring role " + role + " on " + node.getHost(), e);
			}

		}

	}

    /**
     * Only start zookeeper, needed when first node is starting up
     */
    public void startZookeeper(Platform platform) throws PlatformInitializationException {

    	try {

			logger.info(" about to start Zookeeper explicitly");

			//configure zookeeper
			configureRole(NodeRole.ZOOKEEPER, platform);

			//PlatformNode me = getPlatformMgr().getMe();
			//configureNode(me);


			//logger.debug(" local node configured");

			//start zookeeper on local node only

			//startNodeRole(me, NodeRole.ZOOKEEPER);
			startRole(NodeRole.ZOOKEEPER, platform);

    	} catch (Exception e) {
    		throw new PlatformInitializationException("error starting zookeeper for first rest node", e);
    	}

    }

    /**
     * "configue and start the platform
     */
    protected void startPlatformNodes() throws Exception {

        if (getPlatformState() != PlatformState.VIABLE) {
            throw new PlatformStateException(" platform must be in VIABLE state to start Platform node roles. State is " + getPlatformState());
        }


        logger.info(" ***** starting platform nodes ");
        //setPlatformState(PlatformState.VERIFYING);
        //TODO: check whether platform is already up or largely up as specified

        try {

        	//make sure configurations are up-to-date
            configurePlatformNodes();

            //now the start logic -- TODO: encode in config

            logger.info(" @#@#@#@ starting cluster roles ");
            startRole(NodeRole.ZOOKEEPER, platformMgr.getPlatform());

            startRole(NodeRole.HDFS_JOURNAL, platformMgr.getPlatform()); //start journal nodes to accept edits from namenodes

			startRole(NodeRole.HDFS_DATA, platformMgr.getPlatform());

			//startRole(NodeRole.HDFS_NAME);
			startHDFSName(platformMgr.getPlatform());

            //current node should report already running
			startRole(NodeRole.REST, platformMgr.getPlatform());

            startRole(NodeRole.ELASTICSEARCH, platformMgr.getPlatform());

            startRole(NodeRole.KAFKA, platformMgr.getPlatform());

            startRole(NodeRole.SPARK_MASTER, platformMgr.getPlatform());

            startRole(NodeRole.SPARK_WORKER, platformMgr.getPlatform());

			//TODO: evaluate if this is the right place -- need to force check on HDFS status once HDFS is up
            platformMgr.getTenantFileWriter().checkStatus();

        }
        catch (Exception ex) {

        	try {
            	Thread.sleep(1000L);
        	} catch (InterruptedException e) {

        	}
            throw new RuntimeException("Error during platform node startup/verification", ex);
        }

    }

	private void startHDFSName(Platform platform) throws Exception {
		//TODO: replace this with ordering in platform.getNodes to avoid the one-off
		List<PlatformNode> nodes =  platformMgr.getPlatform().getNodes(NodeRole.HDFS_NAME);

		//find the master and start it
		for (PlatformNode node : nodes) {
			if (node.getSetting(HdfsName.STANDBY) == null) {
				logger.info(" starting PRIMARY HDFS Name node ");
				startNodeRole(node, NodeRole.HDFS_NAME);
			}
		}
		//now the stand-by
		for (PlatformNode node : nodes) {
			if (node.getSetting(HdfsName.STANDBY) != null) {
				logger.info(" starting STANDBY HDFS Name Node");
				startNodeRole(node, NodeRole.HDFS_NAME);
			}
		}
	}

	private void startNodeRole (PlatformNode node, NodeRole role) throws Exception{

		NodeClient ncli = newNodeClient(node);

		logger.debug(" checking role status for " + role + " on node " + node.getHost());
		NodeRoleStatus preCheck = ncli.getRoleStatus(role);
		if (preCheck.getState() == State.RUNNING) {
			logger.info("node role " + role + " already running on " + node.getHost());
			return;
		}

		logger.debug(" sending start request for " + role);
		NodeRoleStatus rs =  ncli.changeRoleState(role, State.RUNNING);

		if (role == NodeRole.REST) {
			logger.info(" allowing extra time for REST node to start before checking status");
			Thread.sleep(15000); //TODO: move to a callback-wait with a max timeout, maybe 60, then 24 or 31 seconds is not a killer but we generally wait less
			rs = ncli.getRoleStatus(role);
		}

		//TODO: could move this inside client if retry behavior could be deemed flexible enough
		if (rs.getState() != State.RUNNING) {
			//wait a bit and try once more //TODO: would more re-tries ever help? --possibly if we are REST verifying components and their ReST start is delayed
			logger.warn(" initial role verification failed for role " + role.toString() + " on node " + node.getHost() + " will re-try once");

			Thread.sleep(3000);

			rs = ncli.getRoleStatus(role);
		}

		if (rs.getState() != State.RUNNING) {
			//TODO: this role is not available -- YELLOW?  QUORUM?
			throw new PlatformStateException(" failed to start role " + role.toString() + " on node " + node.getHost() + "and no quorum settings available");
		}

	}


    protected void startRole(NodeRole role, Platform platform) throws Exception{

        //start the role on all nodes that have it
        List<PlatformNode> nodes = platform.getNodes(role);
		if (nodes.size() == 0) {
			logger.error(" node count for role " + role + " is " + nodes.size());
		}
		else {
			logger.info(" starting " + nodes.size() + " nodes with role " + role);
		}

        for (PlatformNode node : nodes) {
            logger.info(" starting role " + role.toString() + " on node " + node.getHost());

			startNodeRole(node, role);
        }


    }

    private void stopRole(NodeRole role) throws Exception{

        //stop the role on all nodes that have it
        List<PlatformNode> nodes =  platformMgr.getPlatform().getNodes(role);
        for (PlatformNode node : nodes) {
            logger.info(" stopping role " + role.toString() + " on node " + node.getHost());
            NodeClient ncli = newNodeClient(node);
            NodeRoleStatus rs =  ncli.changeRoleState(role, State.STOPPED);

            //TODO: could move this inside client if retray behavior could be deemed flexible enough
            if (rs.getState() != State.STOPPED) {
                //wait a bit and try once more //TODO: would more re-tries ever help? --possibly if we are REST verifying components and their ReST start is delayed
                logger.warn(" initial stop verification failed for role " + role.toString() + " on node " + node.getHost() + " will re-try once");

                Thread.sleep(2000);
                rs = ncli.getRoleStatus(role);
			}

            if (rs.getState() != State.STOPPED) {
                //TODO: improve -- but for now, only log and move on to try other roles
                //throw new PlatformStateException(" failed to stop role " + role.toString() + " on node " + node.getHost() + "and no quorum settings available");
                logger.error(" failed to stop role " + role.toString() + " on node " + node.getHost() + " state may not be as requested ");

            }

        }


    }

    public NodeClient newNodeClient(PlatformNode node) {
    	return new DefaultNodeClient(node);
    }


    public void startPlatform() throws Exception {

    	//verify that we can run ( -> VIABLE )
        verifyPlatformSpec();

        //start all platform node roles
        startPlatformNodes();

        //make sure system tenant is initialized at this point
        initializeSystemTenant();

        //get us to a running state
        startProcessing();

        //if no errors, set state
        restCluster.setState(PlatformState.RUNNING);

    }

    protected void initializeSystemTenant() throws Exception {

    	try {

        	//provision tenant 0 - this will be a no-op if already provisioned
        	//new PlatformClient(platformMgr.getPlatform()).provision(new Tenant(Tenant.SYSTEM_TENANT_ID));
			member.provision(new Tenant(Tenant.SYSTEM_TENANT_ID));

    	} catch (Exception e) {
    		logger.info("exception provisioning system tenant", e);
    		throw e;
    	}

    }

    public void verifyPlatformSpec() throws ClusterException{
        //wait for "complete" platform installation
        logger.info("Checking platform readiness ");
        setPlatformState(PlatformState.PENDING);
        try {
            synchronized (platformMgr) {

                while (!platformMgr.isPlatformComplete()) {

                    platformMgr.wait(1000L); //wait for a change - timeout just in case some problem with getting the platform notification

                }

                setPlatformState(PlatformState.VIABLE);
            }
        }
        catch  (Exception e){
            logger.error(" exception waiting for valid platform specification ", e);
            throw new InitializationException("error verifying platform spec", e);
        }
    }


	public  void stopProcessing() throws Exception {

		stopDrivers();

		setPlatformState(PlatformState.READY); //services up, but no spark drivers

	}

    public void startProcessing() throws Exception{

        if (!isPlatformStartEnabled()) {
            logger.warn(" Platform startup is disabled.  This should only happen in test modes ");
            return;
        }

        if (!member.isMaster()) {
            throw new InitializationException("got platform start request but this node is not the master (" + EnvironmentSettings.getHost() + ")");
        }

        if (getPlatformState() == PlatformState.RUNNING) {
            throw new PlatformStateException(" platform already in state " + PlatformState.RUNNING.toString());
        }

        //submit or re-register spark drivers to begin processing

        logger.info("startProcessing()\n\n");
        setPlatformState(PlatformState.STARTING);

        while (getPlatformState() == PlatformState.STARTING) {

            //wait for status of
            initializeSpark();

        }

        if (getPlatformState() != PlatformState.RUNNING) {
            throw  new PlatformStateException(" failed to start platform... state is " + getPlatformState().toString());
        }
		else {
			logger.warn(" platform state set to RUNNING startProcessing succeeded ");
		}


    }


    protected void initializeSpark() throws  Exception{

        //TODO: configurable retry intervals
        //TODO: externally set abort bit?

        //provision spark drivers
        logger.info("Initializing spark processing...");
        logger.info("provisioning drivers and global topics");

        //get docs and load local hashes of drivers
        setupDrivers();

        //wait for drivers to report in the event we, REST, were the service who went down
        if (waitForExistingDrivers()) {
            setPlatformState(PlatformState.RUNNING);
            return;
        }

        int tries = 1;
        while (!provisionGlobalTopics(platformMgr.getPlatform())) {
            try {
                tries++;
                setPlatformStatus(" Topics not provisioned yet.  Waiting to try again... next try will be number " + tries);
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        tries = 1;
        while (!startDrivers()) {
            try {
                tries++;
                setPlatformStatus(" Driver startup not successful.  Waiting then trying again... next try will be number " + tries);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        setPlatformState(PlatformState.RUNNING);
		logger.info("common JAR is version " + TmpVersion.VERSION);

    }
    public void setupDrivers()  {

        logger.info("provisioning Spark drivers ");
        List<Document> drivers = null;
        try {
            drivers = member.getDocs().getDocumentsOfType(DocumentType.STREAM_DRIVER);
        } catch (Exception e) {
            throw new PlatformStateException(" error retrieving global driver documents ", e);
        }

        _globalDrivers = new HashMap<String, DriverStatus>();
        _processors = new HashMap<String, String>();

        //now start the drivers
        for (Document driver : drivers) {

            if (driver.isBodyEnabled()) {

                //String tag = Long.toString(System.currentTimeMillis());
                String tag = "0";

                DriverStatus ds = new DriverStatus( "Unknown", driver, DriverState.PROVISIONED);
                ds.setDriverTag(tag);
                logger.info(" with tag " + tag);
                logger.info("provisioning driver for tracking " + ds.getDriverName());
                _globalDrivers.put(ds.getDriverName(), ds);

            }
            else {
                logger.warn( "skipping provisioning for disabled driver " + driver.getName() );

            }

        }
    }

    public boolean startDrivers () throws ClusterException {

        for (DriverStatus ds : _globalDrivers.values()) {
            if (ds.get_state() == DriverState.REGISTERED) {
                logger.warn(" driver " + ds.getDriverName() + " already running ");
                continue;
            }

            else if (ds.get_state() == DriverState.PROVISIONED || ds.get_state() == DriverState.ERROR) {
                try {
                    logger.info("submitting spark driver " + ds.getDriverName());
                    String driverID = SparkDriverSubmit.submit(platformMgr.getPlatform(), ds.getDriverDef(), ds.getDriverName());
                    ds.set_driverID(driverID);
                    logger.info("driver submitted");
                    ds.set_state(DriverState.RUNNING);
				}
                catch (Exception ex) {
                    //TODO: allow for verbose logging
                    logger.error(" Error starting Spark driver " + ex.getMessage(), ex);

                    return false;
                }
			} else {
				logger.warn("taking no action on driver " + ds.getDriverName()+ " with state " + ds.get_state().toString());
            }


        }

        logger.info("Spark drivers submitted");

        waitForSubmittedDrivers();

		return true;

	}

    private boolean provisionGlobalTopics(Platform platform) {

		if (interrupt) {
			logger.warn("interrupted");
			interrupt = false;
			throw new RuntimeException( "interrupt received");
		}

        List<Document> globalStreams = null;
        try {
            globalStreams = member.getDocs().getDocumentsOfType(DocumentType.STREAM_GLOBAL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.warn(" About to provision topics for " + globalStreams.size() + " global streams ");

        return PreReqUtil.provisionTopicsTolerateErrors(platform, this, globalStreams) ;

    }

    private boolean waitForSubmittedDrivers() throws ClusterException {

        setPlatformStatus("Waiting for submitted drivers to register...");
        while (!isDriverRegistrationComplete()) {
            try {
                Thread.sleep(2000);
                setPlatformStatus("Still waiting for submitted drivers to register...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        setPlatformStatus("Driver registration complete");

        return true;
    }

    private boolean waitForExistingDrivers() throws ClusterException {
        logger.info("Waiting for any running drivers to register...");

        //TODO: replace this with REST call to spark UI port to check for running drivers of the names we use
        int driver_report_timeout = 10; //default value
        try {
            driver_report_timeout = platformMgr.getPlatform().getNode(NodeRole.REST).getSettingInt(Rest.DRIVER_REPORT_TIMEOUT);
        }
        catch (Exception ex) {
            logger.warn(" driver_report_timeout not accessible due to error: " + ex.getMessage());
            logger.warn(" driver_report_timeout set to 10 second default ");
        }

        boolean allRegistered = false;
        long startTime = System.currentTimeMillis();
        long maxWait = driver_report_timeout * 1000L; //after this much time we fail
        while (true) {

            if(isDriverRegistrationComplete()) {
                allRegistered = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!((System.currentTimeMillis() - startTime) < maxWait)) {
                logger.warn("Not all drivers registered within timeout period of " + driver_report_timeout + " seconds.  Submitting required drivers...");
				return false;
			}
		}

		if (allRegistered) {
			setPlatformStatus("all drivers registered...exiting Spark intialization");
            return true;
        }
        return false;

    }

    public boolean isDriverRegistrationComplete()
    {

		if (interrupt) {
			logger.warn("interrupted");
			interrupt = false;
			throw new RuntimeException( "interrupt received");
		}

        //registration is not "complete" (semantically) if no drivers are provisioned
        if (_globalDrivers.size() == 0) {
            return false;
        }

        int regCount = 0;

        for (DriverStatus ds : _globalDrivers.values()) {
            logger.debug(" driver state detected as " + ds.get_state().toString() + " for driver " + ds.getDriverName() + " with ID " + ds.get_driverID() );
            if (ds.get_state() == DriverState.REGISTERED){
                regCount++;
            }

        }
        if (regCount == _globalDrivers.size()) {
            return true;
        }
        else {return false;}
    }

    /**
     * If an upgrade has been requested, this will hold the upgrade information
     */
    PlatformUpgrade upgrade;

    /**
     * The platform state that has been requested, if any
     */
    PlatformState requestedState;

//stop drivers

	public  boolean getDriverStopBit() {
		return driverStop;
	}
	private  boolean driverStop = false;
	private  long lastDriverReport = 0;
	public  void driverHeardFrom() {
		lastDriverReport = System.currentTimeMillis();
		logger.debug("recording last driver report: " + lastDriverReport);
	}
	public  void stopDrivers() throws Exception{


		try {
			//add pending stop to zookeeper -- may exist if previous attempt failed
			setPlatformState(PlatformState.STOPPING);

			driverStop = true;
			int waits = 0;

			long l = System.currentTimeMillis();
			long l2 = lastDriverReport;
			long l3 = l - l2;
			while (l3 < 10000) {
				logger.warn("last driver report was " + l3 + " millis ago, so waiting " + (10000 - l3));
				try {
					Thread.sleep(10000 - l3);
				} catch (InterruptedException e) {
				}
				waits++;
				if (waits > 60) {
					throw new PlatformStateException(" Spark processing not stopping as requested");
				}

				l = System.currentTimeMillis();
				l2 = lastDriverReport;
				l3 = l - l2;

			}
		}
		finally {
			driverStop = false;
		}


	}


}
