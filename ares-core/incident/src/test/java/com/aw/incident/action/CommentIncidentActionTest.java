package com.aw.incident.action;

import java.util.UUID;

import org.junit.Test;

public class CommentIncidentActionTest extends AbstractIncidentActionTest {

	@Test
	public void toFromJson() throws Exception {

		CommentIncidentAction action = new CommentIncidentAction();
		action.setIncidentGuid(UUID.randomUUID().toString());
		action.setComment("this is a test comment");

		super.testUnityFields(action);

	}

	@Test
	public void variables() throws Exception {

	}

}
