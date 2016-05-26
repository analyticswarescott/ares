package com.aw.platform;

import java.util.Map;

import com.aw.common.cluster.zk.ZkCluster;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.restcluster.RestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.nodes.NodeClient;

/**
 * Methods to control changes to platform code and versioning
 */
public class PlatformUpdater  {


	public static final Logger logger = LoggerFactory.getLogger(PlatformUpdater.class);

	public static final String NONE = "none";

	public static final String UPGRADE_VERSION = "upgrade_version";

	private PlatformUpgrade upgrade; //the watermark version all nodes must adhere to
	private PlatformMgr platformMgr;
	private RestCluster cluster;

	public PlatformUpdater() {
	}

	public PlatformUpdater(RestCluster cluster, PlatformMgr platformMgr, PlatformUpgrade upgrade) throws Exception {

		this.cluster = cluster;
		this.platformMgr = platformMgr;
		this.upgrade = upgrade;
		logger.warn("about to upgrade to version " + upgrade.getVersionId() +  " using upgrade file: " + upgrade.getFilename());
	}


	private boolean nodeOnCorrectVersion(PlatformNode node) throws Exception {
		NodeClient nc = new DefaultNodeClient(node);
		NodeStatus ns = null;



		try {ns = nc.getNodeStatus();}
		catch (Exception ex) {
			logger.warn(" exception checking node version " + ex.getMessage());
		}

		int waitForResponsive = 0;
		while (true) {

			boolean wait = false;
			if (ns == null) {
				wait = true;
				logger.warn("node " + node.getHost() + " is not yet responsive");
			}
			else if (ns.getState() == NodeStatus.State.UPGRADING) {
				wait = true;
				logger.warn("node " + node.getHost() + " is UPGRADING ...waiting");
			}

			if (wait) {
				waitForResponsive++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
				}
				if (waitForResponsive > 15) {
					throw new Exception(" timed out waiting for node " + node.getHost() + " to respond ");
				}
				try {
					ns = nc.getNodeStatus();
				} catch (Exception ex) {
					logger.warn(" exception checking node version " + ex.getMessage());
				}
			}
			else {
				break;
			}

		}



		logger.warn(" node has reported a state of "  + ns.getState().toString().toUpperCase() );


		if (upgrade == null) {
			return true;
		}


		String expectedVersion = upgrade.getVersionId();
		String reportedVersion = ns.getVersion();


