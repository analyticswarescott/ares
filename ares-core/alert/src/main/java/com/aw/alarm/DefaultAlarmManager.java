package com.aw.alarm;

import javax.inject.Provider;

import com.aw.platform.Platform;

/**
 * default alarm manager implementation
 *
 *
 *
 */
public class DefaultAlarmManager implements AlarmManager {

	Provider<Platform> platform;

	public DefaultAlarmManager(Provider<Platform> platform) {
		this.platform = platform;
	}

	/**
	 * create an alarm in the elasticsearch alarm index
	 */
	@Override
	public void createAlarm(Alarm alarm) {

		AlarmESClient client = newAlarmESClient(platform);
		client.createAlarm(alarm);

	}

	/**
	 * @param platform the platform for which the client should be created
	 * @return a new alarm elasticsearch client
	 */
	protected AlarmESClient newAlarmESClient(Provider<Platform> platform) {
		return new AlarmESClient(platform);
	}

}
