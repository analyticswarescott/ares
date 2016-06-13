/*
package com.aw.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.TestDependencies;
import com.aw.TestDocumentHandler;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.es.ElasticIndex;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.document.DocumentType;
import com.aw.platform.DefaultPlatform;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.platform.roles.Rest;
import com.aw.streaming.DataFeedUtils;
import com.aw.streaming.StreamingIntegrationTest;
import com.aw.streaming.StreamingWork;

public class NodeInstallationTest extends StreamingIntegrationTest {

    private static String PLATFORM_LOCAL_PARTIAL =
    		"{\n" +
    		"    \"type\" : \"platform\",\n" +
    		"    \"name\" : \"local\",\n" +
    		"    \"display_name\": \"local platform\",\n" +
    		"    \"description\": \"local platform\",\n" +
    		"    \"author\": \"aw\",\n" +
    		"    \"body_class\" : \"com.aw.platform.DefaultPlatform\",\n" +
    		"    \"body\": {\n" +
    		"        \"nodes\": {\n" +
    		"            \"localhost\": {\n" +
    		"                \"elasticsearch\": {\n" +
    		"                    \"cluster_name\" : \"aw\",\n" +
    		"                    \"es_transport_port\": 9300,\n" +
    		"                    \"port\": 9200\n" +
    		"                }},\n" +
    		"            \"127.0.0.1\" : {\n" +
    		"                \"kafka\": {\n" +
    		"                    \"port\": 9092,\n" +
    		"                    \"configuration\": {\n" +
    		"                        \"num.network.threads\": 4\n" +
    		"                    }\n" +
    		"                },\n" +
    		"                \"hdfs_name\": {\n" +
    		"                    \"port\": 9000,\n" +
    		"                    \"hdfs_root_path\": \"/\",\n" +
    		"                    \"ha_cluster_name\": \"aw\"\n" +
    		"                },\n" +
    		"                \"hdfs_data\": {\n" +
    		"                    \"data_dir\": \"/opt/aw/data/hdfs/data\"\n" +
    		"                },\n" +
    		"                \"hdfs_journal\": {\n" +
    		"                    \"port\": 8485,\n" +
    		"                    \"edits_dir\": \"/opt/aw/data/hdfs/journal\"\n" +
    		"                },\n" +
    		"                \"zookeeper\": {\n" +
    		"                    \"port\": 2181,\n" +
    		"                    \"configuration\": {\n" +
    		"                        \"maxClientCnxns\": 70\n" +
    		"                    },\n" +
    		"                    \"server_id\": 1,\n" +
    		"                    \"peer_port\": 2888,\n" +
    		"                    \"leader_elect_port\": 3888,\n" +
    		"                    \"log4j_overrides\": {\n" +
    		"                    }\n" +
    		"                },\n" +
    		"                \"spark_master\": {\n" +
    		"                    \"spark_master_port\": \"7077\",\n" +
    		"                    \"spark_rest_port\": \"6066\",\n" +
    		"                    \"spark_master_ui_port\": \"8888\",\n" +
    		"                    \"log4j_overrides\": {\n" +
    		"                        \"log4j.logger.com.aw.compute\": \"DEBUG\",\n" +
    		"                        \"log4j.logger.com.aw.compute.streams.processor.framework.ProcessorFunction\": \"DEBUG\"\n" +
    		"                    }\n" +
    		"                }\n" +
    		"            }\n" +
    		"        }\n" +
    		"    }\n" +
    		"}";

	@Override
    protected boolean usesElasticsearch() {
        return false;
    }

	@Override
    protected boolean usesKafka() {
        return false;
    }

	@Override
    protected boolean startsRest() {
        return false;
    }

	@Override
    protected boolean startsSpark() {
        return false;
    }

    @Test
    public void startOnlyRestService() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("serviceSharedSecret", "sharedsecret");

        //kafka has to start first, we have no way to start ZK in an integration test otherwise
        startKafka();

        // start the rest service explicitly
        startRest();

		//Explicitly stop ES to test tenant provisioning failure
		stopElasticsearch();

        // assert that the platform has initialized with the single node profile
        {
            JSONObject currentPlatformSpec = (JSONObject) getJSONEntity(restService.get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/admin/platform", null, headers));
            assertNotNull(currentPlatformSpec);
            JSONObject nodes = currentPlatformSpec.getJSONObject("nodes");
            assertEquals(1, nodes.length() );
            assertEquals("localhost", nodes.keys().next() );
            assertEquals(12, nodes.getJSONObject("localhost").length() );

        }

        // call the tenant platform endpoint with an updated node spec -- 2 nodes this time

        JSONObject updatedPlatformSpec = new JSONObject(PLATFORM_LOCAL_PARTIAL);

        HttpResponse put;

        //post DEFAULT platform doc to the wrapper, as it has loaded LOCAL by default
        Document doc = new TestDocumentHandler().getDocument(DocumentType.PLATFORM, Platform.DEFAULT);
        DefaultPlatform platform = doc.getBodyAsObject((DocumentHandler)null);
        platform.getNode(NodeRole.REST).getSettings(NodeRole.REST).put(Rest.PORT, 9099);
        doc.setName(Platform.LOCAL);
        doc.setBody(new JSONObject(JSONUtils.objectToString(platform)));

        setThreadSystemAccess();
        DocumentHandler restie = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, TestDependencies.getPlatform().get());
        restie.updateDocument(doc);

        TestDependencies.getDocMgr().get().getSysDocHandler().updateDocument(doc);

        TestDependencies.getPlatformMgr().get().setPlatform(null);


 */
/*        put = restService.put(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/admin/platform", doc.toJSON().toString(), null, headers);
        assertEquals(200, put.getStatusLine().getStatusCode());*//*


        assertNotEquals("platform should not have started without es and spark", PlatformState.RUNNING, TestDependencies.getRestCluster().get().getPlatformState());

        if (TestDependencies.getRestCluster().get().getPlatformState() != PlatformController.PlatformState.RUNNING) {

        	//allow time to see failures
            startSpark();

            //start ES
            startElasticSearch();

        }

        long start = System.currentTimeMillis();
        while (TestDependencies.getRestCluster().get().getPlatformState() != PlatformController.PlatformState.RUNNING) {

        	//don't wait forever
        	assertTrue("maximum wait time exceeded for platform RUNNING state", System.currentTimeMillis() - start < 20000L);

            Thread.sleep(100); //set this long to see spark IO

        }

         //fire the work again
        StreamingWork.fireDLPWork(TestDependencies.getPlatform().get(), TestDependencies.getRestMember().get());

		//Thread.sleep(15000);


		DataFeedUtils.awaitESResult(ESKnownIndices.EVENTS, Tenant.forId("1"), "user_cd_burn", 44, 60);
		DataFeedUtils.awaitESResult(ESKnownIndices.EVENTS, Tenant.forId("1"), "user_net_transfer_download", 23, 60);
		DataFeedUtils.awaitESResult(ESKnownIndices.EVENTS, Tenant.forId("2"), "user_cd_burn", 44, 60);
		DataFeedUtils.awaitESResult(ESKnownIndices.EVENTS, Tenant.forId("2"), "user_net_transfer_download", 23, 60);


    }
}
*/
