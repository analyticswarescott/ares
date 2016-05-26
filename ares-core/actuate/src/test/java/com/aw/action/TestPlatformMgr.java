package com.aw.action;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformMgr;

/**
 * TODO: common test classes should reside in a test module for the corresponding library that any dependent unit tests can use
 *
 *
 *
 */
public class TestPlatformMgr extends PlatformMgr {

	@Override
	public void sendMessage(Topic topic, Object payload) throws Exception {
		//no-op
	}

	@Override
	public void sendMessage(Topic topic, Tenant tenant, Object payload) throws Exception {
		//no-op
	}

}
