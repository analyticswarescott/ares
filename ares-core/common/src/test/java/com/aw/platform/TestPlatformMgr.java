package com.aw.platform;

import org.apache.log4j.Logger;

/**
 * If errors occur during unit tests that are handled in the PlatformMgr, they will just be logged
 */
public class TestPlatformMgr extends PlatformMgr {

	static final Logger LOGGER = Logger.getLogger(PlatformMgr.class);

	@Override
	void handleMessage(boolean error, String host, Object message, NodeRole origin, NodeRole... related) {
		if (message instanceof Exception) {
			LOGGER.error("error: " + message, (Exception)message);
		} else {
			LOGGER.error("message: " + message);
		}
	}

}
