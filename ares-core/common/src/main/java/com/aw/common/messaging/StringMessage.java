package com.aw.common.messaging;

import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;

/**
 * A message containing a simple string
 *
 *
 *
 */
public class StringMessage extends AbstractMessage<String> {

	/**
	 * Converts object to json
	 *
	 * @param sourceTenant The tenant generating the message
	 * @param topic The topic for the message
	 * @param payload The data within the message
	 */
	public StringMessage(Tenant sourceTenant, Topic topic, Object payload) {
		this(sourceTenant, topic, JSONUtils.objectToString(payload));
	}

	/**
	 * Send a string message
	 *
	 * @param sourceTenant The tenant from which the message is sent
	 * @param topic The topic to which the message is sent
	 * @param payload The data within the message
	 */
	public StringMessage(Tenant sourceTenant, Topic topic, String payload) {
		super(sourceTenant, topic);
		m_payload = payload;
	}

	public String getPayload() { return m_payload; }
	private String m_payload;

}
