package com.aw.common.messaging;

import com.aw.common.tenant.Tenant;

/**
 * A message sent in the platform
 *
 *
 *
 * @param <T> The payload type
 */
public interface Message<T> {

	/**
	 * @return The tenant the message is associated with
	 */
	public Tenant getSourceTenant();

	/**
	 * @return The topic the message is sent on
	 */
	public Topic getTopic();

	/**
	 * @return The message payload
	 */
	public T getPayload();
}
