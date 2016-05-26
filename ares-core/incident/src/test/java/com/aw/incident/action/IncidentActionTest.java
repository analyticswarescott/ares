package com.aw.incident.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.aw.action.DefaultActionFactory;
import com.aw.action.DefaultRootActionFactory;
import com.aw.action.RootActionFactory;
import com.aw.action.json.ActionModule;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.util.JSONUtils;

public class IncidentActionTest {

	@Test
	public void test() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		RootActionFactory factory = new DefaultRootActionFactory(new DefaultActionFactory(IncidentAction.class, IncidentActionType.values()));

		String actionJson = "{\"dg_guid\":\"872c9e35-a0c1-4491-9a50-5e97b01ff831\",\"dg_user\":\"1\",\"dg_comment\":\"This is a comment\",\"dg_time\":1460557938076,\"a_type\":\"incident_comment\",\"inc_guid\":\"d0d31176-2b23-4191-9f4e-912f68bcd8bc\",\"inc_ctype\":\"manual\"}";

		IncidentAction action = JSONUtils.objectFromString(actionJson, IncidentAction.class, new ActionModule(factory));
		assertTrue(action instanceof CommentIncidentAction);

		CommentIncidentAction comment = (CommentIncidentAction)action;
		assertEquals(UUID.fromString("872c9e35-a0c1-4491-9a50-5e97b01ff831"), comment.getGuid());
		assertEquals("This is a comment", comment.getComment());

	}

}
