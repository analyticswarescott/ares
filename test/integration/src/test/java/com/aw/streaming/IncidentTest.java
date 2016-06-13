/*
package com.aw.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.auth.DefaultUser;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.VersionedObject;
import com.aw.common.util.es.ElasticIndex;
import com.aw.incident.DefaultIncident;
import com.aw.incident.Incident;
import com.aw.incident.Incident.Severity;
import com.aw.incident.Incident.State;
import com.aw.incident.action.AssociateIncidentAction;
import com.aw.incident.action.CommentIncidentAction;
import com.aw.incident.action.EventAssociationAction;
import com.aw.incident.action.SetStateIncidentAction;
import com.aw.platform.NodeRole;
import com.aw.platform.exceptions.PlatformInitializationException;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.UnityRunner.ResponseFormat;
import com.aw.unity.UnityUtil;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.es.UnityESClient;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;

public class IncidentTest extends StreamingIntegrationTest {

    */
/**
{
  "inc_assoc_id" : [ "1", "2", "3" ],
  "inc_assoc_type" : "event_association",
  "inc_guid" : "123abc",
  "dg_tags" : [ "tag1", "tag2" ],
  "inc_comment" : "test comment"
}
     *//*

    private static final String ASSOCIATE_ACTION_JSON =
    		"{\n" +
    		"  \"inc_assoc_id\" : [ \"1\", \"2\", \"3\" ],\n" +
    		"  \"inc_assoc_type\" : \"event_association\",\n" +
    		"  \"inc_guid\" : \"123abc\",\n" +
    		"  \"dg_tags\" : [ \"tag1\", \"tag2\" ],\n" +
    		"  \"inc_comment\" : \"test comment\"\n" +
    		"}";

    */
