package com.aw.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.document.DocumentType;
import org.apache.commons.httpclient.util.ExceptionUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.Path;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;

public class TenantProvisionTest extends BaseIntegrationTest {

	@Test
	public void test() throws Exception {
		testStartupAlreadyProvisioned();
		testProvisionedTenantIDs();

		testUnprovision();


	}

	public void testUnprovision() throws Exception {
		provisionTenant("5");

		Impersonation.impersonateTenant("5");

		try {
			DefaultZkAccessor zk = new DefaultZkAccessor(TestDependencies.getPlatform().get(), Hive.TENANT);
			//DefaultZkAccessor zk = TestDependencies.getTaskService().get().getTenantZkAccessor();


			FileWriter w = TestDependencies.getPlatformMgr().get().getTenantFileWriter();

			//ensure we can write to path-ed resources
			zk.put(ZkPurpose.OFFSETS, "before", "test");
			w.writeStringToFile(HadoopPurpose.RAW, new Path("/test"), "test", "test");

			unProvisionTenant("5");

			//expect errors trying write operations to this tenant
			try {
				zk.put(ZkPurpose.OFFSETS, "after", "test");
				Assert.fail("should have received exception");
			}
			catch (Exception ex) {
				if (!ExceptionUtils.getStackTrace(ex).contains("tenant root does not exist")) {
					org.junit.Assert.fail("unexpected Exception " + ex.getMessage());
				}
			}
			try {
				w.writeStringToFile(HadoopPurpose.RAW, new Path("/test"), "test", "test");
				Assert.fail("should have received exception");
			}
			catch (Exception ex) {
				if (!ExceptionUtils.getStackTrace(ex).contains("tenant root does not exist")) {
					org.junit.Assert.fail("unexpected Exception " + ex.getMessage());
				}
			}

		}
		finally {
			Impersonation.unImpersonate();
		}

	}

    public void testStartupAlreadyProvisioned() throws Exception {
        // startup and create 2 tenants
        provisionTenant("1");
        provisionTenant("2");

        TestDependencies.getPlatformMgr().get().reset();
        resetDocuments();
        initDocuments();

        // confirm that the 2 tenants still exist
        CloseableHttpResponse response = client().execute(get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/dev/tenant/ids"));
        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONArray provisionedIDs = (JSONArray) getJSONEntity(response);

        assertTrue(contains(provisionedIDs, "1"));
        assertTrue(contains(provisionedIDs, "2"));

    }

    public void testProvisionedTenantIDs() throws Exception {

        //String s = EnvironmentSettings.getConfDirectory();

        provisionTenant("11");
        provisionTenant("12");
        provisionTenant("14");
        provisionTenant("15");

        CloseableHttpResponse response = client().execute(get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/dev/tenant/ids"));
        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONArray provisionedIDs = (JSONArray) getJSONEntity(response);
        assertTrue(contains(provisionedIDs, "11"));
        assertTrue(contains(provisionedIDs, "12"));
        assertFalse(contains(provisionedIDs, "13"));
        assertTrue(contains(provisionedIDs, "14"));
        assertTrue(contains(provisionedIDs, "15"));


/*        //test removal of a tenant
        unProvisionTenant("11");


        response = client().execute(get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/dev/tenant/ids"));
        assertEquals(200, response.getStatusLine().getStatusCode());

         provisionedIDs = (JSONArray) getJSONEntity(response);
        assertFalse(contains(provisionedIDs, "11"));
        assertTrue(contains(provisionedIDs, "12"));
        assertFalse(contains(provisionedIDs, "13"));
        assertTrue(contains(provisionedIDs, "14"));
        assertTrue(contains(provisionedIDs, "15"));*/

    }


}
