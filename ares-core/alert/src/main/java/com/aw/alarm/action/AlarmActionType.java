package com.aw.alarm.action;

import com.aw.action.ActionType;

/**
 * all known types of alarm actions
 *
 *
 *
 */
public enum AlarmActionType implements ActionType {

	ALARM_CREATE(CreateAlarmAction.class);

	private AlarmActionType(Class<? extends AlarmAction> type) {
		this.type = type;
	}

	@Override
	public Class<? extends AlarmAction> getClassType() { return type; }
	private Class<? extends AlarmAction> type;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
