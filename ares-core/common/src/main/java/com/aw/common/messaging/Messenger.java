package com.aw.common.messaging;

import java.io.IOException;

/**
 * Sends messages containing a payload of the specified type
 *
 *
 *
 * @param <T> The payload type
 */
public interface Messenger<T> {

	/**
	 * Sends a message on a topic
	 *
	 * @param topic
	 * @param message
	 */
	public void send(Message<T> message) throws IOException;

	/**
	 * Close the messenger
	 */
	public void close();

}
