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
public class DGResourceTest {
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
	public void testAddBundle() throws Exception {
		String tenantId = "I'm a tenant!";
		String machineId = "I'm a machine!";
		String bundleId = "I'm a bundle!";
		InputStream mockInputStream = mock(InputStream.class);
		Response response = aresResource.addBundle(tenantId, machineId, bundleId, mockInputStream);
		assertEquals(202, response.getStatus());
		verify(platformMgr).addFile(HadoopPurpose.BUNDLE, Topic.BUNDLE_REF, new Tenant(tenantId), machineId, bundleId, bundleId, mockInputStream);
	}

	@Test
	public void testAddEdrScan() throws Exception {
		String tenantId = "I'm a tenant!";
		String machineId = "I'm a machine!";
		String scanId = "I'm a scan!";
		InputStream mockInputStream = mock(InputStream.class);
		Response response = aresResource.addEDRScan(tenantId, machineId, scanId, mockInputStream);
		assertEquals(202, response.getStatus());
		verify(platformMgr).addFile(HadoopPurpose.EDR, Topic.SCAN_REF, new Tenant(tenantId), machineId, scanId, scanId, mockInputStream);
	}

	@Test
	public void testAddEdrScanWithSequenceNumber() throws Exception {
		String tenantId = "I'm a tenant!";
		String machineId = "I'm a machine!";
		String scanId = "I'm a scan!";
		int sequenceId = 777;
		InputStream mockInputStream = mock(InputStream.class);
		Response response = aresResource.addEDRScan(tenantId, machineId, scanId, sequenceId, mockInputStream);
		assertEquals(202, response.getStatus());
		verify(platformMgr).addFilePart(HadoopPurpose.EDR, new Tenant(tenantId), machineId, scanId, scanId, mockInputStream, sequenceId);
	}

	@Test
	public void testCompleteScan() throws Exception {
		String tenantId = "I'm still a tenant!";
		String machineId = "I'm still a machine!";
		String scanId = "I'm still a scan!";
		Response response = aresResource.completeScan(tenantId, machineId, scanId);
		assertEquals(202, response.getStatus());
		verify(platformMgr).completeFile(HadoopPurpose.EDR, Topic.SCAN_REF, new Tenant(tenantId), machineId, scanId, scanId);
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
