package com.aw.alarm.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Test;

import com.aw.action.ActionContext;
import com.aw.action.DefaultActionContext;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.alarm.Alarm;
import com.aw.common.inject.TestProvider;
import com.aw.common.messaging.Topic;
import com.aw.platform.PlatformMgr;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;

public class CreateAlarmActionTest {

	@Test
	public void testType() {

		assertEquals(AlarmActionType.ALARM_CREATE, new CreateAlarmAction().getType());
		assertEquals(AlarmActionType.ALARM_CREATE.name().toLowerCase(), AlarmActionType.ALARM_CREATE.toString());

	}

	@Test
	public void testExecute() throws Exception {

		CreateAlarmAction action = (CreateAlarmAction)AlarmActionType.ALARM_CREATE.newAction("{}");
		PlatformMgr platformMgr = mock(PlatformMgr.class);

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), new TestProvider<>(platformMgr));

		Data mockData = mock(Data.class);
		Alarm mockAlarm = mock(Alarm.class);
		doReturn(Collections.singleton(mockData)).when(mockAlarm).asData();

		ctx.registerVariable(AlarmAction.VAR_ALARM, mockAlarm);

		action.execute(ctx);

		//make sure the alarm data was sent
		verify(mockData).toJsonString(false, true, false);
		verify(platformMgr).sendMessage(Topic.ALARM, mockData.toJsonString(false, true, false));

	}

	@Test(expected=ActionExecutionException.class)
	public void testExecute_noAlarm() throws Exception {

		CreateAlarmAction action = new CreateAlarmAction();
		PlatformMgr platformMgr = mock(PlatformMgr.class);

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), new TestProvider<>(platformMgr));

		//no alarm should be an ActionExecutionException
		action.execute(ctx);

	}

}
