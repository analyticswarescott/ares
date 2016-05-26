package com.aw.incident.action;

import java.util.UUID;

import org.junit.Test;

import com.aw.common.auth.DefaultUser;

public class AssignIncidentActionTest extends AbstractIncidentActionTest {

	@Test
	public void toFromJson() throws Exception {

		AssignIncidentAction action = new AssignIncidentAction(UUID.fromString("369ea8b7-a401-41e1-8f54-8f562ddcdaa4"), new DefaultUser("test_user"));

		super.testUnityFields(action);

	}

	@Test
	public void copy() throws Exception {

		AssignIncidentAction action = new AssignIncidentAction(UUID.fromString("369ea8b7-a401-41e1-8f54-8f562ddcdaa4"), new DefaultUser("test_user"));
		AssignIncidentAction action2 = action.copy();

	}

}
