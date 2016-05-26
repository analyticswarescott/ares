package com.aw.platform.monitoring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.aw.platform.NodeRole;
import com.aw.platform.restcluster.PlatformController.PlatformState;

public interface PlatformStatus extends Status {

	/**
	 * @return Status for each tenant in the platform
	 */
	public Collection<TenantStatus> getTenantStatus();

	/**
	 * @return Status for each role in the platform
	 */
	public Map<NodeRole, RoleStatus> getRoleStatus();



	/**
	 * @return The overall state of the platform based on the controller
	 */
	public PlatformState getState();

	/**
	 * @return The version of the platform
	 */
	public String getVersion();


	public Map<String, List<NodeStatus>> getNodeStatuses();

}
