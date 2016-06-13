package com.aw.incident.action;

import org.codehaus.jettison.json.JSONObject;

import com.aw.action.ActionContext;
import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.util.KeyValuePair;
import com.aw.common.util.es.ESKnownIndices;
import com.aw.incident.Incident;
import com.aw.unity.dg.CommonField;
import com.aw.util.Statics;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An action on an existing incident
 *
 *
 *
 */
public abstract class AbstractUpdateIncidentAction extends AbstractIncidentAction implements TenantAware {

	protected AbstractUpdateIncidentAction() {
	}

	protected AbstractUpdateIncidentAction(String guid) {
		setIncidentGuid(guid);
	}

	/**
	 * Update a single property on an incident-  can't use the guid property on the object as that is not thread safe. You
	 * may have many threads executing the same update instance against different incident guids - this is why the action
	 * context object is used.
	 *
	 * @param property The property to update
	 * @param value The value to set the property to
	 * @throws Exception If anything goes wrong
	 */
	protected void update(ActionContext ctx, String incidentGuid, String property, Object value) throws ActionExecutionException {

		update(ctx, incidentGuid, new KeyValuePair(property, value));

	}

	/**
	 * Update properties on an incident
	 *
	 * @param properties The properties to update
	 * @throws Exception If anything goes wrong
	 */
	protected void update(ActionContext ctx, String incidentGuid, KeyValuePair... properties) throws ActionExecutionException {

		try {

			JSONObject update = new JSONObject();
			for (KeyValuePair prop : properties) {
				update.put(prop.getKey(), prop.getValue());
			}
			putChange(ctx, update, incidentGuid);

		} catch (Exception e) {
			throw new ActionExecutionException(this, e);
		}

	}

	/**
	 * Make the request directly to ES to change the incident
	 *
	 * @param json
	 * @throws Exception
	 */
	protected void putChange(ActionContext ctx, JSONObject json, String incidentGuid) throws Exception {

		Incident incident = getClient(ctx).getIncident(ctx.getUnity(), incidentGuid, 3, 1000L).get();

		//update the incident here - an exception will occur if there is an error
		getClient(ctx).update(ESKnownIndices.INCIDENTS, Incident.UNITY_TYPE, incidentGuid, json.toString(), incident.getCreationTime().toInstant());

	}

	/**
	 * @param ctx The current action context
	 * @return The incident guid for this incident update action, using the property first, or the context as a fallback, looking for an incident that was created previously
	 */
	protected String getIncidentGuidString(ActionContext ctx) throws ActionPreparationException {

		String ret = null;

		//look for incident.dg_guid if nothing specified
		String raw = getIncidentGuid();
		if (raw == null) {
			raw = Statics.VARIABLE_PREFIX + CreateIncidentAction.VAR_INCIDENT + ActionContext.VARIABLE_SEPARATOR + CommonField.DG_GUID.toString() + Statics.VARIABLE_SUFFIX;
		}

		//look for the guid for a created incident if there is no guid
		String guid = null;
		try {
			guid = replaceVariables(raw, ctx);
		} catch (Exception e) {
			throw new ActionPreparationException("error getting incident guid", e);
		}

		//if nothing that way, error - this should not happen unless there's a problem with getVariableString()
		if (guid == null) {
			throw new ActionPreparationException("could not determine incident guid for tenant=" + getTenantID() + " action=" + getClass().getSimpleName() + " incidentGuid=" + raw);
		}

		//else we have our guid
		ret = guid;

		return ret;

	}

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		setIncidentGuid(getIncidentGuidString(ctx));

	}

	@Override
	public <T> T copy() throws ActionCopyException {
		AbstractUpdateIncidentAction ret = (AbstractUpdateIncidentAction)super.copy();
		ret.m_incidentGuid = m_incidentGuid;
		return (T)ret;
	}


	@JsonProperty(INCIDENT_GUID)
	public String getIncidentGuid() { return m_incidentGuid; }
	public void setIncidentGuid(String incidentGuid) { m_incidentGuid = incidentGuid; }
	private String m_incidentGuid = null;

}
