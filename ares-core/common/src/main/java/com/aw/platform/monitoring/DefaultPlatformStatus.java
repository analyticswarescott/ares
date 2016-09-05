package com.aw.platform.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.nodes.NodeClient;
import com.aw.platform.nodes.NodeClientFactory;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.inject.Provider;


/**
 * Defines the State and last status activity of a Platform
 */
public class DefaultPlatformStatus extends AbstractStatus implements PlatformStatus, NodeClientFactory {


	Provider<Platform> platform;
	DocumentHandler docs;
	RestCluster cluster;


	public DefaultPlatformStatus() {
	}

	/**
	 * Collect status
	 *
	 * @param platform The platform we are collecting status for
	 * @throws Exception If anything goes wrong
	 */
	public DefaultPlatformStatus(Provider<Platform> platform, DocumentHandler docs, RestCluster cluster) throws Exception {

		try {
			this.platform = platform;
			this.docs = docs;
			this.cluster = cluster;

		}
		catch (Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
	}


	public void collect(ZkAccessor zk, NodeClientFactory ncf, Instant timestamp) throws Exception{
		//initialize our collections
		this.roleStatus = new HashMap<NodeRole, RoleStatus>();
		this.tenantStatus = new ArrayList<TenantStatus>();

		//get the platform state
		this.state = cluster.getPlatformState();
		this.statusMessage = cluster.getPlatformStatus();

		//collect tenant status
		for (Document tenantDoc : docs.getAllTenants()) {

			//TODO: set body class for tenant
			Tenant tenant = new Tenant(tenantDoc.getBody());

			TenantStatus status = null;
			if (tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
				status = new SystemTenantStatus();
			}
			else {
				status = new DefaultTenantStatus();
			}

			//impersonate the tenant to collect its status
			Impersonation.impersonateTenant(tenant);
			try {
				status.collect(zk, platform.get(), tenant, docs);
			} finally {
				Impersonation.unImpersonate();
			}
			tenantStatus.add(status);

		}



		//collect role status
		for (NodeRole role : NodeRole.values()) {

			RoleStatus status = new DefaultRoleStatus(platform, docs, role, this);
			if (status.getNodeStatus().size() > 0) {
				this.roleStatus.put(role, status);
			}

		}

		//get node performance statuses since timestamp
		for (PlatformNode node : platform.get().getNodes().values()) {

			//TODO: how to mock this for testing?
			NodeClient nc = ncf.newNodeClient(node);
			List<NodeStatus> nsList = nc.getNodeStatus(timestamp);

			getNodeStatuses().put(node.getHost(), nsList);
		}

	}

	@Override
	public NodeClient newNodeClient(PlatformNode node) {
		return new DefaultNodeClient(node, platform);
	}

	public Map<NodeRole, RoleStatus> getRoleStatus() { return this.roleStatus;  }
	private Map<NodeRole, RoleStatus> roleStatus;


	@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
	@JsonSubTypes({
		@JsonSubTypes.Type(value = DefaultTenantStatus.class, name = "Tenant"),
		@JsonSubTypes.Type(value = SystemTenantStatus.class, name = "System") })
	public Collection<TenantStatus> getTenantStatus() { return this.tenantStatus;  }
	private Collection<TenantStatus> tenantStatus;

	public PlatformController.PlatformState getState() { return state; }
	private PlatformController.PlatformState state;

	/**
	 * @return Most recent status message
	 */
	public String getStatusMessage() { return statusMessage; }
	public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
	private String statusMessage = "(none)";

	/**
	 * @return current required patch level (version number)
	 */
	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }
	private String version = "";

	/**
	 * Return the status of the nodes in this platform
	 * @return
	 */
	@JsonProperty("node_statuses")
	public Map<String, List<NodeStatus>> getNodeStatuses() {return nodestatuses;}
	public void setNodestatuses(Map<String, List<NodeStatus>> nodestatuses) {
		this.nodestatuses = nodestatuses;
	}

	private Map<String, List<NodeStatus>> nodestatuses = new HashMap<>();

	public Instant getStatusTime() {return time;}
	public void setStatusTime(Instant time) {this.time = time;}
	private Instant time = Instant.now();

}
