package com.aw.incident.action;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.incident.DefaultIncident;
import com.aw.incident.Incident;
import org.junit.Test;

public class CreateIncidentActionTest extends AbstractIncidentActionTest {

	@Test
	public void test() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		Incident incident = new DefaultIncident();
		CreateIncidentAction action = new CreateIncidentAction(incident);
		action.setComment("test comment");
		action.setIncident(incident);

		super.testUnityFields(action);

	}

}
