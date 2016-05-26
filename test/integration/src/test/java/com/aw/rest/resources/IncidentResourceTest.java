package com.aw.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import com.aw.incident.*;
import com.aw.incident.action.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.auth.DefaultUser;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.es.ElasticIndex;
import com.aw.incident.CreationType;
import com.aw.incident.DefaultIncident;
import com.aw.incident.Incident;
import com.aw.streaming.DataFeedUtils;
import com.aw.unity.dg.CommonField;
import com.aw.util.Statics;

/**
 * Tests the resource handlers for the {@link IncidentResource}
 */
public class IncidentResourceTest extends BaseIntegrationTest {

	private String testUser;
	private IncidentResource incidentResource;
	private List<String> insertedGuids = new ArrayList<>();

	@Override
	protected boolean usesSpark() {
		return true;
	}

	@Override
	protected boolean startsSpark() {
		return true;
	}

	@Test
	public void testGetIncidents() throws Exception {

		incidentResource = new IncidentResource(TestDependencies.getActionManager().get(), new UnityIncidentManager(TestDependencies.getUnity()));

		provisionTenant("1");
		testUser = "test-0";
		setThreadSecurity("1", "1", testUser);
		addUser(testUser, "1");

		// No incidents yet to grab, should be empty
		assertTrue(incidentResource.getIncidents(IncidentSort.HIGH_SEVERITY_FIRST, false).isEmpty());

		// First, insert some INFORMATIONAL incidents
		for (int i = 0; i < 3; i++) {
			DefaultIncident incident = new DefaultIncident();
			incident.setCreationType(CreationType.MANUAL);
			incident.setDescription("test" + i);
			incident.setName("test" + i);
			incident.setAssignee(new DefaultUser(testUser));
			incident.setSeverity(Incident.Severity.INFORMATIONAL);
			insertedGuids.add(createIncident(incident));
		}

		// Next, insert some critical incidents
		for (int i = 0; i < 5; i++) {
			DefaultIncident incident = new DefaultIncident();
			incident.setCreationType(CreationType.MANUAL);
			incident.setDescription("test" + i);
			incident.setName("test" + i);
			incident.setAssignee(new DefaultUser(testUser));
			incident.setSeverity(Incident.Severity.CRITICAL);
			insertedGuids.add(createIncident(incident));
		}

		// Insert more INFORMATIONAL incidents after the block of critical ones - this is to verify our sorting procedure
		for (int i = 0; i < 3; i++) {
			DefaultIncident incident = new DefaultIncident();
			incident.setCreationType(CreationType.MANUAL);
			incident.setDescription("test" + i);
			incident.setName("test" + i);
			incident.setAssignee(new DefaultUser(testUser));
			incident.setSeverity(Incident.Severity.INFORMATIONAL);
			insertedGuids.add(createIncident(incident));
		}

		// Just for fun, add some closed incidents - these shouldn't be included in the results
		for (int i = 0; i < 5; i++) {
			DefaultIncident incident = new DefaultIncident();
			incident.setCreationType(CreationType.MANUAL);
			incident.setDescription("test" + i);
			incident.setName("test" + i);
			incident.setAssignee(new DefaultUser(testUser));
			incident.setSeverity(Incident.Severity.HIGH);
			incident.setState(Incident.State.CLOSED);
			insertedGuids.add(createIncident(incident));
		}

		//wait for the incidents in the system
		DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident", 16, 180);

		final List<Incident> incidents = new ArrayList<>(incidentResource.getIncidents(IncidentSort.HIGH_SEVERITY_FIRST, false));
		// all incidents except the closed ones
		assertEquals(11, incidents.size());

		// Make sure that we've sorted the incidents correctly
		long prevMaxTime = Long.MAX_VALUE;
		for (int i = 0; i < 5; i++) {
			final Incident incident = incidents.get(i);
			assertTrue(prevMaxTime > incident.getCreationTime().getTime());
			prevMaxTime = incident.getCreationTime().getTime();
			assertEquals(Incident.Severity.CRITICAL, incident.getSeverity());
		}
		prevMaxTime = Long.MAX_VALUE;
		for (int i = 5; i < 11; i++) {
			final Incident incident = incidents.get(i);
			assertTrue(prevMaxTime > incident.getCreationTime().getTime());
			prevMaxTime = incident.getCreationTime().getTime();
			assertEquals(Incident.Severity.INFORMATIONAL, incident.getSeverity());
		}

		final List<Incident> incidents2 = incidentResource.getIncidents(IncidentSort.LOW_SEVERITY_FIRST, true);

		// all incidents
		assertEquals(16, incidents2.size());

		// Make sure that we've sorted the incidents correctly

		long prevMinTime = 0;
		for (int i = 0; i < 6; i++) {
			final Incident incident = incidents2.get(i);
			assertTrue(prevMinTime < incident.getCreationTime().getTime());
			prevMinTime = incident.getCreationTime().getTime();
			assertEquals(Incident.Severity.INFORMATIONAL, incident.getSeverity());
		}
		prevMinTime = 0;
		for (int i = 6; i < 11; i++) {
			final Incident incident = incidents2.get(i);
			assertTrue(prevMinTime < incident.getCreationTime().getTime());
			prevMinTime = incident.getCreationTime().getTime();
			assertEquals(Incident.Severity.HIGH, incident.getSeverity());
		}
		prevMinTime = 0;
		for (int i = 11; i < 16; i++) {
			final Incident incident = incidents2.get(i);
			assertTrue(prevMinTime < incident.getCreationTime().getTime());
			prevMinTime = incident.getCreationTime().getTime();
			assertEquals(Incident.Severity.CRITICAL, incident.getSeverity());
		}

		testApiResponse();
		testActions();
		testSearch();
	}

