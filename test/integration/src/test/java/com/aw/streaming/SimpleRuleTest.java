/*
package com.aw.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aw.TestDependencies;
import com.aw.common.Tag;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.VersionedObject;
import com.aw.common.util.es.ElasticIndex;
import com.aw.incident.Incident;
import com.aw.incident.action.AlarmAssociationAction;
import com.aw.incident.action.AssociateIncidentAction;
import com.aw.incident.action.EventAssociationAction;
import com.aw.incident.action.IncidentActionType;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.UnityRunner.ResponseFormat;
import com.aw.unity.UnityUtil;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.dg.CommonField;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;

public class SimpleRuleTest extends StreamingIntegrationTest {

    */
/**
     * Create an incident, assign it, and close it
     *
     * @throws Exception
     *//*

    @Test
    public void incidentCreationAndActions() throws Exception {

    	//set up a tenant and user
    	provisionTenant("1");
    	String username = UUID.randomUUID().toString();
    	addUser(username, "1");

    	setThreadSystemAccess();
    	setThreadSecurity("1", username, username);

    	//send events through
    	DataFeedUtils.fireBundles(TestDependencies.getPlatform().get(), "1", "bundle_sample_rule.json");

    	//wait for our incident
    	DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident", 1, 180);

    	DataType incidentType = TestDependencies.getUnity().get().getDataType("incident");
    	DataType associateIncidentType = TestDependencies.getUnity().get().getDataType("incident_association");

    	//check the incident by getting all incidents from the db - should just be one
    	Query query = UnityUtil.getDetailQueryFor(incidentType, new FilterGroup());
    	List<VersionedObject<Incident>> incidents = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), query, 60000L, Incident.class).getPojos();

    	assertEquals("there should only be one incident", 1, incidents.size());

    	Incident incident = incidents.get(0).get();

    	assertEquals("incident name wrong", "sample rule: nvidia stuff detected", incident.getName());

    	//wait for the other actions to show up - this tests modifying an incident
    	DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident_comment", 1, 180);
    	DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident_set_state", 1, 180);
    	DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident_association", 2, 300);

    	//get the incident, make sure it has the comment
    	VersionedObject<Incident> versionedIncident = UnityUtil.getPojo(TestDependencies.getUnity().get(), incidentType, 60000L, incident.getGuid().toString(), Incident.class);
    	incident = versionedIncident.get();

    	assertEquals(2L, versionedIncident.getVersion());
    	assertEquals(3, incident.getTags().size());
    	assertTrue(incident.getTags().contains(Tag.valueOf("nvidia")));

    	assertEquals("nvidia stuff was detected - that man is playing galaga dg_pn=adobe-nvidia-virus.exe", incident.getDescription());

    	//get the association and make sure it's correct
    	query = UnityUtil.getDetailQueryFor(associateIncidentType, new FilterGroup());
    	List<VersionedObject<AssociateIncidentAction>> actions = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), query, 60000L, AssociateIncidentAction.class).getPojos();
    	assertEquals(2, actions.size());

    	//check the alarm action
    	AlarmAssociationAction alarmAction = (AlarmAssociationAction)actions.stream().filter(v -> v.get() instanceof AlarmAssociationAction).collect(Collectors.toList()).get(0).get();
    	assertEquals("associated id list wrong size", 1, alarmAction.getAssociatedIds().size());
    	assertNotEquals("association event id not set properly", AlarmAssociationAction.VAR_ALARM_GUID, alarmAction.getAssociatedIds().get(0));
    	assertNotNull("association event id not set properly", alarmAction.getAssociatedIds().get(0));
    	assertEquals("association event type not set properly", IncidentActionType.ALARM_ASSOCIATION.name().toLowerCase(), alarmAction.getAssociationType());
    	assertNotEquals("association incident id not set properly", EventAssociationAction.VAR_INCIDENT_GUID, alarmAction.getIncidentGuid());
    	assertNotNull("association incident id not set properly", alarmAction.getIncidentGuid());
    	assertEquals("associated incident guid wrong", alarmAction.getIncidentGuid(), incident.getGuid().toString());

    	//check the event action
    	EventAssociationAction eventAction = (EventAssociationAction)actions.stream().filter(v -> v.get() instanceof EventAssociationAction).collect(Collectors.toList()).get(0).get();
    	assertEquals("associated id list wrong size", 1, eventAction.getAssociatedIds().size());
    	assertNotEquals("association event id not set properly", EventAssociationAction.VAR_EVENT_GUID, eventAction.getAssociatedIds().get(0));
    	assertNotNull("association event id not set properly", eventAction.getAssociatedIds().get(0));
    	assertEquals("association event type not set properly", IncidentActionType.EVENT_ASSOCIATION.name().toLowerCase(), eventAction.getAssociationType());
    	assertNotEquals("association incident id not set properly", EventAssociationAction.VAR_INCIDENT_GUID, eventAction.getIncidentGuid());
    	assertNotNull("association incident id not set properly", eventAction.getIncidentGuid());
    	assertEquals("associated incident guid wrong", eventAction.getIncidentGuid(), incident.getGuid().toString());

    	//get the event that was associated with the incident, making sure it's there

    	//get alarm created from the simple rule
    	Query alarmQuery = UnityUtil.getDetailQueryFor(null, new FilterGroup());
    	alarmQuery.setContext("alarms");
    	alarmQuery.setDataTypes(TestDependencies.getUnity().get().getMetadata().getDataTypeRepository().getDataTypes());
    	DataUnityResults results = UnityUtil.executeBlocking(TestDependencies.getUnity().get(), alarmQuery, 60000L, ResponseFormat.UNITY_DATA);

    	assertEquals(1, results.getData().size());

    	//verify some alarm fields
    	Data data = results.getData().get(0);
    	assertEquals("[\"tag1\",\"tag2\"]", data.getValue(CommonField.DG_TAGS.asField()));
    	assertEquals("A sample rule", data.getValue(CommonField.DG_DET_NAME.asField()));

    	//finally, check that the alarm association points to this alarm
    	assertEquals(data.getValue(CommonField.DG_ALARM_ID.asField()), alarmAction.getAssociatedIds().get(0));

    }

}
*/
