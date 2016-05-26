package com.aw.platform;

import java.util.List;
import java.util.Map;

import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.RoleOSCommand;

/**
 * A role manager handles the specifics of managing a particular role on a node in the platform.
 *
 *
 *
 */
public interface RoleManager {

	/**
	 * @return The home directory for this role
	 */
	public String getHome();

    /**
     *
     * @return Any environment settings required before executing OS commands for this role
     */
    public Map<String, String> getEnv();

	/**
	 * @return The role managed by this manager
	 */
	public NodeRole getRole();

	/**
	 * Set up the role by modifying its configuration temmplates based on Platform settings
	 *
	 * @throws Exception if anything goes wrong
	 */
    public void configure() throws Exception;

    /**
     * @return The status of the given role
     */
    public NodeRoleStatus getStatus() throws NodeOperationException;

    /**
     * Start the role
     *
     * @throws Exception if anything goes wrong
     */
    public void start() throws Exception;

    /**
     * Stop the role
     *
     * @throws Exception If anything goes wrong
     */
    public void stop() throws Exception;

    /**
     * Initialize the role manager
     *
     * @param node The node on which this role manager is running
     */
    public void init(PlatformNode node);

    public List<RoleOSCommand> getStartCommands() throws Exception;
    public List<RoleOSCommand> getStopCommands() throws Exception;


}