	private void testApiResponse() throws Exception {
		DefaultIncident incident = new DefaultIncident();
		incident.setCreationType(CreationType.MANUAL);
		incident.setDescription("test");
		incident.setName("test");
		incident.setAssignee(new DefaultUser(testUser));
		incident.setSeverity(Incident.Severity.HIGH);
		incident.setState(Incident.State.CLOSED);

		JSONObject object = new JSONObject(JSONUtils.objectToString(incident));
		object.put("inc_sev", "fredsev");
		HttpResponse response = authPost(testUser, Statics.VERSIONED_REST_PREFIX + "/incidents", object, HttpResponse.class);
		assertEquals(400, response.getStatusLine().getStatusCode());

		object = new JSONObject(JSONUtils.objectToString(incident));
		object.put("inc_state", "fredstate");
		response = authPost(testUser, Statics.VERSIONED_REST_PREFIX + "/incidents", object, HttpResponse.class);
		assertEquals(400, response.getStatusLine().getStatusCode());

		object = new JSONObject(JSONUtils.objectToString(incident));
		object.put("dg_guid", "fredguid");
		response = authPost(testUser, Statics.VERSIONED_REST_PREFIX + "/incidents", object, HttpResponse.class);
		assertEquals(400, response.getStatusLine().getStatusCode());
	}

	private void testActions() throws Exception {
		updateIncident(insertedGuids.get(0));
		addAssociation(insertedGuids.get(0));
		assignIncident(insertedGuids.get(0));
		assignIncident(insertedGuids.get(1));
		changeState(insertedGuids.get(0));
		addComment(insertedGuids.get(0));
		addAndReadAttachment(insertedGuids.get(0));
		readTimeline(insertedGuids.get(0));
	}

	private void updateIncident(String guid) throws Exception {
		DefaultIncident incident = new DefaultIncident();
		incident.setCreationType(CreationType.MANUAL);
		incident.setDescription("testActions");
		incident.setName("testActions");
		incident.setAssignee(new DefaultUser(testUser));
		incident.setSeverity(Incident.Severity.CRITICAL);
		insertedGuids.add(createIncident(incident));
		incident.setName("more Different");
		incidentResource.updateIncident(guid, incident);
	}

	private void addAssociation(String guid1) throws Exception {
		List<String> associatedStrings = new ArrayList<>();
		associatedStrings.add(insertedGuids.get(2));
		associatedStrings.add(insertedGuids.get(3));
		associatedStrings.add(insertedGuids.get(4));
		Thread.sleep(1000);
		AssociateIncidentAction action  = new EventAssociationAction();
		action.setIncidentGuid(guid1);
		action.setAssociatedIds(associatedStrings);
		action.setComment("please work test...?");
		incidentResource.associate(guid1, action);

		associatedStrings.add(insertedGuids.get(5));
		Thread.sleep(1000);
		AssociateIncidentAction action2  = new ErrorAssociationAction();
		action2.setIncidentGuid(guid1);
		action2.setAssociatedIds(associatedStrings);
		action2.setComment("please work test...?");
		incidentResource.associate(guid1, action2);
	}

	private void assignIncident(String guid) throws Exception {
		Thread.sleep(1000);
		incidentResource.takeAction(UUID.fromString(guid), new DefaultUser(testUser));
	}

	private void changeState(String guid) throws Exception {
		Thread.sleep(1000);
		incidentResource.takeAction(UUID.fromString(guid), Incident.State.OPEN);
	}

	private void addComment(String guid) throws Exception {
		Thread.sleep(1000);
		incidentResource.takeAction(UUID.fromString(guid), new DefaultComment(new DefaultUser(testUser), "This is a comment"));
	}

	private void addAndReadAttachment(String guid) throws Exception {
		Thread.sleep(1000);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		File file = new File(getClass().getResource("./test_upload_file.txt").getPath());
		builder.addBinaryBody("file", file);
		HttpEntity entity = builder.build();

		String userToken = getUserToken(testUser);
		String url = "http://localhost:" + getRestPort() + Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid + "/attachments";
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		post.setHeader("accessToken", userToken);

		HttpResponse response = reportingService().client().execute(post);

		DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "incident_attachment", 1, 60);

