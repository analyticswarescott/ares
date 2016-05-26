package com.aw.common.inject;

import javax.inject.Provider;

import com.aw.common.tenant.Tenant;
import com.aw.platform.Platform;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.RestMember;

public class RestClusterFactory extends AbstractTenantResourceProvider<RestCluster> {

	private Provider<Platform> platform;
	private RestCluster restCluster;

	public RestClusterFactory(Provider<Platform> platform) {
		this.platform = platform;
	}

	@Override
	protected RestCluster getInstance(Tenant tenant) throws Exception {

		if (restCluster == null) {

			//create the logical reference to the rest cluster here - don't initialize here
			restCluster = new RestCluster(platform, RestMember.CLUSTER_NAME);

		}

		return restCluster;

	}

}
