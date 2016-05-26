package com.aw.platform.monitoring;

import java.util.ArrayList;
import java.util.Collection;

import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.NodeClient;
import com.aw.platform.nodes.NodeClientFactory;

/**
 * Status for a platform role
 *
 *
 *
 */
public class DefaultRoleStatus implements RoleStatus {

	public DefaultRoleStatus() {
	}

	/**
	 * Collect status for the given role
	 *
	 * @param platform
	 * @param doc
	 */
	public DefaultRoleStatus(Platform platform, DocumentHandler docs, NodeRole role, NodeClientFactory clients) throws Exception {

		this.role = role;
		collect(platform, docs, clients);

	}

	protected void collect(Platform platform, DocumentHandler docs, NodeClientFactory clients) throws Exception {

		this.nodeStatus = new ArrayList<NodeRoleStatus>();

		//get status for all nodes running this role
		for (PlatformNode node : platform.getNodes(this.role)) {

			//try to get the status for the role on this node
			NodeRoleStatus status = null;
			try {

				NodeClient client = clients.newNodeClient(node);
				status = client.getRoleStatus(role);

			} catch (Exception e) {

				//include the exception in the status for this node, if one occurred while trying to collect status
				status = new NodeRoleStatus(this.role, State.ERROR, "error communicating with node: " + e.getMessage(), e);

			}

			this.nodeStatus.add(status);

		}

	}

	public Collection<NodeRoleStatus> getNodeStatus() { return this.nodeStatus;  }
	public void setNodeStatus(Collection<NodeRoleStatus> nodeStatus) { this.nodeStatus = nodeStatus; }
	private Collection<NodeRoleStatus> nodeStatus;

	public NodeRole getRole() { return this.role;  }
	public void setRole(NodeRole role) { this.role = role; }
	private NodeRole role;

	public Health getHealth() { return this.health;  }
	public void setHealth(Health health) { this.health = health; }
	private Health health;

}
