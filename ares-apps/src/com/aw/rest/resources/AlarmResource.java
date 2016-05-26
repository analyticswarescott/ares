package com.aw.rest.resources;

import javax.ws.rs.POST;

import com.aw.alarm.Alarm;
import com.aw.alarm.AlarmManager;

public class AlarmResource implements AlarmManager {

	private AlarmManager manager;

	public AlarmResource(AlarmManager alarmManager) {
		this.manager = alarmManager;
	}

	/**
	 * create an alarm
	 */
	@POST
	@Override
	public void createAlarm(Alarm alarm) {

		manager.createAlarm(alarm);

	}

}
