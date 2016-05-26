package com.aw.platform.nodes;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.platform.nodes.managers.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentHandler;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.RoleManager;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.exceptions.NodeOperationGroupedException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.monitoring.os.DefaultOSInfoProvider;
import com.aw.platform.monitoring.os.OSPerfStats;
import com.aw.platform.monitoring.os.SysStatInterface;

/**
 * This is used in the node management service.
 *
 * TODO: why is this in common?
 */
public class NodeManager {

	private static final Logger logger = Logger.getLogger(NodeManager.class);


	private static NodeRole[] ROLE_STOP_ORDER = new NodeRole[]{NodeRole.SPARK_MASTER,
		NodeRole.SPARK_WORKER, NodeRole.ELASTICSEARCH, NodeRole.KAFKA,NodeRole.HDFS_NAME, NodeRole.HDFS_DATA,
		NodeRole.HDFS_JOURNAL, NodeRole.REST, NodeRole.ZOOKEEPER};

	private static NodeRole[] ROLE_START_ORDER = new NodeRole[]{NodeRole.SPARK_MASTER,
		NodeRole.SPARK_WORKER, NodeRole.ELASTICSEARCH, NodeRole.KAFKA,NodeRole.HDFS_NAME, NodeRole.HDFS_DATA,
		NodeRole.HDFS_JOURNAL, NodeRole.REST, NodeRole.ZOOKEEPER};


    protected PlatformNode m_node;
    private Provider<PlatformMgr> platformMgr;
    private Provider<DocumentHandler> docs;
    private SysStatInterface sysStats;

	public NodeStatus.State getState() {
		return m_state;
	}
	public void setState(NodeStatus.State m_state) {
		this.m_state = m_state;
	}

	protected NodeStatus.State m_state  = NodeStatus.State.UNKNOWN;

	DefaultOSInfoProvider statProvider = new DefaultOSInfoProvider();

	@Inject @com.google.inject.Inject
    public NodeManager(Provider<PlatformMgr> platformMgr, Provider<DocumentHandler> docs, SysStatInterface sysStatReader,
    		RestRoleManager rest,
    		HdfsDataRoleManager hdfsData,
    		HdfsNameRoleManager hdfsName,
    		HdfsJournalRoleManager hdfsJournal,
    		ElasticSearchRoleManager elasticsearch,
    		KafkaRoleManager kafka,
    		ZookeeperRoleManager zoo,
    		SparkMasterRoleManager master,
    		SparkWorkerRoleManager worker,
    		ConfigDBMasterRoleManager configDb,
			ConfigDBWorkerRoleManager configDbWorker,
    		DefaultRoleManager defaultRoleManager) throws Exception {
		this(platformMgr, docs, sysStatReader, rest, hdfsData, hdfsJournal, hdfsName, elasticsearch, kafka, zoo, master, worker, configDb, configDbWorker, defaultRoleManager);
    }

	private NodeManager(Provider<PlatformMgr> platformMgr, Provider<DocumentHandler> docs, SysStatInterface sysStatReader, RoleManager... roleManagers) throws Exception {

		this.platformMgr = platformMgr;
		this.docs = docs;
		this.roleManagers = Arrays.asList(roleManagers).stream().collect(Collectors.toMap(RoleManager::getRole, (rm) -> rm));
		this.sysStats = sysStatReader;

    	init();

	}

    protected void init() throws Exception {

		logger.warn(" Node Manager instance: " + this.toString());
		//if FIRST_NODE is true, need to start REST

		m_state = State.UNKNOWN;

    }

    public RoleManager getRoleManager(NodeRole role) {
    	return roleManagers.get(role);
    }

	public List<OSPerfStats> getPerfStats(Instant timestamp) throws Exception {

		List<OSPerfStats> stats = statProvider.getPerfStats(platformMgr.get().getPlatform(), sysStats, timestamp);
		return stats;
	}