/**
     * Create an incident, assign it, and close it
     *
     * @throws Exception
     *//*

    @Test
    public void integrationTest() throws Exception {
    	testIncidentCreationAndActions();
    	testStreamingErrorIncident();
    }

    public void testIncidentCreationAndActions() throws Exception {

    	//set up a tenant and user
    	provisionTenant("1");
    	String username = UUID.randomUUID().toString();
    	addUser(username, "1");

    	setThreadSystemAccess();
    	setThreadSecurity("1", username, username);

    	//get the incident data type for later
    	DataType dataType = TestDependencies.getUnity().get().getMetadata().getDataType("incident");

    	//create a new incident
    	DefaultIncident incident = new DefaultIncident();
    	incident.setName("my test incident");
    	incident.setSeverity(Severity.CRITICAL);
    	incident.setAssignee(new DefaultUser("test_user"));
    	incident.setDescription("incident description");

    	HttpResponse response = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/incidents", new JSONObject(JSONUtils.objectToString(incident)), org.apache.http.HttpResponse.class);
    	assertTrue("incident post failed", HttpStatus.isSuccess(response.getStatusLine().getStatusCode()));
    	String strResponse = IOUtils.toString(response.getEntity().getContent());

    	//wait for the incident in the system
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident", 1, 180);

    	//get the incident from the system and verify some things
    	Query incidentQuery = UnityUtil.getDetailQueryFor(dataType, new FilterGroup());
    	DataUnityResults results = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), incidentQuery, 60000L, ResponseFormat.UNITY_DATA);
    	assertEquals("incident list should have 1 incident in it", 1, results.getData().size());

    	Data incidentData = results.getData().get(0);
    	DataType type = incidentData.getType();
    	assertEquals("incident name wrong", "my test incident", incidentData.getValue(type.getField("dg_name")));
    	String guid = (String)incidentData.getValue(type.getField("dg_guid"));
    	UUID uuid = UUID.fromString(guid);

    	//assign the incident
//    	ActionManagerProvider.get().getActionManager().takeAction(new AssignIncidentAction(uuid, new DefaultUser("test_user_2")));
    	response = authPut(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid + "/assignee", "\"test_user_2\"", org.apache.http.HttpResponse.class);
    	assertTrue("assignment failed", HttpStatus.isSuccess(response.getStatusLine().getStatusCode()));

    	//wait for standard refresh in es
    	Thread.sleep(1500L);

    	//get the incident again and verify
    	results = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), incidentQuery, 60000L, ResponseFormat.UNITY_DATA);
    	assertEquals("incident list should have 1 incident in it", 1, results.getData().size());
    	Data incidentData2 = results.getData().get(0);

    	assertNotEquals("incident should have changed user after being assigned", incidentData.getValue(type.getField("inc_assign")), incidentData2.getValue(type.getField("inc_assign")));
    	assertEquals("incident should have changed user after being assigned", "test_user_2", incidentData2.getValue(type.getField("inc_assign")));

    	//add a comment to the incident

    	//check the incident comments
    	setThreadSecurity("1", username, username);
    	TestDependencies.getActionManager().get().takeAction(new CommentIncidentAction(uuid, "this is a test comment"));

    	//add restfully too

    	//wait for es refresh
    	Thread.sleep(1500L);

    	//get our incident again
    	Incident incident3 = UnityUtil.getPojo(TestDependencies.getUnity().get(), type, 60000L, guid, Incident.class).get();

    	//close the incident
    	TestDependencies.getActionManager().get().takeAction(new SetStateIncidentAction(uuid, State.CLOSED));

    	//wait for es refresh
    	Thread.sleep(1500L);

    	//get the incident again and verify
    	results = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), incidentQuery, 60000L, ResponseFormat.UNITY_DATA);
    	assertEquals("incident list should have 1 incident in it", 1, results.getData().size());
    	Incident incident4 = UnityUtil.getPojo(TestDependencies.getUnity().get(), type, 60000L, guid, Incident.class).get();

    	assertEquals("incident should be closed after set state action", State.CLOSED, incident4.getState());

    	//wait for incident actions in the db
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident_create", 1, 180);
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident_assign", 1, 180);
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident_set_state", 1, 180);
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident_comment", 1, 180);

    	//update the incident and make sure it was updated

    	//this
    	DefaultIncident incident5 = new DefaultIncident();
    	incident5.setGuid(incident4.getGuid());
    	incident5.setCreationTime(null);
    	incident5.setAssignee(null);
    	incident5.setDescription("blah 123");
    	String incidentJson = JSONUtils.objectToString(incident5);
    	response = authPut(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid, incidentJson, HttpResponse.class);
    	assertTrue(response.getStatusLine().toString(), HttpStatus.isSuccess(response.getStatusLine().getStatusCode()));

    	//wait for es refresh
    	Thread.sleep(1500L);

    	//now make sure the incident has been updated
    	Incident incident6 = UnityUtil.getPojo(TestDependencies.getUnity().get(), type, 60000L, incident5.getGuid().toString(), Incident.class).get();
    	assertEquals("blah 123", incident6.getDescription());

    	//do the association
    	response = authPost(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid + "/associations", ASSOCIATE_ACTION_JSON, HttpResponse.class);
    	assertTrue(response.getStatusLine().toString(), HttpStatus.isSuccess(response.getStatusLine().getStatusCode()));

    	//wait for action in the db
    	DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("1"), "incident_association", 1, 180);

    	//get the action
    	DataType actionType = TestDependencies.getUnity().get().getMetadata().getDataType("incident_association");
    	Query query = UnityUtil.getDetailQueryFor(actionType, new FilterGroup());
    	PojoUnityResults<EventAssociationAction> associateResults = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), query, 60000L, EventAssociationAction.class);
    	assertTrue("missing association in elasticsearch", associateResults.getPojos().size() > 0);
    	VersionedObject<EventAssociationAction> versionedAction = associateResults.getPojos().get(0);
    	AssociateIncidentAction action = versionedAction.get();
    	assertNotNull("no action retrieved for " + action.getGuid(), versionedAction);
    	assertEquals("association not tied to correct incident", guid, action.getIncidentGuid());
    	assertEquals("wrong number of associated ids", 3, action.getAssociatedIds().size());
    	assertEquals("wrong associated ids", "1", action.getAssociatedIds().get(0));
    	assertEquals("wrong associated ids", "2", action.getAssociatedIds().get(1));
    	assertEquals("wrong associated ids", "3", action.getAssociatedIds().get(2));
    	assertEquals("wrong number of association tags", 2, action.getTags().size());
    	assertTrue("missing association tag", action.getTags().contains(Tag.valueOf("tag1")));
    	assertTrue("missing association tag", action.getTags().contains(Tag.valueOf("tag2")));

    }

    public void testStreamingErrorIncident() throws Exception {

		//add an error
		TestDependencies.getPlatformMgr().get().handleException(new PlatformInitializationException("this is a test"), NodeRole.SPARK_WORKER, NodeRole.ELASTICSEARCH);

		setThreadSystemAccess();

		UnityESClient client = new UnityESClient(TestDependencies.getPlatform().get());
		while (!client.indexExists(ESKnownIndices.ERRORS, Instant.now())) {
			//wait a second for the index
			Thread.sleep(1000L);
		}

		//now wait for it
		DataFeedUtils.awaitESResult(ESKnownIndices.ERRORS, Tenant.forId("0"), "platform_error", -1, 180);
		DataFeedUtils.awaitESResult(ESKnownIndices.INCIDENTS, Tenant.forId("0"), "incident", 1, 180);

		Query query = UnityUtil.getDetailQueryFor(TestDependencies.getUnity().get().getDataType(Incident.UNITY_TYPE), new FilterGroup());
		PojoUnityResults<Incident> results = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), query, 60000L, Incident.class);
		assertEquals(1, results.getPojos().size());
		Incident incident = results.getPojos().get(0).get();

		//make sure the incident has the error message in the name - the rule is configured this way
		assertTrue(incident.getDescription().contains("this is a test"));

	}

}
*/
