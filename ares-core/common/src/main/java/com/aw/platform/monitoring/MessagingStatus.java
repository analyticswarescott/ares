package com.aw.platform.monitoring;

import com.aw.platform.Platform;

/**
 * Status of the messaging layer (kafka) for a platform
 *
 *
 *
 */
public interface MessagingStatus extends Status {

	/**
	 * Collect messaging status given a platform
	 *
	 * @param platform
	 */
	public void collect(Platform platform);

}
