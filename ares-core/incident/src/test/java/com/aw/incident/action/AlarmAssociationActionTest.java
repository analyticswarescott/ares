package com.aw.incident.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.aw.action.ActionContext;
import com.aw.action.DefaultActionContext;
import com.aw.alarm.Alarm;
import com.aw.alarm.action.AlarmAction;
import com.aw.common.MockProvider;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.incident.DefaultIncident;
import com.aw.platform.PlatformMgr;
import com.aw.unity.UnityInstance;

public class AlarmAssociationActionTest {

	@Before
	public void before() {
		SecurityUtil.setThreadSystemAccess();
	}

	@Test
	public void testType() {

		assertEquals(IncidentActionType.ALARM_ASSOCIATION, new AlarmAssociationAction().getType());

	}

	@Test
	public void testDefaultConstructor() {

		AlarmAssociationAction action = new AlarmAssociationAction();

		assertNotNull(action.getAssociatedIds());
		assertEquals(1, action.getAssociatedIds().size());
		assertEquals(AlarmAssociationAction.VAR_ALARM_GUID, action.getAssociatedIds().get(0));

		assertTrue(action.isPersistable());

	}

	@Test
	public void testPrepareVariable() throws Exception {

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), new MockProvider<>(PlatformMgr.class));

		//create mocks
		UUID incidentGuid = UUID.randomUUID();
		DefaultIncident incident = new DefaultIncident();
		incident.setGuid(incidentGuid);

		UUID alarmGuid = UUID.randomUUID();
		Alarm alarm = mock(Alarm.class);
		doReturn(alarmGuid).when(alarm).getGuid();

		//register variables
		ctx.registerVariable(CreateIncidentAction.VAR_INCIDENT, incident);
		ctx.registerVariable(AlarmAction.VAR_ALARM, alarm);

		AlarmAssociationAction action = new AlarmAssociationAction();
		action.prepare(ctx);

	}

}