		JSONObject object = new JSONObject(IOUtils.toString(response.getEntity().getContent(), "UTF-8"));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		String actionGuid = object.getString(CommonField.DG_GUID_STRING);
		String urlPart = Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid + "/attachments/" + actionGuid;

		HttpResponse actionResponse = authGet(testUser, urlPart, HttpResponse.class);

		Assert.assertEquals(200, actionResponse.getStatusLine().getStatusCode());
		Assert.assertEquals(IOUtils.toString(new FileInputStream(file), "UTF-8"), IOUtils.toString(actionResponse.getEntity().getContent(), "UTF-8"));
		Assert.assertEquals("attachment; filename=\"" + file.getName() + "\"", actionResponse.getHeaders("Content-Disposition")[0].getValue());
	}

	private void readTimeline(String guid) throws Exception {
		DataFeedUtils.awaitESResult(ElasticIndex.INCIDENTS, Tenant.forId("1"), "download_incident_attachment", 1, 60);
		List<Object> timelineOccurrences = incidentResource.getIncidentTimeline(guid);
		Assert.assertEquals(13, timelineOccurrences.size());
		Assert.assertTrue(timelineOccurrences.get(12) instanceof CreateIncidentAction);
		for (int i = 11; i > 7; i--) {
			Assert.assertTrue(timelineOccurrences.get(i) instanceof JSONObject);
		}
		Assert.assertTrue(timelineOccurrences.get(7) instanceof UpdateIncidentAction);
		Assert.assertTrue(timelineOccurrences.get(6) instanceof EventAssociationAction);
		Assert.assertTrue(timelineOccurrences.get(5) instanceof ErrorAssociationAction);
		Assert.assertTrue(timelineOccurrences.get(4) instanceof AssignIncidentAction);
		Assert.assertTrue(timelineOccurrences.get(3) instanceof SetStateIncidentAction);
		Assert.assertTrue(timelineOccurrences.get(2) instanceof CommentIncidentAction);
		Assert.assertTrue(timelineOccurrences.get(1) instanceof AttachIncidentAction);
		Assert.assertTrue(timelineOccurrences.get(0) instanceof DownloadIncidentAttachmentAction);

		HttpResponse response = authGet(testUser, Statics.VERSIONED_REST_PREFIX + "/incidents/" + guid + "/timeline", HttpResponse.class);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		String str = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		new JSONArray(str);
	}

	private String createIncident(DefaultIncident incident) throws Exception {
		final HttpResponse httpResponse = authPost(testUser, Statics.VERSIONED_REST_PREFIX + "/incidents", new JSONObject(JSONUtils.objectToString(incident)), HttpResponse.class);
		assertEquals(200, httpResponse.getStatusLine().getStatusCode());
		String str = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
		JSONObject obj = new JSONObject(str);
		return obj.getString(CommonField.DG_GUID_STRING);
	}

	private void testSearch() throws Exception {
		List<Incident> incidents = incidentResource.searchIncidents("st1", null, null, null, IncidentSort.NEWEST_FIRST);
		assertEquals("Search not working correctly", 4, incidents.size());
		long prevMaxTime = Long.MAX_VALUE;
		for (int i = 0; i < 4; i++) {
			assertEquals("Search not working correctly", "test1", incidents.get(i).getName());
			assertTrue(prevMaxTime > incidents.get(i).getCreationTime().getTime());
			prevMaxTime = incidents.get(i).getCreationTime().getTime();
		}

		incidents = incidentResource.searchIncidents("fer oRe ent", null, null, null, IncidentSort.OLDEST_FIRST);
		assertEquals("Search not working correctly", 1, incidents.size());
		assertEquals("Search not working correctly", "more Different", incidents.get(0).getName());

		incidents = incidentResource.searchIncidents(null, null, null, Incident.Severity.HIGH, IncidentSort.OLDEST_FIRST);
		assertEquals("Search not working correctly", 5, incidents.size());
		long prevMinTime = 0;
		for (int i = 0; i < 5; i++) {
			assertEquals("Search not working correctly", Incident.Severity.HIGH, incidents.get(i).getSeverity());
			assertTrue(prevMinTime < incidents.get(i).getCreationTime().getTime());
			prevMinTime = incidents.get(i).getCreationTime().getTime();
		}

		incidents = incidentResource.searchIncidents(testUser, testUser, Incident.State.CLOSED, null, IncidentSort.LAST_MODIFIED_FIRST);

		assertEquals("Search not working correctly", 5, incidents.size());
		prevMaxTime = Long.MAX_VALUE;
		for (int i = 0; i < 5; i++) {
			assertEquals("Search not working correctly", Incident.State.CLOSED, incidents.get(i).getState());
			assertTrue(prevMaxTime > incidents.get(i).getCreationTime().getTime());
			prevMaxTime = incidents.get(i).getCreationTime().getTime();
		}
	}
}