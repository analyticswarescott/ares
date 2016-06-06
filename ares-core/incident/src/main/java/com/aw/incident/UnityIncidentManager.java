package com.aw.incident;

import com.aw.common.rest.security.SecurityAware;
import com.aw.common.util.VersionedObject;
import com.aw.incident.action.*;
import com.aw.unity.*;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.dg.CommonField;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.constraints.ContainsConstraint;
import com.aw.unity.query.constraints.EqConstraint;
import com.aw.unity.query.constraints.InConstraint;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides access to the incident through the UnityInstance
 * @author jhaight
 */
public class UnityIncidentManager implements IncidentManager, SecurityAware {

	/**
	 * The default amount of incidents to get for a getIncidentsForCurrentUser request
	 */
	private static final int GET_INCIDENTS_COUNT = 50;

	/**
	 * The default amount of incidents to get for a getIncidentsActions request
	 */
	private static final int GET_INCIDENTS_ACTIONS_COUNT = 25;

	/**
	 * The default amount of incidents to get for a search request
	 */
	private static final int GET_INCIDENTS_SEARCH_COUNT = 25;


	private Provider<UnityInstance> unityProvider;

	private DataType incidentDataType;

	@Inject
	public UnityIncidentManager(Provider<UnityInstance> unityProvider) {
		this.unityProvider = unityProvider;
		incidentDataType = getIncidentDataType();
	}