	int tries = 0;
    public boolean announcePresence() throws Exception {

    	try {

        	//get full platform back by announcing presence and cache it
			String platform = new PlatformClient(platformMgr.get().get()).announcePresence();
			tries = 0;
			cachePlatform(platform);
			return true;
        	//update ourselves
        	//updateNode(node);


    	} catch (Exception e) {

			tries++;
			if (tries == 1 ||  tries % 100 == 0) {
			//need to see the stack trace here
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
				pw.close();
				logger.warn(" error stack announcing presence :" + sw.toString());
				logger.info("error announcing presence in Node Manager after " + tries + " tries : " + e.getMessage());
			}

    		return false;
		}
	}

	public void updateNodeState(NodeStatus.State state) throws NodeOperationException, NodeOperationGroupedException {

		logger.warn(" update node state called: " + state);
		switch (state) {
			case RUNNING:
				logger.warn(" starting all roles because RUNNING state requested ");
				setState(State.STARTING);
				start(m_node.getRoles());
				setState(State.RUNNING);
				break;
			case STOPPED:
				logger.warn(" stopping all roles because STOPPED state requested ");
				setState(State.STOPPING);
				stopAll();
				setState(NodeStatus.State.STOPPED);
				break;
			default:
				throw new NodeOperationException("can't change node state to " + state);

		}

	}


	public void updateRoleState(NodeRole role, State state) throws NodeOperationException {

		logger.warn(" update state called: " + role + "-" + state + " : " + this + " roleManagers=" + roleManagers.keySet());
		switch (state) {
			case RUNNING:
				start(role);
				setState(NodeStatus.State.RUNNING);
				break;
			case STOPPED:
				stop(role);
				break;
			default:
				throw new NodeOperationException("can't change node state to " + state);

		}

	}

	public void cachePlatform(String platform) throws Exception{

		//TODO: do we need to convert to object?
		//DefaultPlatform p = JSONUtils.objectFromString(platform, DefaultPlatform.class);

		File f = new File(EnvironmentSettings.getConfDirectory() + File.separatorChar +  "platform.json");
		if (!f.exists()) {
			FileUtils.touch(f);
		}

		logger.debug("caching platform to " + f.getAbsolutePath());

		FileWriter w = new FileWriter(f, false); //overwrite
		w.write(platform);
		w.close();
	}

	private String getVersion() throws Exception {

		File f = new File(EnvironmentSettings.getDgHome() + File.separatorChar + "buildstamp");
		if (f.exists()) {
			String version = FileUtils.readFileToString(f);
			logger.info(" node code version is " + version);
			return version;
		}
		else {return "Unknown";}
	}



	private void evaluateState() throws Exception{

		//are all roles running?
		boolean allRunning = true;
		if (m_node != null) {
			for (NodeRole role : m_node.getRoles()) {
				if (role == NodeRole.NODE) {
					continue; //NODE role running de facto
				}
				else {
					State rs = getRoleStatus(role).getState();
					if (rs != State.RUNNING) {
						logger.info(" role " + role + " is in state " + rs + " ... not able to declare RUNNING");
						allRunning = false;
					}
				}
			}

			if (allRunning) {
				setState(State.RUNNING);
			}
		}

	}

	/**
	 * get overall Node status including current installed version
	 * @return Node Status
	 */
	public List<NodeStatus> getStatus(Instant timestamp) throws  Exception{

		List<NodeStatus> ret = new ArrayList<>();
		evaluateState();

		if (m_node == null) { //this will be true after a patch before re-start of services
			NodeStatus ns = new NodeStatus();
			ns.setState(getState());
			ns.setVersion(getVersion());
			ret.add(ns);
			return ret;
		}
		//return one object per stat entry
		if (timestamp.toEpochMilli() > 0) {
			List<OSPerfStats> stats = getPerfStats(timestamp);

			for (OSPerfStats stat : stats) {
				NodeStatus ns = new NodeStatus();
				ns.setState(getState());
				ns.setVersion(getVersion());
				ns.setPerfStats(stat);
				ret.add(ns);
			}

		}
		else { //return a stat-less obect to allow version verification (for upgrade)
			NodeStatus ns = new NodeStatus();
			ns.setState(getState());
			ns.setVersion(getVersion());
			ret.add(ns);
		}

		return ret;
	}

