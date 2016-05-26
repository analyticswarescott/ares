package com.aw.incident;

import com.aw.incident.action.IncidentAction;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author jhaight
 */
public interface IncidentManager {

	/**
	 * Get incidents assigned to current user
	 * @param sort The way the incidents should be sorted
	 * @param includeClosedIncidents Whether or not to include incidents marked as closed
     * @return The list of incidents
     */
	List<Incident> getIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents) throws Exception;

	/**
	 * Get a portion of assigned incidents for the current user
	 * @param sort The way the incidents should be sorted
	 * @param includeClosedIncidents Whether or not to include incidents marked as closed
	 * @param bufferedAmount The amount of incidents to retrieve
     * @return The list of incidents
     */
	List<Incident> getBufferedIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents, int bufferedAmount) throws Exception;

	/**
	 * Get a portion of assigned incidents for the current user
	 * @param sort The way the incidents should be sorted
	 * @param includeClosedIncidents Whether or not to include incidents marked as closed
	 * @param startingGuid The GUID of the last received
	 * @param bufferedAmount The amount of incidents to retrieve
     * @return The list of incidents
     */
	List<Incident> getBufferedIncidentsForCurrentUser(IncidentSort sort, boolean includeClosedIncidents, String startingGuid, int bufferedAmount) throws Exception;

	/**
	 * Retrieves detailed information in a timeline about a particular incident
	 * @param guid The GUID  of the incident
	 * @return The list of incident occurrences
     */
	List<Object> getIncidentTimeline(String guid) throws Exception;

	/**
	 * Search incidents belonging to the current tenant
	 * @param text Text that must be found in one value of the incident to be included
	 * @param user The user assigned to the incident
	 * @param state The state of the incident
	 * @param severity The severity of the incident
	 * @param sort The way to sort the incidents
     * @return The matching incidents
     */
	List<Incident> searchIncidents(@Nullable String text, @Nullable String user, @Nullable Incident.State state, @Nullable Incident.Severity severity, IncidentSort sort) throws Exception;

	/**
	 * Get a specific incident action
	 * @param guid The guid of the action
	 * @param actionUnityType The unity type of the specified action
     * @return The specific incident action, or null if not found
     */
	IncidentAction getIncidentAction(String guid, String actionUnityType) throws Exception;
}
