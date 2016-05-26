package com.aw.platform;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The platform interface
 *
 *
 *
 */
public interface Platform {

	/**
	 * The name of the local platform
	 */
	public static final String LOCAL = "local";


	/**
	 * The name of the default platform with all services running local for testing
	 */
	public static final String DEFAULT = "default";

	/**
	 * Get nodes with the given role
	 *
	 * @param role The node role being requested
	 * @return The list of platform nodes
	 */
	public List<PlatformNode> getNodes(NodeRole role);

	/**
	 * Get the platform-wide default role settings for the given node role
	 *
	 * @param role The node role whose setting is requested
	 * @param setting The setting requested for the given node type
	 * @return The platform-scoped setting for this role
	 */
	public NodeRoleSettings getSettings(NodeRole role);

	/**
	 * Get all platform nodes
	 *
	 * @return The list of platform nodes
	 */
	public Map<String,PlatformNode> getNodes();

	/**
	 * Get node roles for a given host
	 * @param host The host whose nodes are being requested
	 * @return The list of platform node roles for the host being requested
     */
	public Collection<NodeRole> getNodeRoles(String host);

	/**
	 * Gets any node of a particular type. Exact method details (round robin, always the first) are not
	 * defined.
	 *
	 * @param role The role needed
	 * @return A node of the given role
	 */
	public PlatformNode getNode(NodeRole role);

	/**
	 * Return platform Node for specified host
	 * @param host
	 * @return
	 */
	public PlatformNode getNode(String host);

	/**
	 * Does the platform have the given role defined?
	 *
	 * @param nodeRole The node role needed
	 * @return Whether the role is defined in the platform
	 */
	public boolean hasRole(NodeRole nodeRole);

	/**
	 *
	 * @return the node for the local node
	 */
	public PlatformNode getMe();

}
