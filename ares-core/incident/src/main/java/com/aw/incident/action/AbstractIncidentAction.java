package com.aw.incident.action;

import com.aw.action.AbstractAction;
import com.aw.action.ActionContext;
import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.auth.DefaultUser;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.ResourceManager;
import com.aw.incident.CreationType;
import com.aw.incident.IncidentESClient;
import com.aw.platform.Platform;
import com.aw.util.Statics;
import com.fasterxml.jackson.annotation.JsonProperty;

import kafka.producer.KeyedMessage;

/**
 * Base class for incident actions
 *
 *
 *
 */
public abstract class AbstractIncidentAction extends AbstractAction implements IncidentAction, SecurityAware {

	/**
	 * The incident guid property on incident update actions
	 */
	protected static final String INCIDENT_GUID = "inc_guid";
	/**
	 * Sets up the abstract incident action as one created by the current user automatically if the user
	 * is daw with a new guid, current timestamp, and empty comment.
	 *
	 */
	protected AbstractIncidentAction() {
		setUser(new DefaultUser(getUserID()));
		setCreationType(Statics.DG_USER.equals(getUserID()) ? CreationType.AUTOMATIC : CreationType.MANUAL);
	}

	protected void sendMessage(Platform platform, Topic topic, Object payload) throws ActionExecutionException {

		//build the incident message to add to kafka - send in epoch_millis format for internal unity processing
		KeyedMessage<String, String> msg = new KeyedMessage<>(Topic.toTopicString(getTenantID(), topic), getTenantID(), JSONUtils.objectToString(payload));

		try {

			//add the incident to the incident topic for addition to the system
			ResourceManager.KafkaProducerSingleton.getInstance(platform).send(msg);

		} catch (Exception e) {
			throw new ActionExecutionException("error sending message to kafka topic " + topic, this, e);
		}

		//if all went well, we're done

	}

	@Override
	public <T> T copy() throws ActionCopyException {
		AbstractIncidentAction ret = (AbstractIncidentAction)super.copy();
		ret.m_creationType = m_creationType;
		return (T)ret;
	}

	/**
	 * @return The incident client
	 */
	protected IncidentESClient getClient(ActionContext ctx) {

		if (client == null) {
			client = new IncidentESClient(ctx.getPlatform());
		}

		return client;

	}
	private IncidentESClient client = null;

	@JsonProperty("inc_ctype")
	public CreationType getCreationType() { return m_creationType; }
	public void setCreationType(CreationType creationType) { m_creationType = creationType; }
	private CreationType m_creationType;

}
