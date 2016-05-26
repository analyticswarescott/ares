package com.aw.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import com.aw.common.hadoop.structure.HadoopPurpose;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.write.FileWriter;

/**
 * Integration test for DG resource
 *
 * @author jlehmann
 *
 */
public class DGResourceTest extends BaseIntegrationTest {

	@Test
	public void integrationTest() throws Exception {

    	provisionTenant("1");

    	//test status
    	//testStatus();

    	addBundle();

		addEDRScan();

		addEDRScanPartial();

	}



    public void addBundle() throws Exception {

    	//set up a tenant and user
    	String username = UUID.randomUUID().toString();
    	addUser(username, "1");

    	//add a bundle with some bogus data
    	String data = "{\"name\":\"bogus bundle data\"}";
    	HttpResponse response = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/aw/1/test_machine_1/bundles/test_bundle_1", new JSONObject(data), HttpResponse.class);
    	assertEquals("bundle post failed", HttpStatus.ACCEPTED_202, response.getStatusLine().getStatusCode());

    	//see if we get the file
    	setThreadSecurity("1", username, username);
    	FileWrapper wrapper = TestDependencies.getPlatformMgr().get().getTenantFileReader().read(HadoopPurpose.BUNDLE, new Path("/test_machine_1"), "test_bundle_1");
    	assertNotNull("bundle data missing after adding it", wrapper);
    	assertEquals("bundle data wrong", data, IOUtils.toString(wrapper.getInputStream()));

    }

    public void addEDRScan() throws Exception {

    	//set up a tenant and user
    	String username = UUID.randomUUID().toString();
    	addUser(username, "1");

    	//add an edr scan with some bogus data
    	String data = "{\"name\":\"bogus edr data\"}";
    	HttpResponse response = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/aw/1/test_machine_1/edr_scans/my_scan", new JSONObject(data), org.apache.http.HttpResponse.class);
    	assertEquals("bundle post failed", HttpStatus.ACCEPTED_202, response.getStatusLine().getStatusCode());

    	//see if we get the file
    	setThreadSecurity("1", username, username);
    	FileWrapper wrapper = TestDependencies.getPlatformMgr().get().getTenantFileReader().read(HadoopPurpose.EDR, new Path("/test_machine_1"), "my_scan");
    	assertNotNull("EDR scan missing after adding it", wrapper);
    	assertEquals("edr data wrong", data, IOUtils.toString(wrapper.getInputStream()));

    }

    public void addEDRScanPartial() throws Exception {

    	//set up a tenant and user
    	String username = UUID.randomUUID().toString();
    	addUser(username, "1");

    	//add an edr scan with some bogus data
    	String data = "bogus edr data";

    	//post 3 chunks
    	for (int x=1; x<=3; x++) {

	    	HttpResponse response = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/aw/1/test_machine_1/edr_scans/my_scan_partial/" + x, data + x, org.apache.http.HttpResponse.class);
	    	assertEquals("bundle post failed", HttpStatus.ACCEPTED_202, response.getStatusLine().getStatusCode());

    	}

    	//see if we get the file parts
    	for (int x=1; x<=3; x++) {

	    	setThreadSecurity("1", username, username);
	    	FileWrapper wrapper = TestDependencies.getPlatformMgr().get().getTenantFileReader().read(HadoopPurpose.EDR, new Path("/test_machine_1"), FileWriter.toPartName("my_scan_partial", x));
	    	assertNotNull("EDR scan part " + x + " missing", wrapper);
	    	assertEquals("edr data wrong", data + x, IOUtils.toString(wrapper.getInputStream()));

    	}

    }

}