    /**
     * Get the status for a role on this node
     *
     * @param role The role to get status for
     * @return The status for the role
     */
    public NodeRoleStatus getRoleStatus(NodeRole role) throws NodeOperationException {
    	return getRoleManager(role).getStatus();
    }

	/**
	 * Update the configuration of the node, starting/stopping services as needed etc
	 *
	 * @param node
	 * @throws Exception
	 */
	public void updateNode(PlatformNode node) throws Exception {

		//null out platform in this JVM so configures get it fresh
		platformMgr.get().setPlatform(null);

		m_node = node;

		logger.info(" updating platform node...role count is:   " + node.getRoles().size());

		PlatformNode current = m_current;
		if (current == null) {

			//empty node as current if it's not set yet
			current = new DefaultPlatformNode();

		}

		logger.info("configuring " + node.getRoles());
		configure(node.getRoles());

		//updated current platform node
		m_current = node;

	}

	PlatformNode m_current = null;

    /**
     * Configure the given roles
     *
     * @param roles
     * @throws Exception
     */
    protected void configure(Iterable<NodeRole> roles) throws Exception {

    	//try to configure what we can
    	NodeOperationGroupedException group = null;

    	//on any error, throw it up immediately
    	for (NodeRole role : roles) {

    		try {

    			//configure the role
				configure(role);

    		} catch (NodeOperationException e) {

    			//create the exception if we need it
    			if (group == null) {
    				group = new NodeOperationGroupedException("error configuring roles on " + EnvironmentSettings.getHost());
    			}

    			//add the exception for this role
    			group.add(role, e);

    		}

    	}

    	if (group != null) {
    		throw group;
    	}

    }

    /**
     * Configure the role
     */
    public void configure(NodeRole role) throws Exception{

    	logger.info("CONFIGURE " + role + ": " + this);

		logger.info("configuring role " + role);

    	// a role manager to configure
		RoleManager rm = roleManagers.get(role);

		if (rm == null) {
			throw new Exception("could not find role manager for " + role + " (" + role.getRoleManagerType() + ")");
		}

    	//configure it
		if (!(role == NodeRole.NODE)) { //TODO: can't configure our own role

			rm.init(m_node); //TODO: could call back to get platform always, but this could create unexpected results in PlatformController logic
			rm.configure();

		}

		logger.info("putting role " + role + " in " + this + ".roleManagers: " + roleManagers.keySet());

    }

    public void start(Iterable<NodeRole> roles) throws NodeOperationGroupedException {

    	//try to start up what we can
    	NodeOperationGroupedException group = null;

		//TODO: improve, but need to start ZK first of present
		for (NodeRole role : roles) {
			try {

				//try to start
				if (role == NodeRole.ZOOKEEPER) { //TODO: can't start our own role
					logger.warn("starting ZOOKEEPER role with priority: " + role);
					start(role);
				}
			} catch (NodeOperationException e) {

				if (group == null) {
					group = new NodeOperationGroupedException("error starting roles on " + EnvironmentSettings.getHost());
				}

				group.add(role, e);

			}
		}

    	//start all of the roles
    	for (NodeRole role : roles) {

    		try {

    			//try to start -- skip NODE and ZOOKEEPER
				if (!(role == NodeRole.NODE || role == NodeRole.ZOOKEEPER)) { //TODO: can't start our own role
					logger.warn("starting role: " + role);
					start(role);
				}

    		} catch (NodeOperationException e) {

    			if (group == null) {
    				group = new NodeOperationGroupedException("error starting roles on " + EnvironmentSettings.getHost());
    			}

    			group.add(role, e);

    		}

    	}

    }

