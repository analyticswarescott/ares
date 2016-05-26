package com.aw.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.JSONUtils;
import com.aw.document.DocumentHandler;
import com.aw.document.body.IBodyInitializable;
import com.aw.util.SetMap;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Default platform type loaded from the document handler in JSON format.
 *
 *
 *
 */
public class DefaultPlatform implements Platform, IBodyInitializable {

	public static final Logger logger = LoggerFactory.getLogger(DefaultPlatform.class);

	private static final String SETTINGS = "settings";
	private static final String NODES = "nodes";

	/**
	 * known localhost node used in some single node configurations
	 */
	private static final String LOCALHOST = "localhost";

	/**
	 * Initialize from json - requires custom logic for now because of the way we cache this
	 * information.
	 */
	@Override
	public void initialize(Object data, DocumentHandler docs) throws Exception {

		JSONObject json = new JSONObject(data.toString());

		//optionally initialize platform level settings
		if (json.has(SETTINGS)) {
			Map<String, Map<String, Object>> settingsMap = new HashMap<>();
			JSONUtils.updateFromString(json.get(SETTINGS).toString(), settingsMap);
			m_platformSettings.initialize(null, settingsMap);
		}

		//initialize node specific settings
		Map<String, Map<String, Map<String, Object>>> nodes = new HashMap<>();
		if (!json.has(NODES)) {
			throw new InitializationException("missing \"nodes\" key in platform configuration");
		}
		JSONUtils.updateFromString(json.get(NODES).toString(), nodes);

		//initialize the nodes
		for (Map.Entry<String, Map<String, Map<String, Object>>> node : nodes.entrySet()) {
			DefaultPlatformNode curNode = new DefaultPlatformNode();
			curNode.setHost(node.getKey());
			curNode.initialize(this, node.getValue());
			add(curNode.getHost(), curNode);
		}

	}

	private SetMap<NodeRole, PlatformNode> m_nodesByRole = new SetMap<NodeRole, PlatformNode>();

	/**
	 * Attempts to get the platform-wide setting for the given role
	 *
	 * @param role The role whose setting is being requested
	 * @param setting The setting being requested
	 * @return The setting, or null if not defined
	 */
	@Override
	public NodeRoleSettings getSettings(NodeRole role) {
		return m_platformSettings == null ? null : m_platformSettings.get(role);
	}

	/**
	 * @return The nodes of the requested type, may be an empty list if no nodes of that type are available
	 */
	@Override
	public List<PlatformNode> getNodes(NodeRole role) {

		Set<PlatformNode> nodes = m_nodesByRole.get(role);

		//build the return list
		List<PlatformNode> ret = null;
		if (nodes == null) {
			ret = Collections.emptyList();
		} else {
			ret = new ArrayList<>(nodes);
		}

		return ret;
	}




	@Override
	public Collection<NodeRole> getNodeRoles(String host) {
		PlatformNode node = getNode(host);

		if (node == null) {
			return Collections.emptyList();
		}

		else {
			return node.getRoles();
		}
	}

	@Override
	public String toString() {
		return JSONUtils.objectToString(this);
	}


	@Override
	public PlatformNode getNode(NodeRole role) {

		//for now just the first
		List<PlatformNode> nodes = getNodes(role);
		if (nodes != null && nodes.size() > 0) {
			return nodes.get(0);
		}

		else {
			//return null since we must be waiting for this node to register TODO: revisit this
			return null;
			//throw new InvalidRequestException("Asked for a role for which we have no nodes: " + role);
		}

	}

	@Override
	public PlatformNode getNode(String host) {

		PlatformNode ret = m_nodes.get(host);
		return ret;

	}

	/**
	 * Add a node to this platform object
	 *
	 * @param node
	 */
	public void add(String host, PlatformNode node) {

		//tell the node what platform it belongs to
		node.setPlatform(this);

		m_nodes.put(host, node);

		//cache by role
		for (NodeRole role : node.getRoles()) {
			m_nodesByRole.add(role, node);
		}

	}

	@Override
	public boolean hasRole(NodeRole nodeRole) {

		//true if we have at least 1 node for this role
		Set<PlatformNode> nodes = m_nodesByRole.get(nodeRole);
		return nodes != null && nodes.size() > 0;

	}

	@JsonIgnore
	public PlatformNode getMe() {
		PlatformNode me = getNode(EnvironmentSettings.getHost());

		//TODO: localhost may still be valid at the moment
		if (me == null) {
			me = getNode(LOCALHOST);
		}

		return me;
	}

	public Map<String, PlatformNode> getNodes() { return m_nodes; }
	Map<String, PlatformNode> m_nodes = new HashMap<String, PlatformNode>();

	//holds settings at the platform level - these settings apply to all instances of each role, unless the same
	//setting is specified within the node itself in which case it takes precendence
	public DefaultPlatformNode getSettings() { return m_platformSettings; }
	private DefaultPlatformNode m_platformSettings = new DefaultPlatformNode();

}