	@Override
	public List<Incident> getIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents) throws Exception {

		Filter queryFilter = new EqConstraint(getField(Incident.INCIDENT_ASSIGN), getSecurityContext().getUser().getName());
		if (!includeClosedIncidents) {
			EqConstraint notClosed = new EqConstraint(getField(Incident.INCIDENT_STATE), Incident.State.CLOSED);
			notClosed.setNot(true);
			queryFilter = new FilterGroup().addFilter(queryFilter).addFilter(notClosed);
		}

		Query query = UnityUtil.getDetailQueryFor(getIncidentDataType(), queryFilter);
		query.setSize(GET_INCIDENTS_COUNT);
		query.setAttributes(getSortingQueryAttributes(sort));
		return executeQuery(query, Incident.class);
	}

	@Override
	public List<Incident> getBufferedIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents, int bufferedAmount) throws Exception {
		throw new UnsupportedOperationException("Method not yet implemented");
	}

	@Override
	public List<Incident> getBufferedIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents, String startingGuid, int bufferedAmount) throws Exception {
		throw new UnsupportedOperationException("Method not yet implemented");
	}

	@Override
	public List<Object> getIncidentTimeline(String guid) throws Exception {
		List<IncidentAction> actions = queryForIncidentActions(guid);
		DataUnityResults events = queryForEvents(actions);
		return createTimeline(actions, events);
	}

	@Override
	public List<Incident> searchIncidents(
		@Nullable String text,
		@Nullable String user,
		@Nullable Incident.State state,
		@Nullable Incident.Severity severity,
		IncidentSort sort)
		throws Exception {
		Query query  = new Query();
		FilterGroup filters = new FilterGroup(FilterGroup.GroupOperator.AND);
		if (text != null && !text.isEmpty()) {
			filters.addFilter(createTextFilters(text));
		}

		if (user != null && !user.isEmpty()) {
			filters.addFilter(new EqConstraint(getField(Incident.INCIDENT_ASSIGN), user));
		}

		if (state != null) {
			filters.addFilter(new EqConstraint(getField(Incident.INCIDENT_STATE), state));
		}

		if (severity != null) {
			filters.addFilter(new EqConstraint(getField(Incident.INCIDENT_SEVERITY), severity));
		}
		query.setAllAttributes(true);
		query.setDetail(true);
		query.setCountOnly(false);
		query.setFilter(filters);
		query.setDataTypes(incidentDataType);
		query.setSize(GET_INCIDENTS_SEARCH_COUNT);
		query.setAttributes(getSortingQueryAttributes(sort));

		return executeQuery(query, Incident.class);
	}

	@Override
	public IncidentAction getIncidentAction(String guid, String actionUnityType) throws Exception {
		EqConstraint guidConstraint = new EqConstraint(getField(CommonField.ARES_GUID), guid);
		DataType actionType = unityProvider.get().getDataType(actionUnityType);
		Query query = UnityUtil.getDetailQueryFor(actionType, guidConstraint);
		List<IncidentAction> matchingActions = executeQuery(query, IncidentAction.class);
		return firstOrNull(matchingActions);
	}

	private Filter createTextFilters(String text) {
		FilterGroup filters = new FilterGroup(FilterGroup.GroupOperator.AND);
		String[] textSplitBySpaces = text.split(" ");
		for (String textPortion : textSplitBySpaces) {
			filters.addFilter(new ContainsConstraint(UnityInstance.ALL_FIELD, textPortion.toLowerCase()));
		}
		return filters;
	}

	private DataType getIncidentDataType() {
		return unityProvider.get().getDataType(Incident.UNITY_TYPE);
	}

	private Field getField(String name) {
		return incidentDataType.getField(name);
	}

	private QueryAttribute[] getSortingQueryAttributes(IncidentSort sort) {
		QueryAttribute[] attributes = null;
		switch (sort) {
			case HIGH_SEVERITY_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(getField(Incident.INCIDENT_SEVERITY_SCORE), QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE),
					new QueryAttribute(getField(CommonField.ARES_TIME), QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case LOW_SEVERITY_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(getField(Incident.INCIDENT_SEVERITY_SCORE), QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE),
					new QueryAttribute(getField(CommonField.ARES_TIME), QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case NEWEST_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(getField(CommonField.ARES_TIME), QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case OLDEST_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(getField(CommonField.ARES_TIME), QueryAttribute.Order.ASC, QueryAttribute.Aggregate.NONE)
				};
				break;
			case LAST_MODIFIED_FIRST:
				attributes = new QueryAttribute[] {
					new QueryAttribute(getField(Incident.INCIDENT_MODIFIED_TIME), QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
				};
				break;
		}
		return attributes;
	}

	private <T> List<T> executeQuery(Query query, Class<T> classType) throws Exception {
		PojoUnityResults<T> rawResults = UnityUtil.executeBlocking(unityProvider.get(), query, 30_000, classType);
		return rawResults.getPojos().stream().map(VersionedObject::get).collect(Collectors.toList());
	}

	private Collection<Object> getEventGuids(List<IncidentAction> actions) {
		Set<Object> guids = new HashSet<>();
		actions.forEach(x -> addAssociationGuidsToIncident(guids, x));
		return guids;
	}

	private void addAssociationGuidsToIncident(Collection<Object> guids, IncidentAction action) {
		if (action instanceof AssociateIncidentAction) {
			AssociateIncidentAction associationAction = (AssociateIncidentAction)action;
			guids.addAll(associationAction.getAssociatedIds());
		}
	}

	private List<Object> createTimeline(List<IncidentAction> actions, DataUnityResults events) throws Exception {
		List<Object> timeline = new ArrayList<>();
		ListIterator<Data> eventsIterator = events.getData().listIterator();
		ListIterator<IncidentAction> actionIterator = actions.listIterator();

		while (eventsIterator.hasNext() || actionIterator.hasNext()) {
			timeline.add(getNextTimelinePieceAndAdvanceIterator(eventsIterator, actionIterator));
		}

		return timeline;
	}

	private Object getNextTimelinePieceAndAdvanceIterator(ListIterator<Data> events, ListIterator<IncidentAction> actions) throws Exception {
		Object nextData;
		if (!events.hasNext()) {
			nextData = actions.next();
		} else if (!actions.hasNext()) {
			nextData = convertDataToJsonObject(events.next());
		} else {
			Data event = events.next();
			IncidentAction action = actions.next();

			if (action.getTimestamp().isAfter(event.getTime())) {
				nextData = action;
				events.previous();
			} else {
				nextData = convertDataToJsonObject(event);
				actions.previous();
			}
		}

		return nextData;
	}

	private JSONObject convertDataToJsonObject(Data data) throws Exception {
		return new JSONObject(data.toJsonString(false, true, false));
	}

	private List<IncidentAction> queryForIncidentActions(String guid) throws Exception {
		final EqConstraint guidConstraint = new EqConstraint(unityProvider.get().getDataType(AssignIncidentAction.UNITY_TYPE).getField("inc_guid"), guid);
		Query query = new Query();
		query.setSize(GET_INCIDENTS_ACTIONS_COUNT);
		query.setAttributes(getIncidentSorting());
		query.setAllAttributes(true);
		query.setDetail(true);
		query.setCountOnly(false);
		query.setDataTypes(getIncidentActionDataTypes());
		query.setFilter(guidConstraint);
		query.setDataTypes(getIncidentActionDataTypes());
		return executeQuery(query, IncidentAction.class);
	}

	private DataUnityResults queryForEvents(List<IncidentAction> actions) throws Exception {
		Collection<Object> eventGuids = getEventGuids(actions);
		DataUnityResults results = new DataUnityResults(new ArrayList<>());
		if (!eventGuids.isEmpty()) {
			Filter filter = new InConstraint(getField(CommonField.ARES_GUID), eventGuids);
			Query query = new Query();
			query.setFilter(filter);
			query.setSize(eventGuids.size());
			query.setAttributes(getIncidentSorting());
			query.setAllAttributes(true);
			query.setDetail(true);
			query.setCountOnly(false);
			query.setDataTypes(unityProvider.get().getMetadata().getDataTypeRepository().getDataTypes());
			results = UnityUtil.executeBlocking(unityProvider.get(), query, 30_000, UnityRunner.ResponseFormat.UNITY_DATA);
		}
		return results;
	}

	private DataType[] getIncidentActionDataTypes() {
		return new DataType[] {
			unityProvider.get().getDataType(AssignIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(AssociateIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(AttachIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(CommentIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(CreateIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(ErrorAssociationAction.UNITY_TYPE),
			unityProvider.get().getDataType(EventAssociationAction.UNITY_TYPE),
			unityProvider.get().getDataType(SetStateIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(UpdateIncidentAction.UNITY_TYPE),
			unityProvider.get().getDataType(DownloadIncidentAttachmentAction.UNITY_TYPE)
		};
	}

	private QueryAttribute[] getIncidentSorting() {
		return new QueryAttribute[] {
			new QueryAttribute(getField(unityProvider.get().getDefaultTimeField()), QueryAttribute.Order.DESC, QueryAttribute.Aggregate.NONE)
		};
	}

	private <T> T firstOrNull(List<T> items) {
		T item = null;
		if (items != null && items.size() > 0) {
			item = items.get(0);
		}
		return item;
	}
}
