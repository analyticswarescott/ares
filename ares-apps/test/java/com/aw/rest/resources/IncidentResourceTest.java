package com.aw.rest.resources;

import com.aw.action.ActionManager;
import com.aw.common.auth.User;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.incident.*;
import com.aw.incident.action.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author jhaight
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class IncidentResourceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private ActionManager actionManager;

	@Mock
	private IncidentManager incidentManager;

	@Inject @InjectMocks
	private IncidentResource incidentResource;

	@Before
	public void setup() throws Exception {
		SecurityUtil.setThreadSystemAccess();
	}

	@Test
	public void getIncidentsTest() throws Exception {
		List<Incident> incidents = new ArrayList<>();
		incidents.add(new DefaultIncident());
		when(incidentManager.getIncidentsForCurrentUser(IncidentSort.HIGH_SEVERITY_FIRST, false)).thenReturn(incidents);
		Collection<Incident> returnIncidents = incidentResource.getIncidents(IncidentSort.HIGH_SEVERITY_FIRST, false);
		Assert.assertSame(incidents, returnIncidents);
	}

	@Test
	public void searchIncidentsTest1() throws Exception {
		List<Incident> incidents = new ArrayList<>();
		incidents.add(new DefaultIncident());
		when(incidentManager.searchIncidents("test", null, null, null, IncidentSort.HIGH_SEVERITY_FIRST)).thenReturn(incidents);
		Collection<Incident> returnIncidents = incidentResource.searchIncidents("test", null, null, null, IncidentSort.HIGH_SEVERITY_FIRST);
		Assert.assertSame(incidents, returnIncidents);
	}

	@Test
	public void searchIncidentsTest2() throws Exception {
		List<Incident> incidents = new ArrayList<>();
		incidents.add(new DefaultIncident());
		when(incidentManager.searchIncidents("test", "bob-0", Incident.State.OPEN, Incident.Severity.INFORMATIONAL, IncidentSort.HIGH_SEVERITY_FIRST)).thenReturn(incidents);
		Collection<Incident> returnIncidents = incidentResource.searchIncidents("test", "bob-0", Incident.State.OPEN, Incident.Severity.INFORMATIONAL, IncidentSort.HIGH_SEVERITY_FIRST);
		Assert.assertSame(incidents, returnIncidents);
	}

	@Test
	public void getIncidentTimelineTest() throws Exception {
		String guid = "I'm not really a guid, but don't tell anyone";
		List<Object> actions = new ArrayList<>();
		actions.add(new CreateIncidentAction());
		when(incidentManager.getIncidentTimeline(guid)).thenReturn(actions);
		List<Object> returnActions = incidentResource.getIncidentTimeline(guid);
		Assert.assertSame(actions, returnActions);
	}

	@Test
	public void postIncidentTest() throws Exception {
		Incident incident = mock(Incident.class);
		Incident returnIncident = incidentResource.createIncident(incident);
		verify(incident).setCreationType(CreationType.MANUAL); // Set to manual creation type
		verify(actionManager).takeAction(any(CreateIncidentAction.class)); // Action manager was called to create a new incident
		Assert.assertEquals(incident, returnIncident);
	}

	@Test
	public void updateIncidentTest() throws Exception {
		Incident incident = mock(Incident.class);
		String fakeGuid = "21EC2020-3AEA-4069-A2DD-08002B30309D";
		Incident returnIncident = incidentResource.updateIncident(fakeGuid, incident);
		verify(incident).setGuid(UUID.fromString(fakeGuid));
		verify(actionManager).takeAction(any(UpdateIncidentAction.class));
		Assert.assertEquals(incident, returnIncident);
	}

	@Test
	public void associateTest() throws Exception {
		String fakeGuid1 = "21EC2020-DDDD-FFFF-EEEE-08002B30309D";
		String fakeGuid2 = "21EC2020-DDDD-2222-EEEE-08002B30309D";
		AssociateIncidentAction association = new ErrorAssociationAction();
		association.setGuid(UUID.fromString(fakeGuid2));
		incidentResource.associate(fakeGuid1, association);
		Assert.assertEquals(association.getIncidentGuid(), fakeGuid1);
		verify(actionManager).takeAction(association);
	}

	@Test
	public void assignIncidentTest() throws Exception {
		String fakeGuid = "21EC2020-DDDD-FFFF-EEEE-08002B30309D";
		User user = mock(User.class);
		AssignIncidentAction returnAction = incidentResource.takeAction(UUID.fromString(fakeGuid), user);
		verify(actionManager).takeAction(returnAction);
		Assert.assertEquals(returnAction.getAssignee(), user);
		Assert.assertEquals(UUID.fromString(fakeGuid), UUID.fromString(returnAction.getIncidentGuid()));
	}

	@Test
	public void setIncidentStateTest() throws Exception {
		String fakeGuid = "21EC2020-DDDD-FFFF-EEEE-08002B30309D";
		Incident.State state = Incident.State.OPEN;
		SetStateIncidentAction returnAction = incidentResource.takeAction(UUID.fromString(fakeGuid), state);
		verify(actionManager).takeAction(returnAction);
		Assert.assertEquals(Incident.State.OPEN, returnAction.getState());
		Assert.assertEquals(UUID.fromString(fakeGuid), UUID.fromString(returnAction.getIncidentGuid()));
	}

	@Test
	public void addCommentTest() throws Exception {
		String fakeGuid = "21EC2020-DDDD-FFFF-EEEE-08002B30309D";
		DefaultComment comment = new DefaultComment();
		comment.setComment("This is a comment that I am writing.");
		CommentIncidentAction returnAction = incidentResource.takeAction(UUID.fromString(fakeGuid), comment);
		verify(actionManager).takeAction(returnAction);
		Assert.assertEquals(UUID.fromString(fakeGuid), UUID.fromString(returnAction.getIncidentGuid()));
		Assert.assertEquals(comment.getComment(), returnAction.getComment());
	}

	@Test
	public void attachFileTest() throws Exception {
		UUID fakeGuid = UUID.randomUUID();
		InputStream fakeFile = mock(InputStream.class);
		FormDataContentDisposition fakeMetaData = mock(FormDataContentDisposition.class);
		when(fakeMetaData.getFileName()).thenReturn("kitty cat party.png");
		AttachIncidentAction returnAction = incidentResource.attachFile(fakeGuid, fakeFile, fakeMetaData);
		verify(actionManager).takeAction(returnAction);
		Assert.assertEquals(fakeGuid.toString(), returnAction.getIncidentGuid());
	}

	@Test
	public void downloadFileTest() throws Exception {
		UUID incidentGuid = UUID.randomUUID();
		UUID actionGuid = UUID.randomUUID();
		AttachIncidentAction attachAction = new AttachIncidentAction();
		attachAction.setIncidentGuid(incidentGuid.toString());
		when(incidentManager.getIncidentAction(actionGuid.toString(), AttachIncidentAction.UNITY_TYPE)).thenReturn(attachAction);
		incidentResource.getAttachment(incidentGuid, actionGuid);
	}

	@Test
	public void downloadInvalidFileTest() throws Exception {
		UUID incidentGuid = UUID.randomUUID();
		UUID actionGuid = UUID.randomUUID();
		when(incidentManager.getIncidentAction(actionGuid.toString(), AttachIncidentAction.UNITY_TYPE)).thenReturn(null);
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("404 Not Found");
		incidentResource.getAttachment(incidentGuid, actionGuid);
	}

	@Test
	public void downloadValidActionInvalidIncidentTest() throws Exception {
		UUID incidentGuid = UUID.randomUUID();
		UUID actionGuid = UUID.randomUUID();
		AttachIncidentAction attachAction = new AttachIncidentAction();
		attachAction.setIncidentGuid("you lied to me!");
		when(incidentManager.getIncidentAction(actionGuid.toString(), AttachIncidentAction.UNITY_TYPE)).thenReturn(attachAction);
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("404 Not Found");
		incidentResource.getAttachment(incidentGuid, actionGuid);
	}

	private InputStream createFakeInputStream() throws Exception {
		return new FakeInputStream();
	}

	private class FakeInputStream extends InputStream {

		int count = 10000;

		@Override
		public int read() throws IOException {
			if (count > 0) {
				count--;
				return count;
			} else {
				return -1;
			}
		}
	}
}