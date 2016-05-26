package com.aw.rest.resources;

import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author jhaightdigitalguardian.com.
 */
@RunWith(MockitoJUnitRunner.class)
public class AresResourceTest {
	@Mock
	Provider<PlatformMgr> platformMgrProvider;
	@Mock
	PlatformMgr platformMgr;

	@Mock
	Provider<LocalRestMember> localRestMemberProvider;
	@Mock
	LocalRestMember localRestMember;

	@Mock
	Provider<RestCluster> restClusterProvider;
	@Mock
	RestCluster restCluster;

	@Mock
	TenantMgr tenantMgr;

	com.aw.rest.resources.AresResource aresResource;

	@Before
	public void setup() {
		when(platformMgrProvider.get()).thenReturn(platformMgr);
		when(localRestMemberProvider.get()).thenReturn(localRestMember);
		when(restClusterProvider.get()).thenReturn(restCluster);
		aresResource = new AresResource(platformMgrProvider, localRestMemberProvider, restClusterProvider, tenantMgr);
	}




	@Test
	public void testCreateTenant() throws Exception {
		Tenant tenant = new Tenant();
		Response response = aresResource.createTenant(tenant.getAsJSON().toString());
		assertEquals(200, response.getStatus());
		assertEquals("provisioned", response.getEntity());
		verify(localRestMember).provision(tenant);
	}

	@Test
	public void testDeleteTenant() throws Exception {
		String tenantId = "I'm not going to be a tenant anymore...";
		Response response = aresResource.deleteTenant(tenantId);
		assertEquals(200, response.getStatus());
		verify(tenantMgr).unProvisionTenant(tenantId);
	}
}
