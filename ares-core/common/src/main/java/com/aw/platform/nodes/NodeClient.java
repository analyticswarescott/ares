package com.aw.platform.nodes;

import com.aw.document.Document;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.monitoring.NodeStatus.State;

import java.time.Instant;
import java.util.List;

/**
 * An interface to a node service - this interface was created based on methods in DefaultNodeClient, which used to be
 * NodeClient. Some of the exceptions should probably be more strongly typed.
 *
 *
 *
 */
public interface NodeClient {

	/**
	 * Get the status of this node with perf stats as of timestamp or later
	 * @return The node status
	 * @throws Exception If anything goes wrong
	 */
	public List<NodeStatus> getNodeStatus(Instant timestamp) throws Exception;



	/**
	 * Get the status of this node without perf stats
	 * @return The node status
	 * @throws Exception If anything goes wrong
	 */
	public NodeStatus getNodeStatus() throws Exception;

	/**
	 * Patch the given node
	 * @param upgrade The upgrade information for the patch
	 * @throws Exception If anything goes wrong
	 */
	public void patchNode(PlatformUpgrade upgrade) throws Exception;

	/**
	 * Get the status of the role on this node
	 * @param role The role whose status is requested
	 * @return The status of the role on this node
	 * @throws Exception If anything goes wrong
	 */
    public NodeRoleStatus getRoleStatus(NodeRole role) throws Exception;

    /**
     * Change the stats of the role on this node
     *
     * @param role The role whose state should be changed
     * @param targetState The state to change to
     * @return The status of the role after the state change
     * @throws Exception If anything goes wrong
     */
    public NodeRoleStatus changeRoleState(NodeRole role, State targetState) throws Exception;

    /**
     * Change the state of the entire node
     *
     * @param targetState The requested node state
     * @return The status of the node as a result
     * @throws Exception If anything goes wrong
     */
	public NodeStatus changeNodeState(NodeStatus.State targetState) throws Exception;

	/**
	 * Update a node's roles
	 *
	 * @param node The node configuration containing the new role configurations
	 * @throws Exception If anything goes wrong
	 */
    public void updateNode(PlatformNode node) throws Exception;

    /**
     * Update the platform document on the node
     *
     * @param platformDoc The new platform
     * @throws Exception If anything goes wrong
     */
	public void updatePlatformCache(Document platformDoc) throws Exception;

}
