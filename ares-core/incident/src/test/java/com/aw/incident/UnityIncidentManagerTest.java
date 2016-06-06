package com.aw.incident;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.util.VersionedObject;

import com.aw.incident.action.AssignIncidentAction;
import com.aw.incident.action.AttachIncidentAction;
import com.aw.incident.action.EventAssociationAction;
import com.aw.incident.action.IncidentAction;
import com.aw.unity.*;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.dg.CommonField;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.constraints.EqConstraint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Provider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class UnityIncidentManagerTest {

	@Mock
	private Provider<UnityInstance> unityInstanceProvider;
	@Mock
	private UnityInstance unityInstance;
	@Mock
	private UnityRunner runner;
	@Mock
	private DataType incidentDataType;
	@Mock
	private DataType assignIncidentActionType;

	private Field assignedField = new Field(Incident.INCIDENT_ASSIGN, FieldType.STRING);
	private Field stateField = new Field(Incident.INCIDENT_STATE, FieldType.STRING);
	private Field guidField = new Field(CommonField.ARES_GUID, FieldType.STRING);
	private Field modifiedField = new Field(Incident.INCIDENT_MODIFIED_TIME, FieldType.STRING);
	private Field createdField = new Field(CommonField.ARES_TIME, FieldType.STRING);
	private Field severityField = new Field(Incident.INCIDENT_SEVERITY, FieldType.STRING);
	private Field severityScoreField = new Field(Incident.INCIDENT_SEVERITY_SCORE, FieldType.STRING);
	private Field incidentGuidField = new Field("inc_guid", FieldType.GUID);

	private UnityIncidentManager manager;

	ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

	@Before
	public void setup() {
		PlatformSecurityContext context = mock(PlatformSecurityContext.class);
		when(context.getTenantID()).thenReturn("0");
		when(context.getUser()).thenReturn(new AuthenticatedUser("0", "Michael Jackson", "" ,""));
		ThreadLocalStore.set(context);

		when(unityInstanceProvider.get()).thenReturn(unityInstance);
		when(unityInstance.execute(queryCaptor.capture())).thenReturn(runner);
		when(unityInstance.getDataType(Incident.UNITY_TYPE)).thenReturn(incidentDataType);
		when(unityInstance.getDataType(AssignIncidentAction.UNITY_TYPE)).thenReturn(assignIncidentActionType);

		UnityMetadata metadata = mock(UnityMetadata.class);
		when(unityInstance.getMetadata()).thenReturn(metadata);
		DataTypeRepository repository = mock(DataTypeRepository.class);
		when(metadata.getDataTypeRepository()).thenReturn(repository);

		when(runner.getState()).thenReturn(UnityRunner.State.COMPLETE);

		when(incidentDataType.getField(Incident.INCIDENT_ASSIGN)).thenReturn(assignedField);
		when(incidentDataType.getField(Incident.INCIDENT_STATE)).thenReturn(stateField);
		when(incidentDataType.getField(Incident.INCIDENT_SEVERITY)).thenReturn(severityField);
		when(incidentDataType.getField(Incident.INCIDENT_SEVERITY_SCORE)).thenReturn(severityScoreField);
		when(incidentDataType.getField(Incident.INCIDENT_MODIFIED_TIME)).thenReturn(modifiedField);
		when(incidentDataType.getField(CommonField.ARES_TIME)).thenReturn(severityScoreField);
		when(incidentDataType.getField(CommonField.ARES_GUID)).thenReturn(guidField);
		when(incidentDataType.getField(CommonField.ARES_TIME)).thenReturn(createdField);
		when(assignIncidentActionType.getField("inc_guid")).thenReturn(incidentGuidField);


		manager = new UnityIncidentManager(unityInstanceProvider);
	}

	@Test
	public void testAllSortsGetIncidentsForCurrentUser() throws Exception {
		testGetIncidentsForCurrentUser(IncidentSort.HIGH_SEVERITY_FIRST, false);
		testGetIncidentsForCurrentUser(IncidentSort.LOW_SEVERITY_FIRST, false);
		testGetIncidentsForCurrentUser(IncidentSort.OLDEST_FIRST, false);
		Query query = testGetIncidentsForCurrentUser(IncidentSort.NEWEST_FIRST, false);
		Filter filter = query.getFilter();
		assertTrue(filter instanceof FilterGroup);
		FilterGroup filterGroup = (FilterGroup)filter;
		List<Filter> filters = filterGroup.getFilters();
		assertEquals(2, filters.size());
		query = testGetIncidentsForCurrentUser(IncidentSort.LAST_MODIFIED_FIRST, true);
		filter = query.getFilter();
		assertFalse(filter instanceof FilterGroup);
	}

	@Test
	public void testGetIncidentTimeline() throws Exception {
		testGetIncidentTimeline(true);
		testGetIncidentTimeline(false);
	}

	@Test
	public void testGetIncidentTimelineNoActions() throws Exception {
		setupResults(new ArrayList<>(), IncidentAction.class);
		DataUnityResults eventsResult = new DataUnityResults(getListOfEvents(0));
		when(runner.getResults(UnityRunner.ResponseFormat.UNITY_DATA)).thenReturn(eventsResult);
		List<Object> timeline = manager.getIncidentTimeline("I'm not a guid!");
		assertEquals(0, timeline.size());
	}

	@Test
	public void testGetIncidentTimelineNoEvents() throws Exception {
		setupResults(getListOfIncidentActions(0), IncidentAction.class);
		DataUnityResults eventsResult = new DataUnityResults(new ArrayList<>());
		when(runner.getResults(UnityRunner.ResponseFormat.UNITY_DATA)).thenReturn(eventsResult);
		List<Object> timeline = manager.getIncidentTimeline("I'm not a guid!");
		assertEquals(5, timeline.size());
	}

	@Test
	public void testSearchIncidents() throws Exception {
		List<Incident> incidents = getIncidents();
		setupResults(incidents, Incident.class);
		List<Incident> returnIncidents = manager.searchIncidents("one two three", "john hancock", Incident.State.OPEN, Incident.Severity.MINOR, IncidentSort.LAST_MODIFIED_FIRST);
		assertEquals(incidents, returnIncidents);
		Query query = getExecutedQuery();
		assertTrue(query.getFilter() instanceof FilterGroup);
		assertEquals(4, query.getFilter().getFilters().size());
		checkSort(IncidentSort.LAST_MODIFIED_FIRST, query);
		checkDetailQuery(query);
	}

	@Test
	public void testGetIncidentAction() throws Exception {
		List<IncidentAction> actions = new ArrayList<>();
		actions.add(new AttachIncidentAction());
		setupResults(actions, IncidentAction.class);
		IncidentAction returnAction = manager.getIncidentAction("guid me once", "shame on you");
		assertEquals(actions.get(0), returnAction);
		Query query = getExecutedQuery();
		checkDetailQuery(query);
		assertTrue(query.getFilter() instanceof EqConstraint);
	}

	@Test
	public void testGetIncidentActionNotFound() throws Exception {
		List<IncidentAction> actions = new ArrayList<>();
		setupResults(actions, IncidentAction.class);
		IncidentAction returnAction = manager.getIncidentAction("guid me twice", "shame on me");
		assertNull(returnAction);
		Query query = getExecutedQuery();
		checkDetailQuery(query);
		assertTrue(query.getFilter() instanceof EqConstraint);
	}

	@Test
	public void testGetIncidentActionMultipleResults() throws Exception {
		List<IncidentAction> actions = getListOfIncidentActions(0);
		setupResults(actions, IncidentAction.class);
		IncidentAction returnAction = manager.getIncidentAction("guid me thrice", "I dunno");
		assertEquals(actions.get(0), returnAction);
		Query query = getExecutedQuery();
		checkDetailQuery(query);
		assertTrue(query.getFilter() instanceof EqConstraint);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetBufferedIncidents1() throws Exception {
		manager.getBufferedIncidentsForCurrentUser(null, true, 0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetBufferedIncidents2() throws Exception {
		manager.getBufferedIncidentsForCurrentUser(null, true, null, 0);
	}

	private Query getExecutedQuery() {
		return queryCaptor.getValue();
	}

	private <T> void setupResults(List<T> returnItems, Class<T> itemClass) throws Exception {
		List<VersionedObject<T>> versionedObjects = returnItems.stream().map(item -> new VersionedObject<>(1, item)).collect(Collectors.toList());
		PojoUnityResults<T> results = new PojoUnityResults<>(versionedObjects);
		when(runner.getResults(itemClass)).thenReturn(results);
	}

	private Query testGetIncidentsForCurrentUser(IncidentSort sort, boolean all) throws Exception {
		List<Incident> incidents = getIncidents();
		setupResults(incidents, Incident.class);
		List<Incident> returnIncidents = manager.getIncidentsForCurrentUser(sort, all);
		assertEquals(incidents, returnIncidents);
		Query query = getExecutedQuery();
		assertArrayEquals(new DataType[] {incidentDataType}, query.getDataTypes());
		assertEquals(50, query.getSize());
		checkDetailQuery(query);
		checkSort(sort, query);
		return query;
	}

	private void testGetIncidentTimeline(boolean eventsFirst) throws Exception {
		setupResults(getListOfIncidentActions(eventsFirst ? 0 : 1), IncidentAction.class);
		DataUnityResults eventsResult = new DataUnityResults(getListOfEvents(eventsFirst ? 1 : 0));
		when(runner.getResults(UnityRunner.ResponseFormat.UNITY_DATA)).thenReturn(eventsResult);
		List<Object> timeline = manager.getIncidentTimeline("I'm not a guid!");
		assertEquals(10, timeline.size());
		for (int i = 0; i < 5; i++) {
			int actionIndex = (2 * i) + (eventsFirst ? 1 : 0);
			int eventsIndex = (2 * i) + (eventsFirst ? 0 : 1);
			int shouldBeNumber = -(i - 4);
			assertTrue(timeline.get(eventsIndex) instanceof JSONObject);
			assertEquals("event " + shouldBeNumber, ((JSONObject)timeline.get(eventsIndex)).getString("dg_guid"));
			assertTrue(timeline.get(actionIndex) instanceof IncidentAction);
			assertEquals(new UUID(shouldBeNumber, shouldBeNumber), ((IncidentAction)timeline.get(actionIndex)).getGuid());
		}
	}

	private List<Incident> getIncidents() {
		List<Incident> incidents = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Incident incident = new DefaultIncident();
			incident.setName("I am number " + i);
			incidents.add(incident);
		}
		return incidents;
	}

	private List<IncidentAction> getListOfIncidentActions(int offset) {
		List<IncidentAction> actions = new ArrayList<>();
		for (int i = 4; i >= 0; i--) {
			List<String> ids = new ArrayList<>();
			ids.add(i + "");
			EventAssociationAction action = mock(EventAssociationAction.class);
			when(action.getTimestamp()).thenReturn(Instant.ofEpochSecond(2 * i + offset));
			when(action.getAssociatedIds()).thenReturn(ids);
			when(action.getGuid()).thenReturn(new UUID(i, i));
			actions.add(action);
		}
		return actions;
	}

	private List<Data> getListOfEvents(int offset) {
		List<Data> events = new ArrayList<>();
		for (int i = 4; i >= 0; i--) {
			Data data = mock(Data.class);
			when(data.getTime()).thenReturn(Instant.ofEpochSecond((2 * i) + offset));
			when(data.toJsonString(false, true, false)).thenReturn("{\"dg_guid\":\"event " + i + "\"}");
			events.add(data);
		}
		return events;
	}

	private void checkDetailQuery(Query query) {
		assertTrue(query.isAllAttributes());
		assertTrue(query.isDetail());
		assertFalse(query.getCountOnly());
	}

	private void checkSort(IncidentSort sort, Query query) {
		QueryAttribute[] attributes;
		switch (sort) {
			case HIGH_SEVERITY_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(severityScoreField, QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE),
					new QueryAttribute(createdField, QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case LOW_SEVERITY_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(severityScoreField, QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE),
					new QueryAttribute(createdField, QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case NEWEST_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(createdField, QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case OLDEST_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(createdField, QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case LAST_MODIFIED_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(modifiedField, QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
			default:
				attributes = null;
		}
		assertEquals(attributes.length, query.getAttributes().length);
		for (int i = 0; i < attributes.length; i++) {
			assertEquals(attributes[i].getField().getName(), query.getAttributes()[i].getField().getName());
			assertEquals(attributes[i].getOrder(), query.getAttributes()[i].getOrder());
			assertEquals(attributes[i].getAggregate(), query.getAttributes()[i].getAggregate());
		}
	}
}
