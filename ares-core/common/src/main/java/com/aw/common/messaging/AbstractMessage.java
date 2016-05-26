package com.aw.common.messaging;

import com.aw.common.tenant.Tenant;

/**
 * Basic message implementation
 *
 *
 *
 */
public abstract class AbstractMessage<T> implements Message<T> {

	public AbstractMessage(Tenant sourceTenant, Topic topic) {
		m_sourceTenant = sourceTenant;
		m_topic = topic;
	}

	public Tenant getSourceTenant() { return m_sourceTenant; }
	public void setSourceTenant(Tenant sourceTenant) { m_sourceTenant = sourceTenant; }
	private Tenant m_sourceTenant;

	public Topic getTopic() { return m_topic; }
	public void setTopic(Topic topic) { m_topic = topic; }
	private Topic m_topic;

}