		if (!(ns.getVersion().equals(upgrade.getVersionId()))) {
			logger.error(" expected version was " + expectedVersion + " actual version was " + reportedVersion);
			return false;
		} else {
			return true;
		}

	}


	private String currenthost;
	private boolean nodeHasReported;

	public boolean checkNodeVersion(PlatformNode node) {

		try {
			NodeClient nc = new DefaultNodeClient(node);
			NodeStatus ns = nc.getNodeStatus();//TODO: upgrading status...needed?

			logger.warn("checkNodeVersion: version of node " + node.getHost() + " is " + ns.getVersion());

			if (!nodeOnCorrectVersion(node)) {
				logger.warn(" issuing request to update node " + node.getHost() + " from " + ns.getVersion() +
					" to " + upgrade.getVersionId());

				blockingNodePatch(nc, node);
			}
			else {
				//ensure all the node's services are running
				nc.changeNodeState(NodeStatus.State.RUNNING);

				//wait for running state
				waitForRunning(node);
			}
			return true;
		}
		catch (Exception ex) {
			platformMgr.handleException(ex, NodeRole.REST);
			return false;
		}


	}



	private void blockingNodePatch(NodeClient nc, PlatformNode node) throws Exception{

		logger.warn(" setting node in progress to " + node.getHost());
		cluster.getSystemZkAccessor().put(ZkPurpose.UPGRADE, ZkCluster.Key.UPGRADE_NODE_IN_PROGRESS, node.getHost());

		currenthost = node.getHost();

			//issue patch instruction
			logger.warn(" issuing patchNode command");
			nodeHasReported = false;

			boolean nodeResponding = false;
			nc.patchNode(upgrade);

		//now wait for node to respond to a version verification
		//TODO: what is the timeout here?
		int tries = 0;
		while (!nodeOnCorrectVersion(node)) {
			logger.warn("node " + node.getHost() + " has yet to report ");
			try {
				Thread.sleep(2000); //TODO improve
			}
			catch (InterruptedException ex) {
				//do nothing...this can be interrupted
			}

			if (tries > 15) {
				throw new PlatformStateException(" timed out waiting for node to respond to version check ");
			}

		}

		//re-start all of this node's defined services
		logger.warn(" node " + node.getHost() + " has reported on new version...re-starting services");
		nc.updateNode(node);
		nc.changeNodeState(NodeStatus.State.RUNNING);

		waitForRunning(node);

		logger.warn(" clearing node in progress...was:  " + node.getHost());
		cluster.getSystemZkAccessor().put(ZkPurpose.UPGRADE, ZkCluster.Key.UPGRADE_NODE_IN_PROGRESS, NONE);


	}


	public void patchCluster() throws Exception{

	//complete any in-progress node if one exists
		String nip = cluster.getSystemZkAccessor().get(ZkPurpose.UPGRADE, ZkCluster.Key.UPGRADE_NODE_IN_PROGRESS, String.class);

		if (nip != null ) {
			if (!nip.equals(NONE)) {
				logger.warn(" in-progress node detected " + nip);
				PlatformNode ipNode = platformMgr.getPlatform().getNode(nip);
				if (!checkNodeVersion(ipNode)) {
					logger.warn(" node " + nip + " cannot be checked...skipping ");
				}
			}
		}

   //proceed with normal patch logic
		Map<String, PlatformNode> nodes = platformMgr.getPlatform().getNodes();

		//nodes with no rest can be safely patched
		for (PlatformNode node : nodes.values()) {
			logger.warn("evaluating node " + node.getHost());
			if (node.getHost().equals(EnvironmentSettings.getHost()) || node.getHost().equals("localhost")) {
				logger.warn(" skipping current REST node as this will be patched last ");
				continue;
			}

			if (! (node.getRoles().contains(NodeRole.REST))) {
				logger.warn(" non-REST node " + node.getHost() + " can be patched");
				if (!checkNodeVersion(node)) {
				   logger.warn(" node " + node.getHost() +  " error during version check...skipping ");
				}

			}
			else {
				logger.warn("skipping node " + node.getHost() + " because it runs REST");
			}
		}

		//nodes with REST that are not master (i.e. ME) can be patched

		for (PlatformNode node : platformMgr.getPlatform().getNodes(NodeRole.REST)) {
		  if (node.getHost().equals(EnvironmentSettings.getHost()) || node.getHost().equals("localhost")) {
			  logger.warn(" skipping current REST node as this will be patched last ");
			  continue;
		  }
		  else {
				  logger.warn(" node " + node.getHost() + " evaluated as REST node and not this host");
				  if (!checkNodeVersion(node)) {
					  logger.warn(" node " + node.getHost() +  " cannot be checked...skipping " );
				  }
		  }
		}

		//now we patch our controlling self, the next master elected should see the state and verify
		PlatformNode me = platformMgr.getPlatform().getNode(EnvironmentSettings.getHost());
		if (me == null) {
			me = platformMgr.getPlatform().getNode("localhost");
			//TODO: platform is not getting updated -- need to deal with this
		}
			logger.warn(" all non-REST and follower-REST nodes patched...issuing self-patch request ");
			if (!checkNodeVersion(me)) {
				logger.warn(" node " + me.getHost() +  " cannot be checked...skipping ");
			}
		//if all nodes are correct, then add new config

		//set platform state back to STARTING

	}




	private void waitForRunning(PlatformNode node) throws Exception {
		NodeClient nc = new DefaultNodeClient(node);
		NodeStatus ns = null;



		try {ns = nc.getNodeStatus();}
		catch (Exception ex) {
			logger.warn(" exception getting node status in  waitForRunning() " + ex.getMessage());
		}

		int waitForRunning = 0;
		while (true) {

			boolean wait = false;
			if (ns == null) {
				wait = true;
				logger.warn("waitForRunning: node " + node.getHost() + " is not yet responsive");
			}
			else if (ns.getState() != NodeStatus.State.RUNNING) {
				wait = true;
				logger.warn("waitForRunning: node " + node.getHost() + " is still " + ns.getState() + " ...waiting");
			}

			if (wait) {
				waitForRunning++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
				}
				if (waitForRunning > 30) {
					throw new Exception("waitForRunning: timed out waiting for RUNNING state " + node.getHost() + " to respond ");
				}
				try {
					ns = nc.getNodeStatus();
				} catch (Exception ex) {
					logger.warn("waitForRunning: exception checking node version " + ex.getMessage());
				}
			}
			else {
				break;
			}

		}


		logger.warn("waitForRunning: node " + node.getHost() + " has reported a state of "  + ns.getState().toString().toUpperCase() );


	}



	/**
	 * Config changes are applied centrally to REST only once after all nodes are confirmed as patched
	 * @param pd
	 */
	public void applyConfigFromPatch( ) {
		//TODO: need to decide if we will write updated configs to disk during node patch
	}


}
