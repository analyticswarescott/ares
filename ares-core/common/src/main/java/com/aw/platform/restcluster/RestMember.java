package com.aw.platform.restcluster;

import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.Member;
import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformUpgrade;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A member of a rest cluster.
 *
 *
 *
 */
@JsonDeserialize(as=RemoteRestMember.class)
public interface RestMember extends Member {

	/**
	 * The statically known name of the rest cluster - this does not change
	 */
	public static final String CLUSTER_NAME = "rest";

	/**
	 * @return The platform host for this rest member. Will match a host in the platform definition.
	 */
	public String getHost();

	/**
	 * Upgrade the platform.
	 *
	 * @param versionId
	 * @throws ClusterException
	 */
	public void upgrade(PlatformUpgrade upgrade) throws ClusterException;

	/**
	 * @return Is this rest member master
	 */
	public boolean isMaster();

	/**
	 * Provsion a tenant within the platform
	 *
	 * @param tenant
	 * @return if the provisioning succeeded
	 */
	public boolean provision(Tenant tenant) throws Exception;

	public PlatformController.PlatformState getPlatformState() throws Exception;


}