    public void start(NodeRole role) throws NodeOperationException {

    	//get the existing role manager for this role
        RoleManager rm = roleManagers.get(role);

        if (rm == null) {
        	throw new NodeOperationException("missing role manager when starting " + role);
        }

        try {

			//if the role is already starting or started, it's a no-op
			if (rm.getStatus().getState() == State.STARTING || rm.getStatus().getState() == State.RUNNING) {
				logger.info("not starting role " + rm.getRole() + " - role already started on node");
				return;
			}

            //start the role
			logger.info("starting role " + rm.getRole());

            rm.start();

        } catch (Exception e) {
        	throw new NodeOperationException("when trying to start " + role, e);
        }

    }

    /**
     * Stop the given roles
     *
     * @param roles The roles to stop
     * @throws Exception If anything goes wrong
     */
    public void stop(Iterable<NodeRole> roles) throws NodeOperationGroupedException {

    	NodeOperationGroupedException group = null;

    	for (NodeRole role : roles) {

    		try {

    			//try to stop this role
        		stop(role);

    		} catch (NodeOperationException e) {

    			//cache the exception if an error occurred
    			if (group == null) {
    				group = new NodeOperationGroupedException("error stopping roles on " + EnvironmentSettings.getHost());
    			}

    			group.add(role, e);

    		}
    	}

    }

    public void stop(NodeRole role) throws NodeOperationException {

    	//get the existing role manager
        RoleManager rm = roleManagers.get(role);

    	if (rm == null) {
        	throw new NodeOperationException("missing role manager when stopping " + role);
        }

        try {

            //stop the role
            rm.stop();

        } catch (Exception e) {
        	throw new NodeOperationException("while trying to stop " + role, e);
        }

    }

    /**
     * Stop all running services on the node
     *
     * @throws NodeOperationGroupedException
     */
    public void stopAll() throws NodeOperationGroupedException {

    	NodeOperationGroupedException group = null;

    	for (NodeRole role : ROLE_STOP_ORDER) {

    		try {

    			//try to stop this role
				if (m_node != null) {
					if (m_node.getRoles().contains(role)) {
						stop(role);
					}
				}

    		} catch (NodeOperationException e) {

    			//cache the exception if an error occurred
    			if (group == null) {
    				group = new NodeOperationGroupedException("error stopping all roles on " + EnvironmentSettings.getHost());
    			}

    			group.add(role, e);

    		}

    	}

    	if (group != null) {
    		throw group;
    	}

    }

	private String getHome() {
		return EnvironmentSettings.getDgHome() + "/roles/node_service";
	}

	public void patchNode(PlatformUpgrade platformUpgrade) throws Exception{

		try {
			//logger.warn(" patch node on NodeManager entered ");

			if (platformMgr.get() == null) {
				throw new Exception(" platform mgr is null in patch node");
			}

			if (platformMgr.get().getPlatform() == null) {
				throw new Exception(" platform is null in patch node");
			}

			setState(NodeStatus.State.UPGRADING);
			//get the patch file and place it locally

			FileWrapper wrapper = platformUpgrade.getPatchFile(platformMgr.get());
			wrapper.writeLocally(EnvironmentSettings.getDgHome() + File.separatorChar + "patch.tar.gz");
			logger.warn(" patch file written locally...going offline to patch ");

			stopAll(); //stop all services running on this




			//spawn a command to kill us,  patch this node, and re-start us
			String cmd = "./patch.sh";
			String dir = EnvironmentSettings.getDgHome();
			List<String> args = new ArrayList<String>();
			args.add(platformUpgrade.getVersionId());


			RoleOSCommand command = new RoleOSCommand(dir, cmd, args);
			command.setRoleManager(new DefaultRoleManager(platformMgr.get(), docs));

			//go off the air
			logger.warn(" applying patch, which will stop this process...");

			command.exec(new File(dir));
		}
		catch (Exception ex) {
			logger.error(" patch error " + ex.getMessage());
			throw ex;
		}

	}


    public static int getAvaliableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long getMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public Map<NodeRole, RoleManager> getRoleManagers() { return roleManagers; }
	private Map<NodeRole, RoleManager> roleManagers = new HashMap<>();


}
