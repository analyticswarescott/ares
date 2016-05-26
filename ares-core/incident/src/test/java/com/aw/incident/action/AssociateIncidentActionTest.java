package com.aw.incident.action;

import java.util.Arrays;

import org.junit.Test;

public class AssociateIncidentActionTest extends AbstractIncidentActionTest {

	@Test
	public void toFromJson() throws Exception {

		AssociateIncidentAction action = new EventAssociationAction();
		action.setAssociatedIds(Arrays.asList("1", "2", "3"));
		action.setIncidentGuid("123abc");
		action.setFilter("{ }");

		super.testUnityFields(action);

	}

}
