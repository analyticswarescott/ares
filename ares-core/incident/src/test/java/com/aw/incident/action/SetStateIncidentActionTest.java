package com.aw.incident.action;

import java.util.UUID;

import org.junit.Test;

import com.aw.incident.Incident.State;

public class SetStateIncidentActionTest extends AbstractIncidentActionTest {

	@Test
	public void test() throws Exception {

		SetStateIncidentAction action = new SetStateIncidentAction(UUID.randomUUID(), State.CLOSED);

		super.testUnityFields(action);

	}

}
