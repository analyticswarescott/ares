package com.aw.alarm;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.platform.Platform;

public class DefaultAlarmManagerTest {

	/**
	 * placeholder test
	 */
	@Test
	public void testCreateAlarm() {

		Platform platform = mock(Platform.class);

		DefaultAlarmManager mgr = new DefaultAlarmManager(new TestProvider<>(platform));

		Alarm alarm = mock(Alarm.class);

		mgr = spy(mgr);
		AlarmESClient client = mock(AlarmESClient.class);
		doReturn(client).when(mgr).newAlarmESClient(platform);
		mgr.createAlarm(alarm);

		verify(client).createAlarm(alarm);

	}

}
