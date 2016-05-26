package com.aw.alarm.action;

import com.aw.action.Action;
import com.aw.common.Taggable;

/**
 * an alarm action
 *
 *
 *
 */
public interface AlarmAction extends Action, Taggable {

	public static final String VAR_ALARM = "alarm";

	/**
	 * alarm actions are not persisted, since the alarm itself is the persisted form of this action
	 */
	@Override
	default boolean isPersistable() {
		return false;
	}

}
